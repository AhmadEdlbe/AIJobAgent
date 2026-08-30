package com.aijobagent.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "user_profiles")
public class UserProfileEntity {
    @Id
    private String id = "singleton"; // single user device-bound, use fixed ID; or UUID for multi but spec single

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    private String phoneNumber;
    private String linkedInUrl;
    private String gitHubUrl;

    @Column(columnDefinition = "TEXT")
    private String resumeText;

    private String resumeFilePath;
    private String resumeFileName;

    // Store as comma-separated or JSON
    @Column(columnDefinition = "TEXT")
    private String preferredCountries; // JSON array

    @Column(columnDefinition = "TEXT")
    private String preferredJobTitles;

    @Column(columnDefinition = "TEXT")
    private String skills; // JSON

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getLinkedInUrl() { return linkedInUrl; }
    public void setLinkedInUrl(String linkedInUrl) { this.linkedInUrl = linkedInUrl; }
    public String getGitHubUrl() { return gitHubUrl; }
    public void setGitHubUrl(String gitHubUrl) { this.gitHubUrl = gitHubUrl; }
    public String getResumeText() { return resumeText; }
    public void setResumeText(String resumeText) { this.resumeText = resumeText; }
    public String getResumeFilePath() { return resumeFilePath; }
    public void setResumeFilePath(String resumeFilePath) { this.resumeFilePath = resumeFilePath; }
    public String getResumeFileName() { return resumeFileName; }
    public void setResumeFileName(String resumeFileName) { this.resumeFileName = resumeFileName; }
    public String getPreferredCountries() { return preferredCountries; }
    public void setPreferredCountries(String preferredCountries) { this.preferredCountries = preferredCountries; }
    public String getPreferredJobTitles() { return preferredJobTitles; }
    public void setPreferredJobTitles(String preferredJobTitles) { this.preferredJobTitles = preferredJobTitles; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
