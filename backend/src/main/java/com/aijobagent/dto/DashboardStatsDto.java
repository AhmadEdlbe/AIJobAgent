package com.aijobagent.dto;

public record DashboardStatsDto(
        long totalJobsFound,
        long highMatchJobs,
        long applicationsSent,
        long interviews,
        long offers,
        long pendingApprovals
) {}
