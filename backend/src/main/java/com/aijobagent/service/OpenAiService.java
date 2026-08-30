package com.aijobagent.service;

import com.aijobagent.dto.JobDto;
import com.aijobagent.dto.UserProfileDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {
    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);
    private final WebClient webClient;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${app.openai.api-key:}")
    private String apiKey;
    @Value("${app.openai.model:gpt-4o-mini}")
    private String model;
    @Value("${app.openai.enabled:false}")
    private boolean enabled;
    @Value("${app.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    public OpenAiService(WebClient.Builder builder, @Value("${app.openai.base-url:https://api.openai.com/v1}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public boolean isEnabled(){ return enabled && apiKey!=null && !apiKey.isBlank(); }

    // Analyze job and return match result as JobDto enriched
    public JobDto analyzeJob(JobDto job, UserProfileDto profile) {
        if(!isEnabled()){
            return heuristicAnalyze(job, profile);
        }
        try {
            String prompt = buildMatchPrompt(job, profile);
            String completion = callChat(prompt);
            return parseAnalyzeResponse(completion, job);
        } catch (Exception e){
            log.warn("OpenAI analyze failed, fallback heuristic: {}", e.getMessage());
            return heuristicAnalyze(job, profile);
        }
    }

    public String generateCoverLetter(JobDto job, UserProfileDto profile){
        if(!isEnabled()){
            return heuristicCoverLetter(job, profile);
        }
        try{
            String prompt = """
                    You are a professional cover letter writer.
                    Candidate: %s, Email: %s, Skills: %s, Resume: %s
                    Job: %s at %s (%s) Description: %s Requirements: %s
                    Write a tailored cover letter (300-400 words) highlighting matching skills %s.
                    Do NOT hallucinate experience. Keep tone professional.
                    """.formatted(profile.fullName(), profile.email(), profile.skills(), profile.resumeText(), job.title(), job.company(), job.location(), job.description(), job.requirements(), job.matchingSkills());
            return callChat(prompt);
        }catch(Exception e){
            return heuristicCoverLetter(job, profile);
        }
    }

    public List<Map<String,String>> generateInterviewPrep(JobDto job){
        if(!isEnabled()){
            return heuristicInterview(job);
        }
        try{
            String prompt = """
                    Generate 7 interview Q&A for job: %s at %s. Requirements: %s Missing skills: %s
                    Categories: TECHNICAL, SYSTEM_DESIGN, HR, BEHAVIORAL
                    Return JSON array: [{"question":"...","category":"TECHNICAL","suggestedAnswer":"..."}]
                    """.formatted(job.title(), job.company(), job.requirements(), job.missingSkills());
            String completion = callChat(prompt);
            JsonNode node = om.readTree(extractJson(completion));
            List<Map<String,String>> list = om.convertValue(node, List.class);
            return list;
        }catch(Exception e){
            return heuristicInterview(job).stream().map(m-> Map.of("question", m.get("question"), "category", m.get("category"), "suggestedAnswer", m.get("suggestedAnswer"))).toList();
        }
    }

    private String callChat(String prompt){
        Map<String,Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of("role","user","content", prompt)),
                "temperature", 0.7
        );
        return webClient.post().uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(n -> n.path("choices").get(0).path("message").path("content").asText())
                .block(Duration.ofSeconds(30));
    }

    private String buildMatchPrompt(JobDto job, UserProfileDto profile){
        return """
                Analyze job fit.
                Candidate skills: %s
                Preferred titles: %s Countries: %s
                Job title: %s Company: %s Location: %s WorkMode: %s Seniority: %s
                Description: %s
                Requirements: %s
                TechStacks: %s Salary: %s-%s
                Return JSON: {"matchPercentage":0-100, "matchingSkills":[], "missingSkills":[], "experienceFit":"...", "salaryFit":"...", "whyMatches":"...", "whyNotMatches":"..."}
                Only return JSON.
                """.formatted(profile.skills(), profile.preferredJobTitles(), profile.preferredCountries(), job.title(), job.company(), job.location(), job.workMode(), job.seniority(), job.description(), job.requirements(), job.techStacks(), job.salaryMin(), job.salaryMax());
    }

    private JobDto parseAnalyzeResponse(String json, JobDto job){
        try{
            String clean = extractJson(json);
            JsonNode n = om.readTree(clean);
            int pct = n.path("matchPercentage").asInt(job.matchPercentage());
            List<String> matching = om.convertValue(n.path("matchingSkills"), List.class);
            List<String> missing = om.convertValue(n.path("missingSkills"), List.class);
            String expFit = n.path("experienceFit").asText(job.experienceFit());
            String salFit = n.path("salaryFit").asText(job.salaryFit());
            String whyM = n.path("whyMatches").asText(job.whyMatches());
            String whyNot = n.path("whyNotMatches").asText(job.whyNotMatches());
            return new JobDto(job.id(), job.title(), job.company(), job.description(), job.location(), job.country(), job.workMode(), job.seniority(), job.techStacks(), job.source(), job.url(), job.salaryMin(), job.salaryMax(), job.currency(), job.postedAt(), job.requirements(), job.isFavorite(), pct, matching, missing, expFit, salFit, whyM, whyNot, java.time.Instant.now());
        }catch(Exception e){
            log.warn("Parse analyze failed {}", e.getMessage());
            return heuristicAnalyze(job, null);
        }
    }
    private String extractJson(String s){
        int a = s.indexOf('{'); int b = s.lastIndexOf('}');
        if(a>=0 && b> a) return s.substring(a, b+1);
        int c = s.indexOf('['); int d = s.lastIndexOf(']');
        if(c>=0 && d>c) return s.substring(c, d+1);
        return s;
    }

    // Heuristic fallbacks (mirrors Android)
    private JobDto heuristicAnalyze(JobDto job, UserProfileDto profile){
        List<String> userSkills = profile!=null && profile.skills()!=null? profile.skills().stream().map(String::toLowerCase).toList(): List.of();
        List<String> reqLower = job.requirements()!=null? job.requirements().stream().map(String::toLowerCase).toList(): List.of();
        java.util.ArrayList<String> matching = new java.util.ArrayList<>();
        java.util.ArrayList<String> missing = new java.util.ArrayList<>();
        for(String s: userSkills){ if(reqLower.stream().anyMatch(r-> r.contains(s)) || job.description().toLowerCase().contains(s)) matching.add(s); }
        for(String r: reqLower){ if(userSkills.stream().noneMatch(u-> u.contains(r))) missing.add(r); }
        int base = reqLower.isEmpty()?50: (int)((matching.size()/(double)Math.max(1, matching.size()+missing.size()))*100);
        int bonusTitle = profile!=null && profile.preferredJobTitles()!=null && profile.preferredJobTitles().stream().anyMatch(t-> job.title().toLowerCase().contains(t.toLowerCase()))?10:0;
        int bonusCountry = profile!=null && profile.preferredCountries()!=null && profile.preferredCountries().contains(job.country())?10:0;
        int pct = Math.max(0, Math.min(95, base+bonusTitle+bonusCountry + (int)(Math.random()*10-5)));
        String expFit = pct>=80?"Excellent fit": pct>=60?"Good fit": pct>=40?"Partial fit":"Low fit";
        String salFit = job.salaryMax()==null?"Salary not specified": job.salaryMax()>10000?"Above market": job.salaryMax()>7000?"Market rate":"Below expectations";
        String whyM = matching.isEmpty()?"General experience relevant":"Matches: "+String.join(", ", matching);
        String whyNot = missing.isEmpty()?"No major gaps":"Missing: "+String.join(", ", missing);
        return new JobDto(job.id(), job.title(), job.company(), job.description(), job.location(), job.country(), job.workMode(), job.seniority(), job.techStacks(), job.source(), job.url(), job.salaryMin(), job.salaryMax(), job.currency(), job.postedAt(), job.requirements(), job.isFavorite(), pct, matching, missing, expFit, salFit, whyM, whyNot, java.time.Instant.now());
    }
    private String heuristicCoverLetter(JobDto job, UserProfileDto profile){
        String name = profile!=null?profile.fullName():"Applicant";
        String skills = profile!=null && profile.skills()!=null? String.join(", ", profile.skills()):"relevant skills";
        return """
                Dear Hiring Manager at %s,

                I am excited to apply for the %s position at %s (%s). With experience in %s, I am confident in my ability to contribute effectively.

                %s

                My background aligns with your requirements including %s. I am particularly drawn to %s because of its innovation in the %s space.

                I would welcome the opportunity to discuss how my experience with %s can help %s achieve its goals. Thank you for considering my application.

                Sincerely,
                %s
                %s
                """.formatted(job.company(), job.title(), job.company(), job.location(), skills, job.whyMatches(), job.requirements()!=null? String.join(", ", job.requirements().stream().limit(3).toList()):"", job.company(), job.techStacks()!=null && !job.techStacks().isEmpty()? job.techStacks().get(0):"technology", !job.matchingSkills().isEmpty()? String.join(", ", job.matchingSkills().stream().limit(3).toList()):"my core technologies", name, profile!=null?profile.email():"");
    }
    private List<Map<String,String>> heuristicInterview(JobDto job){
        java.util.ArrayList<Map<String,String>> list = new java.util.ArrayList<>();
        list.add(Map.of("question","Explain your experience with "+(job.requirements()!=null && !job.requirements().isEmpty()?job.requirements().get(0):"Kotlin")+" and how you've used it in production.","category","TECHNICAL","suggestedAnswer","Discuss projects, challenges, outcomes. Use STAR method."));
        list.add(Map.of("question","How would you design a scalable system for "+job.company()+"'s "+job.title()+" role?","category","SYSTEM_DESIGN","suggestedAnswer","Mention microservices, caching, DB choice, scaling strategies."));
        list.add(Map.of("question","What is the difference between Coroutines and Threads in Kotlin?","category","TECHNICAL","suggestedAnswer","Coroutines lightweight, cooperative; Threads OS-level."));
        list.add(Map.of("question","Why do you want to work at "+job.company()+"?","category","HR","suggestedAnswer","Research company values, mention alignment."));
        list.add(Map.of("question","Where do you see yourself in 5 years?","category","HR","suggestedAnswer","Show ambition but commitment."));
        list.add(Map.of("question","Tell me about a challenge you overcame.","category","BEHAVIORAL","suggestedAnswer","Use STAR, quantify impact."));
        if(job.missingSkills()!=null && !job.missingSkills().isEmpty()){
            list.add(Map.of("question","You listed "+job.missingSkills().get(0)+" as missing - how would you ramp up?","category","BEHAVIORAL","suggestedAnswer","Show learning plan: docs, courses, side projects."));
        }
        return list;
    }
}
