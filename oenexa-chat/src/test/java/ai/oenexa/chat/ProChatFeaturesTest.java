package ai.oenexa.chat;

import ai.oenexa.chat.application.ArtifactExtractor;
import ai.oenexa.chat.application.ChatService;
import ai.oenexa.chat.application.GeminiReasoningEngine;
import ai.oenexa.chat.application.ToolRegistry;
import ai.oenexa.chat.domain.ChatMessage;
import ai.oenexa.chat.domain.ChatTool;
import ai.oenexa.chat.domain.CodeArtifact;
import ai.oenexa.chat.domain.CodeContextAttachment;
import ai.oenexa.chat.domain.ConversationMetadata;
import ai.oenexa.chat.domain.ToolCall;
import ai.oenexa.chat.domain.ToolResult;
import ai.oenexa.chat.infra.EchoLlmAdapter;
import ai.oenexa.chat.infra.InMemoryConversationStore;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pro Chat Features Zero-Mock Unit Tests")
class ProChatFeaturesTest {

    @Test
    void testToolRegistryAndDefaultTools() {
        ToolRegistry registry = new ToolRegistry();
        assertThat(registry.listTools()).hasSize(5);

        // AST Analyzer
        ChatTool astTool = registry.get("ast_analyzer");
        assertThat(astTool).isNotNull();
        assertThat(astTool.name()).isEqualTo("ast_analyzer");
        assertThat(astTool.description()).contains("AST");
        assertThat(astTool.parameterSchema()).containsKey("code");
        String astOut = astTool.execute(Map.of("code", "public class Foo {\n  public void bar() { if (true) {} }\n}"));
        assertThat(astOut).contains("AST Analysis");
        assertThat(astOut).contains("cyclomatic complexity");

        // Security Scanner
        ChatTool secTool = registry.get("security_scanner");
        assertThat(secTool.execute(Map.of("code", "String key = \"AKIA1234567890\";"))).contains("SECURITY WARNING");
        assertThat(secTool.execute(Map.of("code", "int a = 1;"))).contains("0 critical vulnerabilities");

        // Test Synthesizer
        ChatTool testTool = registry.get("test_synthesizer");
        assertThat(testTool.execute(Map.of("className", "UserService"))).contains("UserService");

        // Code Refactoring
        ChatTool refactorTool = registry.get("code_refactoring");
        assertThat(refactorTool.execute(Map.of("refactoringType", "EXTRACT_METHOD"))).contains("EXTRACT_METHOD");

        // Architecture Diagram
        ChatTool archTool = registry.get("architecture_diagram");
        assertThat(archTool.execute(Map.of("systemName", "PaymentService"))).contains("PaymentService");

        // Execution via ToolCall
        ToolCall validCall = new ToolCall("c1", "test_synthesizer", Map.of("className", "OrderService"));
        ToolResult validRes = registry.execute(validCall);
        assertThat(validRes.isError()).isFalse();
        assertThat(validRes.output()).contains("OrderService");

        // Null tool call and unknown tool
        ToolResult nullCallRes = registry.execute(null);
        assertThat(nullCallRes.isError()).isTrue();

        ToolResult unknownCallRes = registry.execute(new ToolCall("c2", "non_existent_tool"));
        assertThat(unknownCallRes.isError()).isTrue();

        // Custom Tool registration and exception handling
        registry.register(new ChatTool() {
            @Override public String name() { return "failing_tool"; }
            @Override public String description() { return "fails"; }
            @Override public Map<String, Object> parameterSchema() { return Map.of(); }
            @Override public String execute(Map<String, Object> parameters) { throw new RuntimeException("Simulated error"); }
        });
        ToolResult failingRes = registry.execute(new ToolCall("c3", "failing_tool"));
        assertThat(failingRes.isError()).isTrue();
        assertThat(failingRes.output()).contains("Simulated error");

        // Null registrations or lookups
        registry.register(null);
        assertThat(registry.get(null)).isNull();
    }

