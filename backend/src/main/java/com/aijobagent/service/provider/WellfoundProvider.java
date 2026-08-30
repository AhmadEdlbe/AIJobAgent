package com.aijobagent.service.provider;

import com.aijobagent.dto.JobDto;
import com.aijobagent.dto.ScanRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Wellfound (AngelList) requires API key / scraping with auth.
 * For POC we return mock wellfound jobs but with source=WELLFOUND to indicate aggregation.
 * Replace with real GraphQL call when API key available.
 */
@Component
public class WellfoundProvider implements JobProvider {
    private static final Logger log = LoggerFactory.getLogger(WellfoundProvider.class);
    @Override public String sourceName(){ return "WELLFOUND"; }

    @Override
    public List<JobDto> fetch(ScanRequest req) throws Exception {
        // TODO: implement real Wellfound API: https://wellfound.com/graphql
        // For now return 2 mock startup jobs
        return List.of(
                new JobDto(UUID.randomUUID().toString(), "Full Stack Engineer (Wellfound)", "StartupXYZ", "Join fast-growing startup building fintech. Stack: React, Node.js, Spring Boot.", "Remote","Remote","REMOTE","MID_LEVEL", List.of("FULL_STACK","REACT","SPRING_BOOT"), sourceName(), "https://wellfound.com/jobs", 5000,12000,"USD", Instant.now(), List.of("React","Node.js","Spring Boot"), false,0,List.of(),List.of(),"","","","",null),
                new JobDto(UUID.randomUUID().toString(), "Backend Engineer - Wellfound", "ScaleUp Inc", "Backend heavy role with Java, Spring Boot, PostgreSQL, microservices.", "Berlin","Germany","HYBRID","SENIOR", List.of("BACKEND","JAVA","SPRING_BOOT"), sourceName(), "https://wellfound.com/jobs2", 6000,13000,"USD", Instant.now(), List.of("Java","Spring Boot","PostgreSQL"), false,0,List.of(),List.of(),"","","","",null)
        );
    }
}
