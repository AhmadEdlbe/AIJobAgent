package com.aijobagent.dto;

import java.util.List;

public record ScanRequest(
        String query,
        List<String> preferredCountries,
        List<String> preferredTitles,
        List<String> skills
) {}
