package com.aijobagent.service;

import com.aijobagent.dto.ApplicationDto;
import com.aijobagent.dto.DashboardStatsDto;
import com.aijobagent.entity.ApplicationEntity;
import com.aijobagent.repository.ApplicationRepository;
import com.aijobagent.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApplicationService {
    private final ApplicationRepository appRepo;
    private final JobRepository jobRepo;
    private final MappingService mapper;

    public ApplicationService(ApplicationRepository appRepo, JobRepository jobRepo, MappingService mapper){
        this.appRepo = appRepo; this.jobRepo = jobRepo; this.mapper = mapper;
    }

    public List<ApplicationDto> getAll(){
        return appRepo.findAll().stream().map(e -> {
            var job = jobRepo.findById(e.getJobId()).map(mapper::toDto).orElse(null);
            return mapper.toDto(e, job);
        }).toList();
    }
    public List<ApplicationDto> getByStatus(String status){
        try{
            var st = ApplicationEntity.ApplicationStatus.valueOf(status);
            return appRepo.findByStatus(st).stream().map(e-> mapper.toDto(e, jobRepo.findById(e.getJobId()).map(mapper::toDto).orElse(null))).toList();
        }catch(Exception ex){ return List.of(); }
    }
    public Optional<ApplicationDto> getById(String id){
        return appRepo.findById(id).map(e-> mapper.toDto(e, jobRepo.findById(e.getJobId()).map(mapper::toDto).orElse(null)));
    }
    public ApplicationDto create(String jobId, String status){
        var e = new ApplicationEntity();
        e.setId(UUID.randomUUID().toString());
        e.setJobId(jobId);
        try{ e.setStatus(ApplicationEntity.ApplicationStatus.valueOf(status)); }catch(Exception ex){ e.setStatus(ApplicationEntity.ApplicationStatus.SAVED); }
        if(e.getStatus()== ApplicationEntity.ApplicationStatus.APPLIED) e.setAppliedAt(Instant.now());
        e.setCreatedAt(Instant.now()); e.setUpdatedAt(Instant.now());
        appRepo.save(e);
        return mapper.toDto(e, jobRepo.findById(jobId).map(mapper::toDto).orElse(null));
    }
    public Optional<ApplicationDto> updateStatus(String id, String status){
        var opt = appRepo.findById(id);
        if(opt.isEmpty()) return Optional.empty();
        var e = opt.get();
        try{ e.setStatus(ApplicationEntity.ApplicationStatus.valueOf(status)); }catch(Exception ex){ return Optional.empty(); }
        if(e.getStatus()== ApplicationEntity.ApplicationStatus.APPLIED && e.getAppliedAt()==null) e.setAppliedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        appRepo.save(e);
        return Optional.of(mapper.toDto(e, jobRepo.findById(e.getJobId()).map(mapper::toDto).orElse(null)));
    }
    public void delete(String id){ appRepo.deleteById(id); }

    public DashboardStatsDto stats(){
        long total = jobRepo.count();
        long high = jobRepo.countByMatchPercentageGreaterThanEqual(75);
        long applied = appRepo.countByStatus(ApplicationEntity.ApplicationStatus.APPLIED);
        long interviews = appRepo.countByStatusIn(List.of(ApplicationEntity.ApplicationStatus.INTERVIEW_SCHEDULED, ApplicationEntity.ApplicationStatus.INTERVIEW_COMPLETED));
        long offers = appRepo.countByStatus(ApplicationEntity.ApplicationStatus.OFFER_RECEIVED);
        long pending = appRepo.countByStatus(ApplicationEntity.ApplicationStatus.PENDING_APPROVAL);
        return new DashboardStatsDto(total, high, applied, interviews, offers, pending);
    }
}
