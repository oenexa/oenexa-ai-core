package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "multimodal_attachments")
public class MultimodalAttachmentEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "goal_id", length = 36)
    private String goalId;

    @Column(name = "attachment_type", nullable = false, length = 50)
    private String attachmentType;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "base64_data_or_url", columnDefinition = "TEXT", nullable = false)
    private String base64DataOrUrl;

    @Column(name = "extracted_text_or_transcript", columnDefinition = "TEXT")
    private String extractedTextOrTranscript;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt = Instant.now();

    public MultimodalAttachmentEntity() {}

    public MultimodalAttachmentEntity(String id, String goalId, String attachmentType, String fileName, String mimeType, String base64DataOrUrl, String extractedTextOrTranscript) {
        this.id = id;
        this.goalId = goalId;
        this.attachmentType = attachmentType;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.base64DataOrUrl = base64DataOrUrl;
        this.extractedTextOrTranscript = extractedTextOrTranscript;
        this.uploadedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }
    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public String getBase64DataOrUrl() { return base64DataOrUrl; }
    public void setBase64DataOrUrl(String base64DataOrUrl) { this.base64DataOrUrl = base64DataOrUrl; }
    public String getExtractedTextOrTranscript() { return extractedTextOrTranscript; }
    public void setExtractedTextOrTranscript(String extractedTextOrTranscript) { this.extractedTextOrTranscript = extractedTextOrTranscript; }
    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}
