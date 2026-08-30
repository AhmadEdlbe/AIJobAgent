package com.aijobagent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public record UserProfileDto(
        String id,
        @NotBlank String fullName,
        @Email String email,
        String phoneNumber,
        String linkedInUrl,
        String gitHubUrl,
        String resumeText,
        String resumeFileName,
        List<String> preferredCountries,
        List<String> preferredJobTitles,
        List<String> skills,
        Instant updatedAt
) {}
