package com.oenexa.api.mapper;

import com.oenexa.api.dto.agent.AgentDtos.AgentDirectiveDto;
import com.oenexa.api.dto.agent.AgentDtos.GoalExecutionStatusDto;
import com.oenexa.api.dto.agent.AgentDtos.GoalSubmissionDto;
import com.oenexa.api.dto.auth.AuthDtos.LoginRequestDto;
import com.oenexa.api.dto.auth.AuthDtos.RegisterRequestDto;
import com.oenexa.api.dto.auth.AuthDtos.TokenResponseDto;
import com.oenexa.api.dto.auth.AuthDtos.UserProfileDto;
import com.oenexa.api.dto.chat.ChatDtos.ChatTurnResponseDto;
import com.oenexa.api.dto.chat.ChatDtos.ConversationSummaryDto;
import com.oenexa.api.dto.chat.ChatDtos.SendMessageRequestDto;
import com.oenexa.api.dto.code.CodeDtos.CodeAnalysisRequestDto;
import com.oenexa.api.dto.code.CodeDtos.CodeAnalysisResultDto;
import com.oenexa.api.dto.code.CodeDtos.SecurityAuditSummaryDto;
import com.oenexa.api.dto.code.CodeDtos.TestSynthesisResultDto;
import com.oenexa.api.dto.knowledge.KnowledgeDtos.DocumentChunkDto;
import com.oenexa.api.dto.knowledge.KnowledgeDtos.DocumentIngestRequestDto;
import com.oenexa.api.dto.knowledge.KnowledgeDtos.KnowledgeSearchResultDto;
import com.oenexa.api.dto.memory.MemoryDtos.MemoryHierarchyStatsDto;
import com.oenexa.api.dto.memory.MemoryDtos.MemoryLookupResponseDto;
import com.oenexa.domain.model.auth.AuthDomain.LoginRequest;
import com.oenexa.domain.model.auth.AuthDomain.RegisterRequest;
import com.oenexa.domain.model.auth.AuthDomain.TokenPair;
import com.oenexa.domain.model.auth.AuthDomain.UserProfile;
import com.oenexa.domain.model.chat.ChatDomain.*;
import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisRequest;
import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisResponse;
import com.oenexa.domain.model.code.CodeEngineeringDomain.SecurityScanResponse;
import com.oenexa.domain.model.code.CodeEngineeringDomain.TestGenerationResponse;
import com.oenexa.domain.model.memory.EnterpriseMemoryHierarchyDomain.MemoryHierarchyStats;
import com.oenexa.domain.model.memory.EnterpriseMemoryHierarchyDomain.MemoryLayerTier;
import com.oenexa.domain.model.memory.EnterpriseMemoryHierarchyDomain.MemoryRetrievalResult;
import com.oenexa.domain.model.cognitive.OenexaCoreBrainDomain.CoreBrainDirective;
import com.oenexa.domain.model.knowledge.RagDocument.Chunk;
import com.oenexa.domain.model.knowledge.RagDocument.DocumentInput;
import com.oenexa.domain.model.knowledge.RagDocument.SearchResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("API Mappers Zero-Mock Unit Tests")
class ApiMappersTest {

    @Test
    @DisplayName("AuthApiMapper maps between Auth DTOs and domain models")
    void testAuthApiMapper() {
        AuthApiMapper mapper = new AuthApiMapper();
        assertNull(mapper.toDomainLogin(null));
        assertNull(mapper.toDomainRegister(null));
        assertNull(mapper.toTokenDto(null));
        assertNull(mapper.toProfileDto(null));

        LoginRequest login = mapper.toDomainLogin(new LoginRequestDto("admin", "secret"));
        assertNotNull(login);
        assertEquals("admin", login.usernameOrEmail());

        RegisterRequest register = mapper.toDomainRegister(new RegisterRequestDto("dev", "dev@oenexa.ai", "pass123", "Lead Dev"));
        assertNotNull(register);
        assertEquals("dev@oenexa.ai", register.email());

        TokenResponseDto tokenDto = mapper.toTokenDto(new TokenPair("acc-123", "ref-123", 900));
        assertNotNull(tokenDto);
        assertEquals("acc-123", tokenDto.accessToken());
        assertEquals("Bearer", tokenDto.tokenType());

        UserProfile profile = new UserProfile(1L, "dev", "dev@oenexa.ai", "Lead", "Dev", "DE", null, null, null, null, "en", "UTC", "FREE", "ROLE_ENGINEER", "t-1", false, Instant.now());
        UserProfileDto profileDto = mapper.toProfileDto(profile);
        assertNotNull(profileDto);
        assertEquals("Lead Dev", profileDto.fullName());
    }

    @Test
    @DisplayName("ChatApiMapper maps between Chat DTOs and domain models")
    void testChatApiMapper() {
        ChatApiMapper mapper = new ChatApiMapper();
        assertNull(mapper.toDomainRequest(null));
        assertNull(mapper.toTurnResponseDto(null));
        assertNull(mapper.toSummaryDto(null));

        ChatRequest req = mapper.toDomainRequest(new SendMessageRequestDto("Hello", "c-1", "gemini-2.5-flash", true));
        assertNotNull(req);
        assertEquals("Hello", req.message());

        ChatResponse resp = new ChatResponse("Reply", "c-1", 100);
        ChatTurnResponseDto turnDto = mapper.toTurnResponseDto(resp);
        assertNotNull(turnDto);
        assertEquals("Reply", turnDto.reply());

        ConversationMetadata meta = new ConversationMetadata("c-1", "Chat Title");
        ConversationSummaryDto summary = mapper.toSummaryDto(meta);
        assertNotNull(summary);
        assertEquals("Chat Title", summary.title());
    }

