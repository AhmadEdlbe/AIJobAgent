package com.aijobagent.service;

import com.aijobagent.dto.*;
import com.aijobagent.entity.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class MappingService {
    private final ObjectMapper om = new ObjectMapper();

    // Helpers
    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            // try JSON array first
            return om.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // fallback pipe/comma
            if (json.contains("|")) return List.of(json.split("\\|"));
            if (json.contains(",")) return List.of(json.split(","));
            return List.of(json);
        }
    }
    private String toJson(List<String> list) {
        if (list == null) return "[]";
        try { return om.writeValueAsString(list); } catch (Exception e){ return String.join("|", list); }
    }
    private List<String> csvToList(String csv){ if(csv==null||csv.isBlank()) return List.of(); return List.of(csv.split(",")); }
    private String listToCsv(List<String> l){ if(l==null) return ""; return String.join(",", l); }

    public UserProfileDto toDto(UserProfileEntity e){
        return new UserProfileDto(e.getId(), e.getFullName(), e.getEmail(), e.getPhoneNumber(), e.getLinkedInUrl(), e.getGitHubUrl(), e.getResumeText(), e.getResumeFileName(), fromJson(e.getPreferredCountries()), fromJson(e.getPreferredJobTitles()), fromJson(e.getSkills()), e.getUpdatedAt());
    }
    public UserProfileEntity toEntity(UserProfileDto dto){
        UserProfileEntity e = new UserProfileEntity();
        e.setId(dto.id()!=null?dto.id():"singleton");
        e.setFullName(dto.fullName()); e.setEmail(dto.email()); e.setPhoneNumber(dto.phoneNumber());
        e.setLinkedInUrl(dto.linkedInUrl()); e.setGitHubUrl(dto.gitHubUrl());
        e.setResumeText(dto.resumeText()); e.setResumeFileName(dto.resumeFileName());
        e.setPreferredCountries(toJson(dto.preferredCountries()));
        e.setPreferredJobTitles(toJson(dto.preferredJobTitles()));
        e.setSkills(toJson(dto.skills()));
        return e;
    }
    public JobDto toDto(JobEntity e){
        return new JobDto(
                e.getId(), e.getTitle(), e.getCompany(), e.getDescription(), e.getLocation(), e.getCountry(),
                e.getWorkMode()!=null?e.getWorkMode().name():null,
                e.getSeniority()!=null?e.getSeniority().name():null,
                csvToList(e.getTechStacks()),
                e.getSource()!=null?e.getSource().name():null,
                e.getUrl(), e.getSalaryMin(), e.getSalaryMax(), e.getCurrency(), e.getPostedAt(),
                e.getRequirements()!=null? List.of(e.getRequirements().split("\\|")):List.of(),
                e.isFavorite(),
                e.getMatchPercentage()!=null?e.getMatchPercentage():0,
                e.getMatchingSkills()!=null? List.of(e.getMatchingSkills().split("\\|")):List.of(),
                e.getMissingSkills()!=null? List.of(e.getMissingSkills().split("\\|")):List.of(),
                e.getExperienceFit(), e.getSalaryFit(), e.getWhyMatches(), e.getWhyNotMatches(), e.getAnalyzedAt()
        );
    }
    public JobEntity toEntity(JobDto dto){
        JobEntity e = new JobEntity();
        e.setId(dto.id()); e.setTitle(dto.title()); e.setCompany(dto.company()); e.setDescription(dto.description());
        e.setLocation(dto.location()); e.setCountry(dto.country());
        try{ e.setWorkMode(dto.workMode()!=null? JobEntity.WorkMode.valueOf(dto.workMode()): JobEntity.WorkMode.REMOTE);}catch(Exception ex){e.setWorkMode(JobEntity.WorkMode.REMOTE);}
        try{ e.setSeniority(dto.seniority()!=null? JobEntity.Seniority.valueOf(dto.seniority()): JobEntity.Seniority.MID_LEVEL);}catch(Exception ex){e.setSeniority(JobEntity.Seniority.MID_LEVEL);}
        e.setTechStacks(listToCsv(dto.techStacks()));
        try{ e.setSource(dto.source()!=null? JobEntity.JobSource.valueOf(dto.source()): JobEntity.JobSource.MANUAL);}catch(Exception ex){e.setSource(JobEntity.JobSource.MANUAL);}
        e.setUrl(dto.url()); e.setSalaryMin(dto.salaryMin()); e.setSalaryMax(dto.salaryMax()); e.setCurrency(dto.currency());
        e.setPostedAt(dto.postedAt());
        e.setRequirements(dto.requirements()!=null? String.join("|", dto.requirements()):"");
        e.setFavorite(dto.isFavorite());
        e.setMatchPercentage(dto.matchPercentage());
        e.setMatchingSkills(dto.matchingSkills()!=null? String.join("|", dto.matchingSkills()):"");
        e.setMissingSkills(dto.missingSkills()!=null? String.join("|", dto.missingSkills()):"");
        e.setExperienceFit(dto.experienceFit()); e.setSalaryFit(dto.salaryFit());
        e.setWhyMatches(dto.whyMatches()); e.setWhyNotMatches(dto.whyNotMatches());
        e.setAnalyzedAt(dto.analyzedAt());
        return e;
    }
    public ApplicationDto toDto(ApplicationEntity e, JobDto job){
        return new ApplicationDto(e.getId(), e.getJobId(), job, e.getStatus().name(), e.getAppliedAt(), e.getInterviewDate(), e.getNotes(), e.getCoverLetterId(), e.getCreatedAt(), e.getUpdatedAt());
    }
    public CoverLetterDto toDto(CoverLetterEntity e){ return new CoverLetterDto(e.getId(), e.getJobId(), e.getContent(), e.getGeneratedAt(), e.isEdited()); }
    public InterviewPrepDto toDto(InterviewPrepEntity e){
        List<InterviewPrepDto.InterviewQuestionDto> qs = List.of();
        if(e.getQuestionsJson()!=null){
            try{ qs = om.readValue(e.getQuestionsJson(), new TypeReference<List<InterviewPrepDto.InterviewQuestionDto>>() {});}catch(Exception ex){ qs = List.of(); }
        }
        return new InterviewPrepDto(e.getId(), e.getJobId(), qs, e.getGeneratedAt());
    }
}
