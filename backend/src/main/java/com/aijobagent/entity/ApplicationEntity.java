package com.aijobagent.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "applications", indexes = {
        @Index(name = "idx_job_id", columnList = "jobId"),
        @Index(name = "idx_status", columnList = "status")
})
public class ApplicationEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String jobId;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.SAVED;

    private Instant appliedAt;
    private Instant interviewDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String coverLetterId;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public enum ApplicationStatus {
        SAVED, PENDING_APPROVAL, APPLIED, INTERVIEW_SCHEDULED, INTERVIEW_COMPLETED, REJECTED, OFFER_RECEIVED, WITHDRAWN
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public Instant getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Instant appliedAt) { this.appliedAt = appliedAt; }
    public Instant getInterviewDate() { return interviewDate; }
    public void setInterviewDate(Instant interviewDate) { this.interviewDate = interviewDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCoverLetterId() { return coverLetterId; }
    public void setCoverLetterId(String coverLetterId) { this.coverLetterId = coverLetterId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
