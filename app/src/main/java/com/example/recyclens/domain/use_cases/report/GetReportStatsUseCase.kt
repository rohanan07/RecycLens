package com.example.recyclens.domain.use_cases.report

import com.example.recyclens.domain.repository.ReportRepository

class GetReportStatsUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return repository.getReportStats()
    }
}