    @Test
    void testArtifactExtractor() {
        ArtifactExtractor extractor = new ArtifactExtractor();
        assertThat(extractor.extract(null)).isEmpty();
        assertThat(extractor.extract("   ")).isEmpty();
        assertThat(extractor.extract("No code blocks here.")).isEmpty();

        String markdown = """
                Here is the code:
                ```java
                // file: src/main/java/App.java
                public class App {}
                ```

                And a patch:
                ```diff
                --- a/file.txt
                +++ b/file.txt
                @@ -1,1 +1,1 @@
                -old
                +new
                ```

                And other files:
                ```typescript
                const x = 1;
                ```
                ```json
                {"key": "val"}
                ```
                ```yaml
                kind: Pod
                ```
                ```mermaid
                graph TD;
                ```
                ```sql
                SELECT 1;
                ```
                ```dockerfile
                FROM alpine
                ```
                ```
                Plain snippet
                ```
                """;

        List<CodeArtifact> artifacts = extractor.extract(markdown);
        assertThat(artifacts).hasSize(9);

        // Check custom file path extraction
        CodeArtifact javaArtifact = artifacts.get(0);
        assertThat(javaArtifact.filePath()).isEqualTo("src/main/java/App.java");
        assertThat(javaArtifact.title()).isEqualTo("App.java");
        assertThat(javaArtifact.action()).isEqualTo("CREATE");

        // Check diff patch action
        CodeArtifact diffArtifact = artifacts.get(1);
        assertThat(diffArtifact.action()).isEqualTo("PATCH");

        // Check default constructor of CodeArtifact
        CodeArtifact simple = new CodeArtifact("Title", "path.txt", "code");
        assertThat(simple.artifactId()).isNotNull();
        assertThat(simple.action()).isEqualTo("CREATE");
    }

    @Test
    void testDomainRecords() {
        ToolCall call = new ToolCall("c1", "tool_a");
        assertThat(call.arguments()).isEmpty();

        ToolResult res = new ToolResult("c1", "tool_a", "output");
        assertThat(res.isError()).isFalse();

        CodeContextAttachment attachment = new CodeContextAttachment("src/Main.java", "code");
        assertThat(attachment.language()).isEqualTo("plaintext");
        assertThat(attachment.startLine()).isEqualTo(1);

        CodeContextAttachment fullAtt = new CodeContextAttachment("src/App.java", "java", 5, 15, "code");
        assertThat(fullAtt.endLine()).isEqualTo(15);

        ConversationMetadata meta = new ConversationMetadata(UUID.randomUUID(), UUID.randomUUID(), "Title");
        assertThat(meta.model()).isEqualTo("gemini-2.5-flash");
        assertThat(meta.messageCount()).isZero();
        assertThat(meta.pinned()).isFalse();

        ConversationMetadata inc = meta.withIncrementedCount();
        assertThat(inc.messageCount()).isEqualTo(1);

        ConversationMetadata renamed = inc.withTitle("Updated Title");
        assertThat(renamed.title()).isEqualTo("Updated Title");

        ConversationMetadata pinned = renamed.withPinned(true);
        assertThat(pinned.pinned()).isTrue();

        ChatMessage proMsg = ChatMessage.of(
                ChatMessage.Role.TOOL,
                "tool response",
                "thinking",
                "gemini-2.5-pro",
                List.of(fullAtt),
                List.of(call),
                List.of(res),
                List.of(new CodeArtifact("T", "f", "c"))
        );
        assertThat(proMsg.role()).isEqualTo(ChatMessage.Role.TOOL);
        assertThat(proMsg.attachments()).isNotEmpty();
        assertThat(proMsg.toolCalls()).isNotEmpty();
        assertThat(proMsg.toolResults()).isNotEmpty();
        assertThat(proMsg.artifacts()).isNotEmpty();
        assertThat(proMsg.estimatedTokens()).isGreaterThan(1);
    }

