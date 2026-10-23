package com.oenexa.application.service.orchestration;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.chat.ChatDomain.*;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Intelligent Multi-Model Router dynamically routing software engineering
 * tasks across Claude 3.5 Sonnet, GPT-4o, Gemini 2.5 Flash, DeepSeek V3,
 * Mistral, Llama 3, and OENEXA Custom models.
 */
@Service
public class MultiModelRouterService {

    public TaskType determineTaskType(String message) {
        if (message == null || message.isBlank()) {
            return TaskType.FAST_CHAT;
        }
        String lower = message.toLowerCase();
        if (lower.contains("prd") || lower.contains("user story") || lower.contains("user stories") || lower.contains("product requirements") || lower.contains("acceptance criteria")) {
            return TaskType.PRODUCT_MANAGEMENT;
        } else if (lower.contains("image") || lower.contains("audio") || lower.contains("voice") || lower.contains("multimodal") || lower.contains("diagram upload") || lower.contains("screenshot")) {
            return TaskType.MULTIMODAL_ANALYSIS;
        } else if (lower.contains("architecture") || lower.contains("c4") || lower.contains("system context") || lower.contains("blueprint")) {
            return TaskType.ARCHITECTURE;
        } else if (lower.contains("security") || lower.contains("sast") || lower.contains("cve") || lower.contains("vulnerability") || lower.contains("owasp")) {
            return TaskType.SECURITY_AUDIT;
        } else if (lower.contains("bug") || lower.contains("fix") || lower.contains("npe") || lower.contains("stack trace") || lower.contains("error")) {
            return TaskType.BUG_FIX;
        } else if (lower.contains("repository") || lower.contains("repo") || lower.contains("codebase") || lower.contains("index") || lower.contains("knowledge map")) {
            return TaskType.REPO_ANALYSIS;
        } else if (lower.contains("write") || lower.contains("code") || lower.contains("implement") || lower.contains("api") || lower.contains("refactor") || lower.contains("controller")) {
            return TaskType.CODING;
        } else if (lower.contains("why") || lower.contains("explain") || lower.contains("compare") || lower.contains("tradeoff") || lower.contains("reason")) {
            return TaskType.REASONING;
        }
        return TaskType.FAST_CHAT;
    }

    public ModelRoutingDecision routeTask(TaskType taskType, String explicitPreference) {
        Objects.requireNonNull(taskType, "taskType must not be null");

        // Honor explicit user preference if supplied and known
        if (explicitPreference != null && !explicitPreference.isBlank() && !explicitPreference.equalsIgnoreCase("auto")) {
            String p = explicitPreference.toLowerCase();
            if (p.contains("ollama") || p.contains("local")) {
                return new ModelRoutingDecision(ModelProvider.LOCAL_OLLAMA, "llama3.3:latest", taskType, "User explicitly selected local air-gapped Ollama inference.");
            } else if (p.contains("private")) {
                return new ModelRoutingDecision(ModelProvider.LOCAL_PRIVATE, "deepseek-r1:14b", taskType, "User explicitly selected local air-gapped private model.");
            } else if (p.contains("claude")) {
                return new ModelRoutingDecision(ModelProvider.ANTHROPIC_CLAUDE, "claude-3-5-sonnet", taskType, "User explicitly selected Anthropic Claude.");
            } else if (p.contains("gpt") || p.contains("openai")) {
                return new ModelRoutingDecision(ModelProvider.OPENAI_GPT, "gpt-4o", taskType, "User explicitly selected OpenAI GPT-4o.");
            } else if (p.contains("deepseek")) {
                return new ModelRoutingDecision(ModelProvider.DEEPSEEK, "deepseek-v3", taskType, "User explicitly selected DeepSeek V3.");
            } else if (p.contains("mistral")) {
                return new ModelRoutingDecision(ModelProvider.MISTRAL, "mistral-large", taskType, "User explicitly selected Mistral Large.");
            } else if (p.contains("llama")) {
                return new ModelRoutingDecision(ModelProvider.LLAMA, "llama-3.3-70b", taskType, "User explicitly selected Meta Llama 3.3.");
            } else if (p.contains("oenexa") || p.contains("custom")) {
                return new ModelRoutingDecision(ModelProvider.OENEXA_CUSTOM, "oenexa-expert-code-v1", taskType, "User explicitly selected OENEXA Custom Model.");
            } else if (p.contains("gemini")) {
                return new ModelRoutingDecision(ModelProvider.GEMINI, "gemini-2.5-flash", taskType, "User explicitly selected Google Gemini.");
            }
        }

        // Autonomous optimal task-based routing
        return switch (taskType) {
            case PRODUCT_MANAGEMENT -> new ModelRoutingDecision(
                    ModelProvider.ANTHROPIC_CLAUDE,
                    "claude-3-5-sonnet",
                    TaskType.PRODUCT_MANAGEMENT,
                    "Selected Anthropic Claude 3.5 Sonnet for structured Product Requirements Document and INVEST user story generation."
            );
            case MULTIMODAL_ANALYSIS -> new ModelRoutingDecision(
                    ModelProvider.GEMINI,
                    "gemini-2.5-flash",
                    TaskType.MULTIMODAL_ANALYSIS,
                    "Selected Google Gemini 2.5 Flash for high-fidelity native multimodal image, audio, and document reasoning."
            );
            case CODING -> new ModelRoutingDecision(
                    ModelProvider.ANTHROPIC_CLAUDE,
                    "claude-3-5-sonnet",
                    TaskType.CODING,
                    "Selected Anthropic Claude 3.5 Sonnet for superior code generation, refactoring, and type-safety."
            );
            case REASONING -> new ModelRoutingDecision(
                    ModelProvider.ANTHROPIC_CLAUDE,
                    "claude-3-5-sonnet",
                    TaskType.REASONING,
                    "Selected Anthropic Claude 3.5 Sonnet for deep architectural reasoning and chain-of-thought analysis."
            );
            case FAST_CHAT -> new ModelRoutingDecision(
                    ModelProvider.DEEPSEEK,
                    "deepseek-v3",
                    TaskType.FAST_CHAT,
                    "Selected DeepSeek V3 for ultra-fast, low-latency conversational responses."
            );
            case REPO_ANALYSIS -> new ModelRoutingDecision(
                    ModelProvider.ANTHROPIC_CLAUDE,
                    "claude-3-5-sonnet",
                    TaskType.REPO_ANALYSIS,
                    "Selected Anthropic Claude 3.5 Sonnet for large 200k context window codebase analysis."
            );
            case BUG_FIX -> new ModelRoutingDecision(
                    ModelProvider.OPENAI_GPT,
                    "gpt-4o",
                    TaskType.BUG_FIX,
                    "Selected OpenAI GPT-4o for precise root cause diagnosis and patch synthesis."
            );
            case SECURITY_AUDIT -> new ModelRoutingDecision(
                    ModelProvider.OENEXA_CUSTOM,
                    "oenexa-security-sast-v1",
                    TaskType.SECURITY_AUDIT,
                    "Selected OENEXA Security Specialist model for SAST and OWASP vulnerability auditing."
            );
            case ARCHITECTURE -> new ModelRoutingDecision(
                    ModelProvider.GEMINI,
                    "gemini-2.5-flash",
                    TaskType.ARCHITECTURE,
                    "Selected Google Gemini 2.5 Flash for multimodal diagrams and system architecture generation."
            );
        };
    }

    public ModelRoutingDecision routeMessage(String message, String explicitPreference) {
        TaskType taskType = determineTaskType(message);
        return routeTask(taskType, explicitPreference);
    }
}


