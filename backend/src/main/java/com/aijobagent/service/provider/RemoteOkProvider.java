package com.aijobagent.service.provider;

import com.aijobagent.dto.JobDto;
import com.aijobagent.dto.ScanRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class RemoteOkProvider implements JobProvider {
    private static final Logger log = LoggerFactory.getLogger(RemoteOkProvider.class);
    private final WebClient webClient = WebClient.builder().baseUrl("https://remoteok.com").build();
    private final ObjectMapper om = new ObjectMapper();

    @Override public String sourceName(){ return "REMOTE_OK"; }

    @Override
    public List<JobDto> fetch(ScanRequest req) throws Exception {
        try{
            String json = webClient.get().uri("/api").header("User-Agent","AIJobAgent/1.0").retrieve().bodyToMono(String.class).block();
            if(json==null) return List.of();
            JsonNode arr = om.readTree(json);
            List<JobDto> out = new ArrayList<>();
            for(JsonNode n: arr){
                if(n.has("id") && n.path("position").isTextual()){
                    String title = n.path("position").asText();
                    String company = n.path("company").asText("Unknown");
                    String desc = n.path("description").asText("");
                    try{ desc = Jsoup.parse(desc).text(); }catch(Exception e){}
                    if(desc.length()>800) desc = desc.substring(0,800);
                    String location = n.path("location").asText("Remote");
                    List<String> tags = new ArrayList<>();
                    if(n.has("tags") && n.get("tags").isArray()){
                        for(JsonNode t: n.get("tags")) tags.add(t.asText());
                    }
                    List<String> tech = tags.stream().map(String::toUpperCase).filter(t-> List.of("JAVA","SPRING","ANDROID","KOTLIN","REACT","NODE","BACKEND","FRONTEND","FULL_STACK").stream().anyMatch(k-> t.contains(k))).map(t-> t.contains("ANDROID")?"ANDROID": t.contains("REACT")?"REACT": t.contains("SPRING")?"SPRING_BOOT": t.contains("JAVA")?"JAVA":"FULL_STACK").distinct().toList();
                    if(tech.isEmpty()) tech = List.of("JAVA","SPRING_BOOT");
                    String q = req!=null? req.query():null;
                    if(q!=null && !q.isBlank() && !title.toLowerCase().contains(q.toLowerCase()) && !desc.toLowerCase().contains(q.toLowerCase())) continue;
                    out.add(new JobDto(
                            "remoteok_"+n.path("id").asText(UUID.randomUUID().toString()),
                            title, company, desc, location, location.contains("Remote")?"Remote":location,
                            "REMOTE","MID_LEVEL", tech, sourceName(),
                            n.path("url").asText("https://remoteok.com"),
                            null,null,"USD", Instant.now(),
                            tags.stream().limit(4).toList(), false,0,List.of(),List.of(),"","","","",null
                    ));
                    if(out.size()>=10) break;
                }
            }
            return out;
        }catch(Exception e){
            log.warn("RemoteOK failed {}", e.getMessage());
            return List.of();
        }
    }
}