    @Test
    void testInMemoryStoreAndChatServiceProMethods() {
        InMemoryConversationStore store = new InMemoryConversationStore();
        ChatService service = new ChatService(store, new EchoLlmAdapter(), "System Prompt", 4000);
        UUID tenantId = UUID.randomUUID();
        UUID convId = UUID.randomUUID();

        // Create conversation
        StepVerifier.create(service.createConversation(tenantId, convId, "Initial Session", "gemini-2.5-pro"))
                .assertNext(meta -> assertThat(meta.title()).isEqualTo("Initial Session"))
                .verifyComplete();

        // Update Title
        StepVerifier.create(service.updateTitle(tenantId, convId, "Renamed Session"))
                .assertNext(meta -> assertThat(meta.title()).isEqualTo("Renamed Session"))
                .verifyComplete();

        // Set Pinned
        StepVerifier.create(service.setPinned(tenantId, convId, true))
                .assertNext(meta -> assertThat(meta.pinned()).isTrue())
                .verifyComplete();

        // Get Metadata
        StepVerifier.create(service.getMetadata(tenantId, convId))
                .assertNext(meta -> {
                    assertThat(meta.title()).isEqualTo("Renamed Session");
                    assertThat(meta.pinned()).isTrue();
                })
                .verifyComplete();

        // Send message with attachments via reply
        var attachment = new CodeContextAttachment("src/Foo.java", "java", 1, 5, "class Foo {}");
        StepVerifier.create(service.reply(tenantId, convId, "Refactor clean code", "gemini-2.5-pro", true, List.of(attachment)).collectList())
                .assertNext(chunks -> assertThat(String.join("", chunks)).isNotEmpty())
                .verifyComplete();

        // Complete turn synchronously
        GeminiReasoningEngine engine = new GeminiReasoningEngine();
        StepVerifier.create(service.complete(tenantId, convId, "Synthesize test coverage", "gemini-2.5-pro", true, List.of(attachment), engine))
                .assertNext(turn -> {
                    assertThat(turn.intent()).isEqualTo("TESTING_SYNTHESIS");
                    assertThat(turn.toolCalls()).isNotEmpty();
                    assertThat(turn.confidenceScore()).isEqualTo(0.98);
                })
                .verifyComplete();

        // Stream structured events
        StepVerifier.create(service.streamEvents(tenantId, convId, "Analyze security", "gemini-2.5-flash", true, List.of()))
                .assertNext(ev -> assertThat(ev.event()).isEqualTo("session_start"))
                .thenConsumeWhile(ev -> !ev.event().equals("done"))
                .assertNext(ev -> assertThat(ev.event()).isEqualTo("done"))
                .verifyComplete();

        // List Conversations
        StepVerifier.create(service.listConversations(tenantId))
                .assertNext(list -> {
                    assertThat(list).isNotEmpty();
                    assertThat(list.get(0).conversationId()).isEqualTo(convId);
                })
                .verifyComplete();

        // Fork conversation
        UUID forkedId = UUID.randomUUID();
        StepVerifier.create(service.forkConversation(tenantId, convId, forkedId, 1))
                .assertNext(forkMeta -> {
                    assertThat(forkMeta.conversationId()).isEqualTo(forkedId);
                    assertThat(forkMeta.title()).contains("Fork of");
                })
                .verifyComplete();

        // Clear history
        StepVerifier.create(service.clearHistory(tenantId, convId))
                .verifyComplete();

        StepVerifier.create(service.history(tenantId, convId))
                .assertNext(h -> assertThat(h).isEmpty())
                .verifyComplete();

        // Delete conversation
        StepVerifier.create(service.deleteConversation(tenantId, convId))
                .verifyComplete();

        StepVerifier.create(service.listConversations(tenantId))
                .assertNext(list -> assertThat(list.stream().noneMatch(m -> m.conversationId().equals(convId))).isTrue())
                .verifyComplete();
    }

