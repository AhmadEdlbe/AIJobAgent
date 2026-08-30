package com.aijobagent.controller;

import com.aijobagent.dto.CoverLetterDto;
import com.aijobagent.dto.InterviewPrepDto;
import com.aijobagent.dto.JobDto;
import com.aijobagent.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService){ this.aiService=aiService; }

    @PostMapping("/analyze/{jobId}")
    public ResponseEntity<JobDto> analyze(@PathVariable String jobId){
        return ResponseEntity.ok(aiService.analyze(jobId));
    }

    @PostMapping("/cover-letter/{jobId}")
    public ResponseEntity<CoverLetterDto> coverLetter(@PathVariable String jobId){
        return ResponseEntity.ok(aiService.coverLetter(jobId));
    }

    @PostMapping("/interview-prep/{jobId}")
    public ResponseEntity<InterviewPrepDto> interviewPrep(@PathVariable String jobId){
        return ResponseEntity.ok(aiService.interviewPrep(jobId));
    }
}
