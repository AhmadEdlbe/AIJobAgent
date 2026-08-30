package com.aijobagent.dto;

import java.time.Instant;

public record CoverLetterDto(
        String id,
        String jobId,
        String content,
        Instant generatedAt,
        boolean isEdited
) {}