    @Test
    void testGeminiReasoningEngineProFeatures() {
        ToolRegistry registry = new ToolRegistry();
        ArtifactExtractor extractor = new ArtifactExtractor();
        GeminiReasoningEngine engine = new GeminiReasoningEngine(registry, extractor);

        assertThat(engine.toolRegistry()).isSameAs(registry);
        assertThat(engine.artifactExtractor()).isSameAs(extractor);

        // TurnResult legacy constructor
        var legacyTurn = new GeminiReasoningEngine.TurnResult("thought", "response", "intent", List.of("follow-up"));
        assertThat(legacyTurn.confidenceScore()).isEqualTo(0.95);
        assertThat(legacyTurn.toolCalls()).isEmpty();

        // Test with attachments and intents
        var attachment = new CodeContextAttachment("src/Bar.java", "class Bar {}");
        var turnWithAttach = engine.process("Generate code for data model", "gemini-2.5-pro", true, List.of(attachment));
        assertThat(turnWithAttach.thought()).contains("Attached Context Evaluated");
        assertThat(turnWithAttach.thought()).contains("src/Bar.java");
        assertThat(turnWithAttach.toolCalls()).isNotEmpty();
        assertThat(turnWithAttach.confidenceScore()).isEqualTo(0.98);

        // Test stream with ChatMessage containing attachments
        ChatMessage msgWithAtt = ChatMessage.of(
                ChatMessage.Role.USER,
                "Show architecture design",
                null,
                "gemini-2.5-pro",
                List.of(attachment),
                List.of(),
                List.of(),
                List.of()
        );
        StepVerifier.create(engine.stream(List.of(msgWithAtt), "gemini-2.5-pro").collectList())
                .assertNext(chunks -> {
                    String full = String.join("", chunks);
                    assertThat(full).contains("<thought>");
                    assertThat(full).contains("OENEXA");
                })
                .verifyComplete();
    }

    @Test
    void testToolMetadataAndEdgeCases() {
        ToolRegistry registry = new ToolRegistry();
        for (ChatTool tool : registry.listTools()) {
            assertThat(tool.name()).isNotBlank();
            assertThat(tool.description()).isNotBlank();
            assertThat(tool.parameterSchema()).isNotEmpty();
        }

        // Test with empty parameters fallbacks
        ChatTool testTool = registry.get("test_synthesizer");
        assertThat(testTool.execute(Map.of())).contains("Component");

        ChatTool refactorTool = registry.get("code_refactoring");
        assertThat(refactorTool.execute(Map.of())).contains("EXTRACT_RECORD");

        ChatTool archTool = registry.get("architecture_diagram");
        assertThat(archTool.execute(Map.of())).contains("OENEXA");

        ChatTool secTool = registry.get("security_scanner");
        assertThat(secTool.execute(Map.of("code", "String password = \"secret\";"))).contains("SECURITY WARNING");
    }

    @Test
    void testArtifactExtractorEdgeCases() {
        ArtifactExtractor extractor = new ArtifactExtractor();
        
        // Blank code block is ignored
        String textWithBlank = "```java\n   \n```\nSome text\n```java\npublic class A {}\n```";
        List<CodeArtifact> artifacts = extractor.extract(textWithBlank);
        assertThat(artifacts).hasSize(1);
        assertThat(artifacts.get(0).language()).isEqualTo("java");
        assertThat(artifacts.get(0).filePath()).isEqualTo("src/main/java/Component1.java");

        // JS language default
        String jsText = "```js\nconsole.log('hi');\n```";
        List<CodeArtifact> jsArtifacts = extractor.extract(jsText);
        assertThat(jsArtifacts).hasSize(1);
        assertThat(jsArtifacts.get(0).filePath()).isEqualTo("src/index1.js");
    }

