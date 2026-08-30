package com.example.aijobagent.domain.usecase

import com.example.aijobagent.domain.model.DashboardStats
import com.example.aijobagent.domain.repository.ApplicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor(
    private val repo: ApplicationRepository
) {
    operator fun invoke(): Flow<DashboardStats> = repo.getDashboardStats()
}
