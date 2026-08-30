package com.aijobagent.service;

import com.aijobagent.dto.CoverLetterDto;
import com.aijobagent.dto.InterviewPrepDto;
import com.aijobagent.dto.JobDto;
import com.aijobagent.entity.CoverLetterEntity;
import com.aijobagent.entity.InterviewPrepEntity;
import com.aijobagent.repository.CoverLetterRepository;
import com.aijobagent.repository.InterviewPrepRepository;
import com.aijobagent.repository.JobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AiService {
    private final OpenAiService openAi;
    private final JobRepository jobRepo;
    private final CoverLetterRepository coverRepo;
    private final InterviewPrepRepository prepRepo;
    private final MappingService mapper;
    private final ProfileService profileService;
    private final ObjectMapper om = new ObjectMapper();

    public AiService(OpenAiService openAi, JobRepository jobRepo, CoverLetterRepository coverRepo, InterviewPrepRepository prepRepo, MappingService mapper, ProfileService profileService){
        this.openAi=openAi; this.jobRepo=jobRepo; this.coverRepo=coverRepo; this.prepRepo=prepRepo; this.mapper=mapper; this.profileService=profileService;
    }

    public JobDto analyze(String jobId){
        var jobOpt = jobRepo.findById(jobId);
        if(jobOpt.isEmpty()) throw new IllegalArgumentException("Job not found");
        var jobDto = mapper.toDto(jobOpt.get());
        var profile = profileService.get().orElse(null);
        // need profile dto; if present map
        // fallback empty profile
        if(profile==null) profile = new com.aijobagent.dto.UserProfileDto("singleton","", "", "", "", "", "", "", List.of(), List.of(), List.of(), Instant.now());
        JobDto analyzed = openAi.analyzeJob(jobDto, profile);
        var entity = mapper.toEntity(analyzed);
        entity.setUpdatedAt(Instant.now());
        jobRepo.save(entity);
        return analyzed;
    }

    public CoverLetterDto coverLetter(String jobId){
        var jobOpt = jobRepo.findById(jobId);
        if(jobOpt.isEmpty()) throw new IllegalArgumentException("Job not found");
        var jobDto = mapper.toDto(jobOpt.get());
        var profile = profileService.get().orElse(new com.aijobagent.dto.UserProfileDto("singleton","Candidate","candidate@example.com","","","","", "", List.of(), List.of(), List.of(), Instant.now()));
        String content = openAi.generateCoverLetter(jobDto, profile);
        var e = new CoverLetterEntity();
        e.setId(UUID.randomUUID().toString());
        e.setJobId(jobId);
        e.setContent(content);
        e.setGeneratedAt(Instant.now());
        e.setEdited(false);
        coverRepo.save(e);
        return mapper.toDto(e);
    }

    public InterviewPrepDto interviewPrep(String jobId){
        var jobOpt = jobRepo.findById(jobId);
        if(jobOpt.isEmpty()) throw new IllegalArgumentException("Job not found");
        var jobDto = mapper.toDto(jobOpt.get());
        var list = openAi.generateInterviewPrep(jobDto);
        String json;
        try{ json = om.writeValueAsString(list);}catch(Exception ex){ json="[]"; }
        var e = new InterviewPrepEntity();
        e.setId(UUID.randomUUID().toString());
        e.setJobId(jobId);
        e.setQuestionsJson(json);
        e.setGeneratedAt(Instant.now());
        prepRepo.save(e);
        return mapper.toDto(e);
    }
}
