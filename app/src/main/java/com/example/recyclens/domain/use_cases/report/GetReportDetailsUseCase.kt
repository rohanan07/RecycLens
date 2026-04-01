package com.example.recyclens.domain.use_cases.report

import com.example.recyclens.domain.model.ReportDetails
import com.example.recyclens.domain.repository.ReportRepository

class GetReportDetailsUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(reportId: String): Result<ReportDetails> {
        return repository.getReportDetails(reportId)
    }
}