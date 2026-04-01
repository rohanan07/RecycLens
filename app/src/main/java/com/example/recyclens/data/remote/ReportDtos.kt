package com.example.recyclens.data.remote

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * DTO for the POST /api/reports/verify-image endpoint.
 */
data class VerifyImageRequest(
    val imageUrl: String
)

/**
 * DTO representing the successful response from the /api/reports/verify-image endpoint.
 */
data class VerifyImageResponse(
    val verified: Boolean,
    val wasteType: String?,
    val confidenceScore: Double?
)

/**
 * DTO for the POST /api/reports endpoint.
 */
data class CreateReportRequest(
    val imageUrl: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val wasteType: String,
    val weight: Int
)

/**
 * DTO representing the successful response from the GET /api/reports/stats endpoint.
 */
data class ReportStatsDto(
    val pendingCount: Int
)

/**
 * DTO representing a single waste report item in a list from the GET /api/reports endpoint.
 */
data class WasteReportDto(
    @SerializedName("_id")
    val id: String,
    val address: String,
    val status: String,
    val createdAt: Date,
    val imageUrl: String
)

data class ReportDetailDto(
    @SerializedName("_id")
    val id: String,
    val address: String,
    val imageUrl: String,
    val wasteType: String,
    val weight: Int,
    val status: String,
    val location: LocationDto,
    val citizenId: CitizenDto?,
    val createdAt: Date
)

data class LocationDto(
    val coordinates: List<Double> // [longitude, latitude]
)

data class CitizenDto(
    val name: String,
    val phoneNumber: String
)

data class MarkAsCleanedRequest(
    val cleanedImageUrl: String
)