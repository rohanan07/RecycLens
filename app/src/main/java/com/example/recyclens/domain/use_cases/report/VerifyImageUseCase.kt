package com.example.recyclens.domain.use_cases.report

import android.net.Uri
import com.example.recyclens.domain.repository.ReportRepository
import com.example.recyclens.domain.repository.VerifyImageResult

class VerifyImageUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(photoUri: Uri): Result<VerifyImageResult> {
        return repository.verifyWasteImage(photoUri)
    }
}