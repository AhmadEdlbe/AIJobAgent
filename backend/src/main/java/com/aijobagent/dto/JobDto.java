package com.aijobagent.dto;

import java.time.Instant;
import java.util.List;

public record JobDto(
        String id,
        String title,
        String company,
        String description,
        String location,
        String country,
        String workMode,
        String seniority,
        List<String> techStacks,
        String source,
        String url,
        Integer salaryMin,
        Integer salaryMax,
        String currency,
        Instant postedAt,
        List<String> requirements,
        boolean isFavorite,
        // AI matching
        int matchPercentage,
        List<String> matchingSkills,
        List<String> missingSkills,
        String experienceFit,
        String salaryFit,
        String whyMatches,
        String whyNotMatches,
        Instant analyzedAt
) {}
