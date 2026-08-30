package com.aijobagent.dto;

import java.time.Instant;
import java.util.List;

public record InterviewPrepDto(
        String id,
        String jobId,
        List<InterviewQuestionDto> questions,
        Instant generatedAt
) {
    public record InterviewQuestionDto(String question, String category, String suggestedAnswer) {}
}
