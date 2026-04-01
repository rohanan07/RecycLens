package com.example.recyclens.domain.use_cases.report


import com.example.recyclens.domain.model.WasteReportListItem
import com.example.recyclens.domain.repository.ReportRepository

class GetWasteReportsUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(status: String?, assignedToMe: Boolean): Result<List<WasteReportListItem>> {
        return repository.getWasteReports(status, assignedToMe)
    }
}