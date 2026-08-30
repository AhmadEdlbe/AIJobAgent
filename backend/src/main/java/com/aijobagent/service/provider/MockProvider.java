package com.aijobagent.service.provider;

import com.aijobagent.dto.JobDto;
import com.aijobagent.dto.ScanRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class MockProvider implements JobProvider {
    @Override public String sourceName(){ return "MANUAL"; }

    @Override
    public List<JobDto> fetch(ScanRequest req) {
        List<String> titles = List.of("Senior Android Developer", "Backend Engineer (Spring Boot)", "Full Stack Developer", "Frontend React Developer", "Java Developer", "Node.js Engineer");
        List<String> companies = List.of("Google", "Meta", "Amazon", "Booking.com", "Careem", "Noon", "Microsoft", "Spotify");
        List<String> countries = req!=null && req.preferredCountries()!=null && !req.preferredCountries().isEmpty()? req.preferredCountries(): List.of("UAE","Germany","USA","Remote","UK");
        List<String> queryTitles = req!=null && req.preferredTitles()!=null? req.preferredTitles(): List.of();
        var list = new ArrayList<JobDto>();
        for(int i=0;i<8;i++){
            String title = queryTitles.isEmpty()? titles.get((int)(Math.random()*titles.size())): queryTitles.get((int)(Math.random()*queryTitles.size()));
            String company = companies.get((int)(Math.random()*companies.size()));
            String country = countries.get((int)(Math.random()*countries.size()));
            String workMode = List.of("REMOTE","HYBRID","ONSITE").get((int)(Math.random()*3));
            String seniority = List.of("JUNIOR","MID_LEVEL","SENIOR").get((int)(Math.random()*3));
            List<String> tech = switch (title){
                case String s when s.contains("Android") -> List.of("ANDROID","JAVA","SPRING_BOOT");
                case String s when s.contains("Backend") -> List.of("BACKEND","JAVA","SPRING_BOOT");
                case String s when s.contains("Frontend") -> List.of("FRONTEND","REACT","NODE_JS");
                case String s when s.contains("Full Stack") -> List.of("FULL_STACK","REACT","SPRING_BOOT");
                default -> List.of("JAVA","SPRING_BOOT");
            };
            list.add(new JobDto(
                    UUID.randomUUID().toString(), title, company,
                    "We are looking for "+title+" to join "+company+". Must have experience in Java, Kotlin, Spring Boot. Location: "+country+" competitive salary.",
                    country, country, workMode, seniority, tech, sourceName(),
                    "https://example.com/jobs/"+UUID.randomUUID(),
                    3000 + (int)(Math.random()*5000), 8000 + (int)(Math.random()*10000), "USD",
                    Instant.now().minusSeconds((long)(Math.random()*7*24*3600)),
                    List.of("Kotlin","Java","Spring Boot","Android"),
                    false, 0, List.of(), List.of(), "", "", "", "", null
            ));
        }
        return list;
    }
}
