package com.example.recyclens.domain.use_cases.report

import com.example.recyclens.domain.model.WasteReportListItem
import com.example.recyclens.domain.repository.ReportRepository

class GetMyReportsUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(status: String?): Result<List<WasteReportListItem>> {
        return repository.getMyReports(status)
    }
}