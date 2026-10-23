package com.oenexa.domain;

import java.util.List;
import java.util.Objects;

/**
 * Enterprise Domain Records for OpenAI Wire-Compatible Multimodal, Audio, and Files Specification.
 */
public final class OpenAiMediaDomain {

    private OpenAiMediaDomain() {}

    public record OpenAiTranscriptionResponse(
            String text,
            String language,
            double duration,
            String task
    ) {
        public OpenAiTranscriptionResponse {
            Objects.requireNonNull(text, "text must not be null");
            language = (language != null) ? language : "en";
            task = (task != null) ? task : "transcribe";
        }
    }

    public record OpenAiImageData(
            String b64Json,
            String url,
            String revisedPrompt
    ) {}

    public record OpenAiImageResponse(
            long created,
            List<OpenAiImageData> data
    ) {
        public OpenAiImageResponse {
            data = (data != null) ? List.copyOf(data) : List.of();
        }
    }

    public record OpenAiFileObject(
            String id,
            long bytes,
            long createdAt,
            String filename,
            String purpose
    ) {
        public OpenAiFileObject {
            Objects.requireNonNull(id, "id must not be null");
            Objects.requireNonNull(filename, "filename must not be null");
            purpose = (purpose != null) ? purpose : "assistants";
        }
    }

    public record OpenAiFileList(
            String object,
            List<OpenAiFileObject> data
    ) {
        public OpenAiFileList {
            object = (object != null) ? object : "list";
            data = (data != null) ? List.copyOf(data) : List.of();
        }
    }
}
