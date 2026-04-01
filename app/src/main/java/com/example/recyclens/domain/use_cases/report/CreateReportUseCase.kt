package com.example.recyclens.domain.use_cases.report

import com.example.recyclens.domain.repository.ReportRepository

class CreateReportUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(
        imageUrl: String,
        latitude: Double,
        longitude: Double,
        address: String,
        wasteType: String,
        weight: Int
    ): Result<Unit> {
        if (imageUrl.isBlank() || address.isBlank() || wasteType.isBlank() || weight <= 0) {
            return Result.failure(IllegalArgumentException("All fields must be filled correctly."))
        }
        return repository.createWasteReport(imageUrl, latitude, longitude, address, wasteType, weight)
    }
}