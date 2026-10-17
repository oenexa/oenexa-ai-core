package com.oenexa.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "project_knowledge_chunks")
public class ProjectKnowledgeChunkEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "repository_id", nullable = false, length = 100)
    private String repositoryId;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(nullable = false, length = 50)
    private String language;

    @Column(name = "symbol_type", nullable = false, length = 50)
    private String symbolType;

    @Column(name = "symbol_name", length = 255)
    private String symbolName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "embedding_vector_json", columnDefinition = "TEXT", nullable = false)
    private String embeddingVectorJson;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public ProjectKnowledgeChunkEntity() {}

    public ProjectKnowledgeChunkEntity(String id, String repositoryId, String filePath, String language,
                                       String symbolType, String symbolName, String content,
                                       String embeddingVectorJson, int chunkIndex) {
        this.id = id;
        this.repositoryId = repositoryId;
        this.filePath = filePath;
        this.language = language;
        this.symbolType = symbolType;
        this.symbolName = symbolName;
        this.content = content;
        this.embeddingVectorJson = embeddingVectorJson != null ? embeddingVectorJson : "[]";
        this.chunkIndex = chunkIndex;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getSymbolType() { return symbolType; }
    public void setSymbolType(String symbolType) { this.symbolType = symbolType; }
    public String getSymbolName() { return symbolName; }
    public void setSymbolName(String symbolName) { this.symbolName = symbolName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getEmbeddingVectorJson() { return embeddingVectorJson; }
    public void setEmbeddingVectorJson(String embeddingVectorJson) { this.embeddingVectorJson = embeddingVectorJson; }
    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
