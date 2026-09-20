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

@Component
public class GlassdoorProvider implements JobProvider {
    private static final Logger log = LoggerFactory.getLogger(GlassdoorProvider.class);
    @Override public String sourceName(){ return "GLASSDOOR"; }

    @Override
    public List<JobDto> fetch(ScanRequest req) throws Exception {
        try {
            String query = req != null && req.query() != null ? req.query() : "Spring Boot";
            String url = "https://www.glassdoor.com/Job/jobs.htm?sc.keyword=" + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
            Document doc = Jsoup.connect(url).userAgent("AIJobAgent/1.0").timeout(8000).header("Accept","text/html").get();
            List<JobDto> out = new ArrayList<>();
            for (Element card : doc.select("li[data-test=jobListing], li.react-job-listing")) {
                Element titleEl = card.selectFirst("a[data-test=job-title], a.jobLink");
                Element companyEl = card.selectFirst("div[class*=Employer], span[class*=companyName]");
                Element locEl = card.selectFirst("div[class*=location]");
                if (titleEl == null) continue;
                String title = titleEl.text().trim();
                String company = companyEl != null ? companyEl.text().trim() : "Unknown";
                if (title.isBlank()) continue;
                String loc = locEl != null ? locEl.text().trim() : "Remote";
                String link = titleEl.attr("href");
                if (!link.startsWith("http")) link = "https://glassdoor.com" + link;
                if (out.size() >= 8) break;
                out.add(new JobDto("glassdoor_"+UUID.randomUUID(), title, company, "Glassdoor: "+title+" at "+company, loc, loc, "HYBRID","MID_LEVEL", inferTech(title), sourceName(), link, null,null,"USD", Instant.now(), List.of("Java","Spring Boot"), false,0,List.of(),List.of(),"","","","",null));
            }
            if (!out.isEmpty()) return out;
        } catch (Exception e) {
            log.warn("Glassdoor scrape failed: {}", e.getMessage());
        }
        return List.of(
                new JobDto(UUID.randomUUID().toString(), "Backend Engineer - Glassdoor", "Glassdoor Corp", "Glassdoor aggregation: Spring Boot, microservices.", "USA", "USA", "ONSITE", "SENIOR", List.of("BACKEND","JAVA","SPRING_BOOT"), sourceName(), "https://glassdoor.com/Job/mock1", 7000,13000,"USD", Instant.now(), List.of("Java","Spring Boot","Kafka"), false,0,List.of(),List.of(),"","","","",null),
                new JobDto(UUID.randomUUID().toString(), "React Developer - Glassdoor", "Design via Glassdoor", "Frontend React, TypeScript.", "UK", "UK", "REMOTE", "MID_LEVEL", List.of("FRONTEND","REACT"), sourceName(), "https://glassdoor.com/Job/mock2", 4500,9500,"USD", Instant.now(), List.of("React","TypeScript"), false,0,List.of(),List.of(),"","","","",null)
        );
    }
    private List<String> inferTech(String t){
        String l=t.toLowerCase();
        if(l.contains("android")) return List.of("ANDROID");
        if(l.contains("react")) return List.of("REACT","FRONTEND");
        return List.of("JAVA","SPRING_BOOT");
    }
}
