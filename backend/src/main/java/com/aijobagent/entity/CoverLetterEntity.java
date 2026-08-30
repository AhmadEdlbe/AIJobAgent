package com.aijobagent.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "cover_letters", indexes = @Index(name="idx_job_cover", columnList = "jobId"))
public class CoverLetterEntity {
    @Id
    private String id;
    @Column(nullable = false)
    private String jobId;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    private Instant generatedAt = Instant.now();
    private boolean isEdited = false;

    @CreationTimestamp
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
    public boolean isEdited() { return isEdited; }
    public void setEdited(boolean edited) { isEdited = edited; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
