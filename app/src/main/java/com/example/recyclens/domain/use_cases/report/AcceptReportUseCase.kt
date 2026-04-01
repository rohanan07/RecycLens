package com.example.recyclens.domain.use_cases.report

import com.example.recyclens.domain.repository.ReportRepository

class AcceptReportUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(reportId: String): Result<Unit> {
        if (reportId.isBlank()) {
            return Result.failure(IllegalArgumentException("Report ID cannot be empty."))
        }
        return repository.acceptReport(reportId)
    }
}