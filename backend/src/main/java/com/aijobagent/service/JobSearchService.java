package com.aijobagent.service;

import com.aijobagent.dto.JobDto;
import com.aijobagent.dto.ScanRequest;
import com.aijobagent.dto.UserProfileDto;
import com.aijobagent.entity.JobEntity;
import com.aijobagent.repository.JobRepository;
import com.aijobagent.service.provider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class JobSearchService {
    private static final Logger log = LoggerFactory.getLogger(JobSearchService.class);
    private final JobRepository jobRepository;
    private final OpenAiService openAiService;
    private final MappingService mapper;
    private final List<JobProvider> providers;
    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    public JobSearchService(JobRepository jobRepository, OpenAiService openAiService, MappingService mapper,
                            RemoteOkProvider remoteOkProvider,
                            WeWorkRemotelyProvider wwrProvider,
                            WellfoundProvider wellfoundProvider,
                            MockProvider mockProvider,
                            LinkedInProvider linkedInProvider,
                            IndeedProvider indeedProvider,
                            GlassdoorProvider glassdoorProvider,
                            CompanyCareerPageProvider companyProvider) {
        this.jobRepository = jobRepository;
        this.openAiService = openAiService;
        this.mapper = mapper;
        this.providers = List.of(remoteOkProvider, wwrProvider, wellfoundProvider, mockProvider, linkedInProvider, indeedProvider, glassdoorProvider, companyProvider);
    }

    public List<JobDto> scan(ScanRequest req, UserProfileDto profile) {
        List<CompletableFuture<List<JobDto>>> futures = providers.stream()
                .map(p -> CompletableFuture.supplyAsync(() -> {
                    try { return p.fetch(req); } catch (Exception e){ log.warn("Provider {} failed: {}", p.getClass().getSimpleName(), e.getMessage()); return List.<JobDto>of(); }
                }, executor))
                .toList();
        List<JobDto> all = new ArrayList<>();
        for(var f: futures){
            try{ all.addAll(f.get()); }catch(Exception e){ log.warn("Future failed {}", e.getMessage()); }
        }
        // Deduplicate by url/title+company
        var dedup = all.stream().collect(java.util.stream.Collectors.toMap(
                j -> (j.url()!=null && !j.url().isBlank()? j.url(): j.title()+j.company()),
                j->j,
                (a,b)->a
        )).values().stream().toList();

        // Analyze each and persist
        List<JobDto> analyzed = new ArrayList<>();
        for(JobDto j: dedup){
            JobDto enriched = openAiService.analyzeJob(j, profile);
            // ensure id
            if(enriched.id()==null || enriched.id().isBlank()){
                enriched = new JobDto(UUID.randomUUID().toString(), enriched.title(), enriched.company(), enriched.description(), enriched.location(), enriched.country(), enriched.workMode(), enriched.seniority(), enriched.techStacks(), enriched.source(), enriched.url(), enriched.salaryMin(), enriched.salaryMax(), enriched.currency(), enriched.postedAt()!=null?enriched.postedAt():Instant.now(), enriched.requirements(), enriched.isFavorite(), enriched.matchPercentage(), enriched.matchingSkills(), enriched.missingSkills(), enriched.experienceFit(), enriched.salaryFit(), enriched.whyMatches(), enriched.whyNotMatches(), Instant.now());
            }
            analyzed.add(enriched);
            try{
                JobEntity e = mapper.toEntity(enriched);
                e.setCreatedAt(Instant.now());
                e.setUpdatedAt(Instant.now());
                jobRepository.save(e);
            }catch(Exception ex){ log.warn("Save failed {}", ex.getMessage()); }
        }
        analyzed.sort((a,b)-> Integer.compare(b.matchPercentage(), a.matchPercentage()));
        return analyzed;
    }
}