    @Test
    @DisplayName("AgentApiMapper maps between Agent DTOs and domain models")
    void testAgentApiMapper() {
        AgentApiMapper mapper = new AgentApiMapper();
        assertNull(mapper.toDomainGoalRequest(null, "u1"));
        assertNull(mapper.toGoalStatusDto(null));
        assertNull(mapper.toBrainDirective(null));

        OenexaAutonomousGoalRequest goalReq = mapper.toDomainGoalRequest(new GoalSubmissionDto("Refactor core", "repo-1", true, true), "user-99");
        assertNotNull(goalReq);
        assertEquals("Refactor core", goalReq.goal());

        OenexaAutonomousGoalResponse goalResp = new OenexaAutonomousGoalResponse("g-1", "Refactor core", null, List.of(), List.of(), List.of(), 0.99, "Done", Instant.now());
        GoalExecutionStatusDto statusDto = mapper.toGoalStatusDto(goalResp);
        assertNotNull(statusDto);
        assertEquals("g-1", statusDto.goalId());

        CoreBrainDirective directive = mapper.toBrainDirective(new AgentDirectiveDto("d-1", "Optimize DB", "oenexa-neo-gpt-o3-max", "AUTONOMOUS", Map.of()));
        assertNotNull(directive);
        assertEquals("Optimize DB", directive.objective());
    }

    @Test
    @DisplayName("KnowledgeApiMapper maps between Knowledge DTOs and domain models")
    void testKnowledgeApiMapper() {
        KnowledgeApiMapper mapper = new KnowledgeApiMapper();
        assertNull(mapper.toDomainInput(null));
        assertNull(mapper.toChunkDto(null));
        assertNull(mapper.toSearchDto(null));

        DocumentInput input = mapper.toDomainInput(new DocumentIngestRequestDto("Arch", "Details", "repo-1", "markdown"));
        assertNotNull(input);
        assertEquals("Arch", input.title());

        Chunk chunk = new Chunk("ch-1", "Doc", "Snippet", 0);
        DocumentChunkDto chunkDto = mapper.toChunkDto(chunk);
        assertNotNull(chunkDto);
        assertEquals("ch-1", chunkDto.chunkId());

        SearchResult search = new SearchResult("ch-1", "Doc", "Snippet", 0.92);
        KnowledgeSearchResultDto searchDto = mapper.toSearchDto(search);
        assertNotNull(searchDto);
        assertEquals(0.92, searchDto.relevanceScore());
    }

    @Test
    @DisplayName("MemoryApiMapper maps between Memory DTOs and domain models")
    void testMemoryApiMapper() {
        MemoryApiMapper mapper = new MemoryApiMapper();
        assertNull(mapper.toLookupDto(null));
        assertNull(mapper.toStatsDto(null));

        MemoryRetrievalResult res = new MemoryRetrievalResult("key:1", MemoryLayerTier.L2_JVM_HEAP_RAM, "payload", 50, MemoryLayerTier.L2_JVM_HEAP_RAM, true, false);
        MemoryLookupResponseDto lookupDto = mapper.toLookupDto(res);
        assertNotNull(lookupDto);
        assertTrue(lookupDto.hit());
        assertEquals("payload", lookupDto.content());

        MemoryHierarchyStats stats = new MemoryHierarchyStats(100, 50, 30, 20, 0, 0, 1024000, 204800, 0.94);
        MemoryHierarchyStatsDto statsDto = mapper.toStatsDto(stats);
        assertNotNull(statsDto);
        assertEquals(50, statsDto.ramEntries());
    }

    @Test
    @DisplayName("CodeApiMapper maps between Code DTOs and domain models")
    void testCodeApiMapper() {
        CodeApiMapper mapper = new CodeApiMapper();
        assertNull(mapper.toDomainAnalysisRequest(null));
        assertNull(mapper.toAnalysisResultDto(null));
        assertNull(mapper.toSecuritySummaryDto(null));
        assertNull(mapper.toTestResultDto(null));

        CodeAnalysisRequest req = mapper.toDomainAnalysisRequest(new CodeAnalysisRequestDto("class A {}", "java", "A.java"));
        assertNotNull(req);
        assertEquals("java", req.language());

        CodeAnalysisResponse analysis = new CodeAnalysisResponse("java", 100, 2, 5, 7, List.of("A"), List.of("m1"), List.of(), List.of(), true);
        CodeAnalysisResultDto analysisDto = mapper.toAnalysisResultDto(analysis);
        assertNotNull(analysisDto);
        assertEquals(100, analysisDto.lines());

        SecurityScanResponse scan = new SecurityScanResponse("s-1", true, 0, 0, 0, 0, 0, List.of(), List.of(), "Passed", "OWASP");
        SecurityAuditSummaryDto secDto = mapper.toSecuritySummaryDto(scan);
        assertNotNull(secDto);
        assertTrue(secDto.passed());

        TestGenerationResponse testGen = new TestGenerationResponse("CalcTest", "JUNIT_5", "class CalcTest {}", "class CalcTest {}", List.of("testAdd"), 100, true);
        TestSynthesisResultDto testDto = mapper.toTestResultDto(testGen);
        assertNotNull(testDto);
        assertTrue(testDto.zeroMockVerified());
    }
}

