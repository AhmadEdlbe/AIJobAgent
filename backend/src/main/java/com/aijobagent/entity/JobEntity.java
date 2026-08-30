package com.aijobagent.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_match", columnList = "matchPercentage"),
        @Index(name = "idx_source", columnList = "source"),
        @Index(name = "idx_posted", columnList = "postedAt")
})
public class JobEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;
    private String country;

    @Enumerated(EnumType.STRING)
    private WorkMode workMode = WorkMode.REMOTE;

    @Enumerated(EnumType.STRING)
    private Seniority seniority = Seniority.MID_LEVEL;

    @Column(columnDefinition = "TEXT")
    private String techStacks; // CSV

    @Enumerated(EnumType.STRING)
    private JobSource source = JobSource.MANUAL;

    @Column(columnDefinition = "TEXT")
    private String url;

    private Integer salaryMin;
    private Integer salaryMax;
    private String currency = "USD";

    private Instant postedAt;

    @Column(columnDefinition = "TEXT")
    private String requirements; // pipe

    private boolean isFavorite = false;

    // AI fields
    private Integer matchPercentage = 0;
    @Column(columnDefinition = "TEXT")
    private String matchingSkills;
    @Column(columnDefinition = "TEXT")
    private String missingSkills;
    private String experienceFit;
    private String salaryFit;
    @Column(columnDefinition = "TEXT")
    private String whyMatches;
    @Column(columnDefinition = "TEXT")
    private String whyNotMatches;
    private Instant analyzedAt;

    @CreationTimestamp
    private Instant createdAt;

    private Instant updatedAt;

    public enum JobSource { LINKEDIN, INDEED, GLASSDOOR, WELLFOUND, REMOTE_OK, WE_WORK_REMOTELY, COMPANY_PAGE, MANUAL }
    public enum WorkMode { REMOTE, HYBRID, ONSITE }
    public enum Seniority { JUNIOR, MID_LEVEL, SENIOR, LEAD, EXECUTIVE }
    // TechStack as string names

    // getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public WorkMode getWorkMode() { return workMode; }
    public void setWorkMode(WorkMode workMode) { this.workMode = workMode; }
    public Seniority getSeniority() { return seniority; }
    public void setSeniority(Seniority seniority) { this.seniority = seniority; }
    public String getTechStacks() { return techStacks; }
    public void setTechStacks(String techStacks) { this.techStacks = techStacks; }
    public JobSource getSource() { return source; }
    public void setSource(JobSource source) { this.source = source; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Integer getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Integer salaryMin) { this.salaryMin = salaryMin; }
    public Integer getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Integer salaryMax) { this.salaryMax = salaryMax; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Instant getPostedAt() { return postedAt; }
    public void setPostedAt(Instant postedAt) { this.postedAt = postedAt; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public Integer getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(Integer matchPercentage) { this.matchPercentage = matchPercentage; }
    public String getMatchingSkills() { return matchingSkills; }
    public void setMatchingSkills(String matchingSkills) { this.matchingSkills = matchingSkills; }
    public String getMissingSkills() { return missingSkills; }
    public void setMissingSkills(String missingSkills) { this.missingSkills = missingSkills; }
    public String getExperienceFit() { return experienceFit; }
    public void setExperienceFit(String experienceFit) { this.experienceFit = experienceFit; }
    public String getSalaryFit() { return salaryFit; }
    public void setSalaryFit(String salaryFit) { this.salaryFit = salaryFit; }
    public String getWhyMatches() { return whyMatches; }
    public void setWhyMatches(String whyMatches) { this.whyMatches = whyMatches; }
    public String getWhyNotMatches() { return whyNotMatches; }
    public void setWhyNotMatches(String whyNotMatches) { this.whyNotMatches = whyNotMatches; }
    public Instant getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(Instant analyzedAt) { this.analyzedAt = analyzedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
