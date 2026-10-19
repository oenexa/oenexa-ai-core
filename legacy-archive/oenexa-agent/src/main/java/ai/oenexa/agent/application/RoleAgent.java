package ai.oenexa.agent.application;

import ai.oenexa.agent.domain.Agent;
import ai.oenexa.agent.domain.AgentContext;
import ai.oenexa.agent.domain.AgentMemory;
import ai.oenexa.agent.domain.AgentReasoner;
import ai.oenexa.agent.domain.AgentType;
import ai.oenexa.agent.domain.Tool;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Generic agent: a role prompt + memory + optional tools + optional delegation rule.
 * All 15 agent types are instances of this class configured by {@link AgentCatalog}.
 */
public class RoleAgent implements Agent {

    private final AgentType type;
    private final String rolePrompt;
    private final AgentReasoner reasoner;
    private final Tool.Registry tools;
    private final List<String> toolNames;
    private final Function<String, Optional<Delegation>> delegationRule;
    private final AgentMemory memory = new AgentMemory(50);

    public RoleAgent(AgentType type, String rolePrompt, AgentReasoner reasoner, Tool.Registry tools,
                     List<String> toolNames, Function<String, Optional<Delegation>> delegationRule) {
        this.type = type;
        this.rolePrompt = rolePrompt;
        this.reasoner = reasoner;
        this.tools = tools;
        this.toolNames = toolNames;
        this.delegationRule = delegationRule;
    }

    @Override
    public AgentType type() {
        return type;
    }

    public List<String> remembered() {
        return memory.recall();
    }

    @Override
    public Mono<Result> execute(Task task, AgentContext context) {
        Mono<String> toolOutput = Flux.fromIterable(toolNames)
                .concatMap(name -> tools.call(name, task.objective()).map(out -> "[" + name + "] " + out))
                .collectList()
                .map(list -> String.join("\n", list));

        return toolOutput.defaultIfEmpty("").flatMap(observations -> {
            String input = """
                    Objective: %s
                    Prior agent findings: %s
                    Memory: %s
                    Tool observations: %s""".formatted(task.objective(), context.shared(), memory.recall(), observations);
            return reasoner.think(rolePrompt, input);
        }).map(output -> {
            memory.remember(output);
            context.log(type + ": " + output);
            return new Result(output, Map.of(), delegationRule.apply(output));
        });
    }
}
