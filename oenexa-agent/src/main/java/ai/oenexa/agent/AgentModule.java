package ai.oenexa.agent;

import ai.oenexa.agent.application.AgentCatalog;
import ai.oenexa.agent.application.Coordinator;
import ai.oenexa.agent.application.TemplateReasoner;
import ai.oenexa.agent.domain.AgentReasoner;
import ai.oenexa.agent.domain.Tool;
import ai.oenexa.agent.web.AgentController;
import ai.oenexa.common.module.PlatformModule;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(AgentController.class)
public class AgentModule implements PlatformModule {

    @Bean
    @ConditionalOnMissingBean
    AgentReasoner agentReasoner() {
        return new TemplateReasoner();
    }

    @Bean
    @ConditionalOnMissingBean
    Tool.Registry toolRegistry(ObjectProvider<Tool> tools) {
        Tool.Registry registry = new Tool.Registry();
        tools.orderedStream().forEach(registry::register);
        return registry;
    }

    @Bean
    Coordinator coordinator(AgentReasoner reasoner, Tool.Registry tools) {
        return new Coordinator(AgentCatalog.build(reasoner, tools));
    }

    @Override
    public String id() {
        return "agents";
    }

    @Override
    public String displayName() {
        return "OENEXA AI Agents";
    }

    @Override
    public String description() {
        return "15 specialised agents with memory, tools and delegation, run by a coordinator.";
    }

    @Override
    public List<String> capabilities() {
        return List.of("coordinator", "workflows", "delegation", "agent-memory", "tool-registry");
    }

    @Override
    public ModuleStatus status() {
        return ModuleStatus.ACTIVE;
    }
}
