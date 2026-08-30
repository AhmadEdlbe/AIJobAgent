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
public class WeWorkRemotelyProvider implements JobProvider {
    private static final Logger log = LoggerFactory.getLogger(WeWorkRemotelyProvider.class);
    @Override public String sourceName(){ return "WE_WORK_REMOTELY"; }

    @Override
    public List<JobDto> fetch(ScanRequest req) throws Exception {
        try{
            String category = "programming";
            if(req!=null && req.query()!=null && req.query().toLowerCase().contains("android")) category="programming";
            Document doc = Jsoup.connect("https://weworkremotely.com/categories/"+category+"-remote-jobs").userAgent("AIJobAgent/1.0").timeout(8000).get();
            List<JobDto> out = new ArrayList<>();
            for(Element sec: doc.select("section.jobs li")){
                Element a = sec.selectFirst("a");
                if(a==null) continue;
                String title = a.selectFirst("span.title")!=null? a.selectFirst("span.title").text(): a.text();
                String company = a.selectFirst("span.company")!=null? a.selectFirst("span.company").text(): "Unknown";
                if(title.isBlank()) continue;
                String href = a.attr("href");
                String url = href.startsWith("http")? href: "https://weworkremotely.com"+href;
                String q = req!=null? req.query():null;
                if(q!=null && !q.isBlank() && !title.toLowerCase().contains(q.toLowerCase())) continue;
                out.add(new JobDto(UUID.randomUUID().toString(), title, company, "Remote job at "+company+" for "+title, "Remote","Remote","REMOTE","MID_LEVEL", List.of("JAVA","SPRING_BOOT"), sourceName(), url, null,null,"USD", Instant.now(), List.of("Java","Spring Boot"), false,0,List.of(),List.of(),"","","","",null));
                if(out.size()>=8) break;
            }
            return out;
        }catch(Exception e){
            log.warn("WWR failed {}", e.getMessage());
            return List.of();
        }
    }
}
