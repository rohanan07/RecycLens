package com.example.recyclens.domain.repository

import android.net.Uri
import com.example.recyclens.domain.model.ReportDetails
import com.example.recyclens.domain.model.WasteReportListItem


data class VerifyImageResult(
    val imageUrl: String,
    val detectedWasteType: String,
    val confidenceScore: Double
)
interface ReportRepository {
    suspend fun verifyWasteImage(photoUri: Uri): Result<VerifyImageResult>// Returns image URL on success
    suspend fun createWasteReport(
        imageUrl: String,
        latitude: Double,
        longitude: Double,
        address: String,
        wasteType: String,
        weight: Int
    ): Result<Unit>
    suspend fun getReportStats(): Result<Int>
    suspend fun getWasteReports(status: String?, assignedToMe: Boolean): Result<List<WasteReportListItem>>
    suspend fun acceptReport(reportId: String): Result<Unit>
    suspend fun getReportDetails(reportId: String): Result<ReportDetails>
    suspend fun markAsCleaned(reportId: String, cleanedPhotoUri: Uri): Result<Unit>
    suspend fun getMyReports(status: String?): Result<List<WasteReportListItem>>
}