    @Test
    void testInMemoryConversationStoreEdgeCases() {
        InMemoryConversationStore store = new InMemoryConversationStore();
        UUID tenantId = UUID.randomUUID();
        UUID convId1 = UUID.randomUUID();
        UUID convId2 = UUID.randomUUID();

        // Append message with length > 40 chars
        String longText = "This is a very long text exceeding forty characters for conversation titling";
        ChatMessage msg1 = ChatMessage.of(ChatMessage.Role.USER, longText);
        StepVerifier.create(store.append(tenantId, convId1, msg1)).verifyComplete();
        StepVerifier.create(store.getMetadata(tenantId, convId1))
                .assertNext(m -> assertThat(m.title()).endsWith("..."))
                .verifyComplete();

        // Append message with blank text
        ChatMessage msgBlank = ChatMessage.of(ChatMessage.Role.USER, "   ");
        StepVerifier.create(store.append(tenantId, convId2, msgBlank)).verifyComplete();
        StepVerifier.create(store.getMetadata(tenantId, convId2))
                .assertNext(m -> assertThat(m.title()).isEqualTo("New Conversation"))
                .verifyComplete();

        // createConversation with blank title and blank model
        UUID convId3 = UUID.randomUUID();
        StepVerifier.create(store.createConversation(tenantId, convId3, "   ", "   "))
                .assertNext(m -> {
                    assertThat(m.title()).isEqualTo("New Conversation");
                    assertThat(m.model()).isEqualTo("gemini-2.5-flash");
                })
                .verifyComplete();

        // updateTitle and setPinned on non-existent conversation
        UUID convId4 = UUID.randomUUID();
        StepVerifier.create(store.updateTitle(tenantId, convId4, "Brand New Title"))
                .assertNext(m -> assertThat(m.title()).isEqualTo("Brand New Title"))
                .verifyComplete();

        UUID convId5 = UUID.randomUUID();
        StepVerifier.create(store.setPinned(tenantId, convId5, true))
                .assertNext(m -> assertThat(m.pinned()).isTrue())
                .verifyComplete();

        // forkConversation on non-existent source conversation
        UUID forkedId = UUID.randomUUID();
        StepVerifier.create(store.forkConversation(tenantId, UUID.randomUUID(), forkedId, -1))
                .assertNext(m -> {
                    assertThat(m.title()).isEqualTo("Forked Conversation");
                    assertThat(m.model()).isEqualTo("gemini-2.5-flash");
                })
                .verifyComplete();
    }

    @Test
    void testReasoningEngineConstructorsAndErrorConfidence() {
        // Constructor with nulls
        GeminiReasoningEngine engine = new GeminiReasoningEngine(null, null);
        assertThat(engine.toolRegistry()).isNotNull();
        assertThat(engine.artifactExtractor()).isNotNull();

        // Null input
        var turnNull = engine.process(null, "gemini-2.5-flash", false, List.of());
        assertThat(turnNull.response()).isNotNull();

        // Refactoring intent
        var turnRefactor = engine.process("Refactor this method", "gemini-2.5-flash", true, List.of());
        assertThat(turnRefactor.toolCalls()).isNotEmpty();
        assertThat(turnRefactor.toolCalls().get(0).toolName()).isEqualTo("code_refactoring");

        // Error result confidence
        ToolResult errorResult = new ToolResult("call-1", "ast_analyzer", "Failed to parse", true);
        assertThat(errorResult.isError()).isTrue();
        assertThat(errorResult.output()).isEqualTo("Failed to parse");

        ToolRegistry customRegistry = new ToolRegistry();
        customRegistry.register(new ChatTool() {
            @Override public String name() { return "ast_analyzer"; }
            @Override public String description() { return "Fails"; }
            @Override public Map<String, Object> parameterSchema() { return Map.of(); }
            @Override public String execute(Map<String, Object> parameters) {
                throw new RuntimeException("Crash");
            }
        });
        ToolResult execResult = customRegistry.execute(new ToolCall("id-1", "ast_analyzer", Map.of()));
        assertThat(execResult.isError()).isTrue();

        GeminiReasoningEngine failingEngine = new GeminiReasoningEngine(customRegistry, new ArtifactExtractor());
        var failTurn = failingEngine.process("Generate code for model", "gemini-2.5-flash", true, List.of());
        assertThat(failTurn.confidenceScore()).isEqualTo(0.70);
    }

    @Test
    void testSpringAiAdapterAndChatMessageConstructors() {
        // Spring AI Tool role conversion
        ChatMessage toolMsg = new ChatMessage(ChatMessage.Role.TOOL, "tool output", Instant.now(), null, "gemini-2.5-flash");
        var springAiMsg = ai.oenexa.chat.infra.SpringAiLlmAdapter.toSpringAi(toolMsg);
        assertThat(springAiMsg).isNotNull();
        assertThat(springAiMsg.getText()).contains("Tool Output: tool output");

        // ChatMessage 3-arg constructor
        ChatMessage userMsg = new ChatMessage(ChatMessage.Role.USER, "hello", Instant.now());
        assertThat(userMsg.role()).isEqualTo(ChatMessage.Role.USER);
        assertThat(userMsg.model()).isEqualTo("gemini-2.5-flash");
    }
}
