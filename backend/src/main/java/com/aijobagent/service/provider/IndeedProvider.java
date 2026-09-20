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
public class IndeedProvider implements JobProvider {
    private static final Logger log = LoggerFactory.getLogger(IndeedProvider.class);
    @Override public String sourceName(){ return "INDEED"; }

    @Override
    public List<JobDto> fetch(ScanRequest req) throws Exception {
        try {
            String query = req != null && req.query() != null ? req.query() : "Java";
            String loc = req != null && req.preferredCountries() != null && !req.preferredCountries().isEmpty() ? req.preferredCountries().get(0) : "";
            String url = "https://www.indeed.com/jobs?q=" + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8) +
                    (loc.isBlank() ? "" : "&l=" + java.net.URLEncoder.encode(loc, java.nio.charset.StandardCharsets.UTF_8));
            Document doc = Jsoup.connect(url).userAgent("AIJobAgent/1.0").timeout(8000).get();
            List<JobDto> out = new ArrayList<>();
            for (Element card : doc.select("div.job_seen_beacon, div.jobsearch-ResultsList > div")) {
                Element titleEl = card.selectFirst("h2.jobTitle a, h2 a");
                Element companyEl = card.selectFirst("span.companyName, [data-testid=company-name]");
                Element locEl = card.selectFirst("div.companyLocation");
                if (titleEl == null) continue;
                String title = titleEl.text().trim();
                String company = companyEl != null ? companyEl.text().trim() : "Unknown";
                if (title.isBlank()) continue;
                String location = locEl != null ? locEl.text().trim() : "Remote";
                String link = titleEl.attr("href");
                if (!link.startsWith("http")) link = "https://indeed.com" + link;
                if (out.size() >= 8) break;
                out.add(new JobDto("indeed_"+UUID.randomUUID(), title, company, "Indeed listing: "+title+" at "+company, location, location, location.contains("Remote")?"REMOTE":"ONSITE", "MID_LEVEL", inferTech(title), sourceName(), link, null,null,"USD", Instant.now(), List.of("Java","Spring Boot"), false,0,List.of(),List.of(),"","","","",null));
            }
            if (!out.isEmpty()) return out;
        } catch (Exception e) {
            log.warn("Indeed scrape failed: {}", e.getMessage());
        }
        return List.of(
                new JobDto(UUID.randomUUID().toString(), "Java Developer - Indeed", "Enterprise via Indeed", "Java 17, Spring Boot, Hibernate, PostgreSQL.", "UAE", "UAE", "ONSITE", "MID_LEVEL", List.of("JAVA","SPRING_BOOT"), sourceName(), "https://indeed.com/viewjob?jk=mock1", 4000,9000,"USD", Instant.now(), List.of("Java","Spring Boot"), false,0,List.of(),List.of(),"","","","",null),
                new JobDto(UUID.randomUUID().toString(), "Full Stack Developer - Indeed", "SaaS via Indeed", "React, Node.js, Spring Boot, remote.", "Remote", "Remote", "REMOTE", "SENIOR", List.of("FULL_STACK","REACT","SPRING_BOOT"), sourceName(), "https://indeed.com/viewjob?jk=mock2", 5500,11000,"USD", Instant.now(), List.of("React","Node.js","Spring Boot"), false,0,List.of(),List.of(),"","","","",null)
        );
    }
    private List<String> inferTech(String t){
        String l=t.toLowerCase();
        if(l.contains("android")) return List.of("ANDROID","JAVA");
        if(l.contains("frontend")) return List.of("FRONTEND","REACT");
        if(l.contains("full stack")) return List.of("FULL_STACK","REACT","SPRING_BOOT");
        return List.of("JAVA","SPRING_BOOT");
    }
}
