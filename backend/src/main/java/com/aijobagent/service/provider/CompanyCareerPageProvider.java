package com.aijobagent.service.provider;

import com.aijobagent.dto.JobDto;
import com.aijobagent.dto.ScanRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Company career pages aggregator.
 * Configurable via application.yml app.career-pages.urls (comma separated).
 * For MVP, uses Greenhouse/Lever mock + configurable URLs via Jsoup.
 * If no URLs configured, returns curated mock company pages.
 */
@Component
public class CompanyCareerPageProvider implements JobProvider {
    private static final Logger log = LoggerFactory.getLogger(CompanyCareerPageProvider.class);
    @Value("${app.career-pages.urls:}")
    private String urls;

    @Override public String sourceName(){ return "COMPANY_PAGE"; }

    @Override
    public List<JobDto> fetch(ScanRequest req) throws Exception {
        List<JobDto> out = new ArrayList<>();
        // If user configured URLs, try scrape each (Greenhouse/Lever JSON)
        if (urls != null && !urls.isBlank()) {
            for (String raw : urls.split(",")) {
                String u = raw.trim();
                if (u.isBlank()) continue;
                try {
                    // Try Greenhouse API pattern: https://boards-api.greenhouse.io/v1/boards/<board>/jobs
                    // or Lever: https://api.lever.co/v0/postings/<company>
                    // For generic, attempt Jsoup and look for job links
                    var doc = org.jsoup.Jsoup.connect(u).userAgent("AIJobAgent/1.0").timeout(8000).get();
                    for (var el : doc.select("a[href*=/jobs], a[href*=/careers], div[data-testid=job] a")) {
                        String title = el.text().trim();
                        if (title.length() < 5 || title.length() > 80) continue;
                        String link = el.attr("abs:href");
                        out.add(new JobDto("company_"+UUID.randomUUID(), title, extractCompany(u), "Career page: "+title, "Remote", "Remote", "REMOTE","MID_LEVEL", List.of("JAVA","SPRING_BOOT"), sourceName(), link.isBlank()?u:link, null,null,"USD", Instant.now(), List.of("Java"), false,0,List.of(),List.of(),"","","","",null));
                        if (out.size() >= 5) break;
                    }
                } catch (Exception e) {
                    log.warn("Career page {} scrape failed: {}", u, e.getMessage());
                }
            }
            if (!out.isEmpty()) return out.stream().limit(8).toList();
        }
        // Fallback curated company pages (Booking, Spotify, Careem)
        return List.of(
                new JobDto(UUID.randomUUID().toString(), "Android Engineer - Booking.com Careers", "Booking.com", "Direct career page: Android, Kotlin, Compose at Booking.com (Amsterdam/Remote).", "Amsterdam", "Netherlands", "HYBRID", "SENIOR", List.of("ANDROID","JAVA"), sourceName(), "https://careers.booking.com/jobs/mock-android", 6500,12500,"EUR", Instant.now(), List.of("Kotlin","Android","Compose"), false,0,List.of(),List.of(),"","","","",null),
                new JobDto(UUID.randomUUID().toString(), "Backend Engineer - Spotify Careers", "Spotify", "Spotify direct: Java, Spring Boot, microservices, music domain.", "Stockholm", "Sweden", "HYBRID", "MID_LEVEL", List.of("BACKEND","JAVA","SPRING_BOOT"), sourceName(), "https://lifeatspotify.com/jobs/mock-backend", 6000,11500,"EUR", Instant.now(), List.of("Java","Spring Boot","Kafka"), false,0,List.of(),List.of(),"","","","",null),
                new JobDto(UUID.randomUUID().toString(), "Full Stack Engineer - Careem Careers", "Careem", "Careem careers: React, Node.js, Spring Boot, Dubai.", "Dubai", "UAE", "ONSITE", "MID_LEVEL", List.of("FULL_STACK","REACT","SPRING_BOOT"), sourceName(), "https://careers.careem.com/jobs/mock-fullstack", 5000,10000,"USD", Instant.now(), List.of("React","Spring Boot"), false,0,List.of(),List.of(),"","","","",null)
        );
    }
    private String extractCompany(String url){
        try { var h = new java.net.URL(url).getHost(); return h.replace("www.","").split("\\.")[0]; } catch(Exception e){ return "Company"; }
    }
}
