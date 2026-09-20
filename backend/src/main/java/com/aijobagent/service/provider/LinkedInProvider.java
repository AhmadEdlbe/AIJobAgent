package com.aijobagent.service.provider;

import com.aijobagent.dto.JobDto;
import com.aijobagent.dto.ScanRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * LinkedIn jobs scrape via public guest endpoint.
 * Falls back to mock if blocked (LinkedIn requires auth for many regions).
 */
@Component
public class LinkedInProvider implements JobProvider {
    private static final Logger log = LoggerFactory.getLogger(LinkedInProvider.class);
    @Override public String sourceName(){ return "LINKEDIN"; }

    @Override
    public List<JobDto> fetch(ScanRequest req) throws Exception {
        try {
            String query = req != null && req.query() != null ? req.query() : "Android";
            // Public LinkedIn guest API - may be blocked, hence try/catch
            String url = "https://www.linkedin.com/jobs-guest/jobs/api/seeMoreJobPostings/search?keywords=" +
                    java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8) + "&location=Worldwide&start=0";
            Document doc = Jsoup.connect(url)
                    .userAgent("AIJobAgent/1.0 (+https://aijobagent.com)")
                    .header("Accept", "text/html")
                    .timeout(8000)
                    .get();
            List<JobDto> out = new ArrayList<>();
            for (Element card : doc.select("div.job-search-card, li")) {
                Element titleEl = card.selectFirst("h3, .base-search-card__title, .job-title");
                Element companyEl = card.selectFirst("h4, .base-search-card__subtitle, .company-name");
                Element locEl = card.selectFirst(".job-search-card__location, .job-location");
                Element linkEl = card.selectFirst("a");
                if (titleEl == null || companyEl == null) continue;
                String title = titleEl.text().trim();
                String company = companyEl.text().trim();
                if (title.isBlank()) continue;
                String loc = locEl != null ? locEl.text().trim() : "Remote";
                String link = linkEl != null ? linkEl.attr("href") : "https://linkedin.com/jobs/search?keywords=" + query;
                if (out.size() >= 8) break;
                out.add(new JobDto(
                        "linkedin_" + UUID.randomUUID(),
                        title, company,
                        "LinkedIn opportunity: " + title + " at " + company + " (" + loc + ")",
                        loc, loc.contains("Remote") ? "Remote" : loc,
                        loc.contains("Remote") ? "REMOTE" : "HYBRID",
                        "MID_LEVEL",
                        inferTech(title), sourceName(),
                        link, null, null, "USD", Instant.now(),
                        List.of("Java","Spring Boot","Kotlin"), false, 0, List.of(), List.of(), "", "", "", "", null
                ));
            }
            if (!out.isEmpty()) return out;
        } catch (Exception e) {
            log.warn("LinkedIn scrape failed, fallback mock: {}", e.getMessage());
        }
        // Fallback mock LinkedIn jobs (2)
        return List.of(
                new JobDto(UUID.randomUUID().toString(), "Senior Android Developer - LinkedIn", "LinkedIn Top Startup", "Build Android apps with Kotlin, Jetpack Compose, MVVM, Hilt. Remote friendly.", "Remote", "Remote", "REMOTE", "SENIOR", List.of("ANDROID","JAVA","SPRING_BOOT"), sourceName(), "https://linkedin.com/jobs/view/mock1", 6000, 12000, "USD", Instant.now(), List.of("Kotlin","Android","Compose"), false, 0, List.of(), List.of(), "", "", "", "", null),
                new JobDto(UUID.randomUUID().toString(), "Backend Engineer (Spring Boot) - LinkedIn", "Fintech via LinkedIn", "Spring Boot, PostgreSQL, microservices, Java 17.", "Berlin", "Germany", "HYBRID", "MID_LEVEL", List.of("BACKEND","JAVA","SPRING_BOOT"), sourceName(), "https://linkedin.com/jobs/view/mock2", 5000, 10000, "USD", Instant.now(), List.of("Java","Spring Boot","PostgreSQL"), false, 0, List.of(), List.of(), "", "", "", "", null)
        );
    }

    private List<String> inferTech(String title) {
        String t = title.toLowerCase();
        if (t.contains("android")) return List.of("ANDROID","JAVA","SPRING_BOOT");
        if (t.contains("backend")) return List.of("BACKEND","JAVA","SPRING_BOOT");
        if (t.contains("frontend")) return List.of("FRONTEND","REACT","NODE_JS");
        if (t.contains("full stack")) return List.of("FULL_STACK","REACT","SPRING_BOOT");
        return List.of("JAVA","SPRING_BOOT");
    }
}
