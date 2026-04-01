package com.example.recyclens.domain.use_cases.report

import android.net.Uri
import com.example.recyclens.domain.repository.ReportRepository

class MarkAsCleanedUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(reportId: String, cleanedPhotoUri: Uri?): Result<Unit> {
        if (reportId.isBlank()) {
            return Result.failure(IllegalArgumentException("Report ID cannot be empty."))
        }
        if (cleanedPhotoUri == null) {
            return Result.failure(IllegalArgumentException("A photo of the cleaned area is required."))
        }
        return repository.markAsCleaned(reportId, cleanedPhotoUri)
    }
}