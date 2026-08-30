package com.aijobagent.dto;

import java.time.Instant;

public record ApplicationDto(
        String id,
        String jobId,
        JobDto job,
        String status,
        Instant appliedAt,
        Instant interviewDate,
        String notes,
        String coverLetterId,
        Instant createdAt,
        Instant updatedAt
) {}
