package com.example.recyclens.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.recyclens.data.remote.CreateReportRequest
import com.example.recyclens.data.remote.MarkAsCleanedRequest
import com.example.recyclens.data.remote.VerifyImageRequest
import com.example.recyclens.data.services.ApiService
import com.example.recyclens.domain.model.ReportDetails
import com.example.recyclens.domain.model.WasteReportListItem
import com.example.recyclens.domain.repository.ReportRepository
import com.example.recyclens.domain.repository.VerifyImageResult
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume


private const val AUTH_TOKEN_KEY = "auth_token"

class ReportRepositoryImpl(
    private val apiService: ApiService,
    private val sharedPreferences: SharedPreferences,
    private val context: Context
) : ReportRepository {

    override suspend fun verifyWasteImage(photoUri: Uri): Result<VerifyImageResult> {
        return try {
            val imageUrl = uploadImageToCloudinary(photoUri)
                ?: return Result.failure(Exception("Failed to upload image to Cloudinary."))

            val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
                ?: return Result.failure(Exception("Not authenticated."))

            // CORRECTED: Wrap the imageUrl in the VerifyImageRequest DTO
            val request = VerifyImageRequest(imageUrl = imageUrl)
            val response = apiService.verifyWasteImage("Bearer $token", request.imageUrl)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.verified) {
                    Result.success(
                        VerifyImageResult(
                            imageUrl = imageUrl,
                            detectedWasteType = body.wasteType ?: "unknown",
                            confidenceScore = body.confidenceScore?: 0.0
                        )
                    )
                } else {
                    Result.failure(Exception("Image could not be verified as waste."))
                }
            } else {
                Result.failure(Exception("Image verification failed with code ${response.code()}"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun createWasteReport(
        imageUrl: String,
        latitude: Double,
        longitude: Double,
        address: String,
        wasteType: String,
        weight: Int
    ): Result<Unit> {
        return try {
            val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
                ?: return Result.failure(Exception("Not authenticated."))

            val request =
                CreateReportRequest(imageUrl, latitude, longitude, address, wasteType, weight)
            val response = apiService.createWasteReport("Bearer $token", request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to submit report to server."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadImageToCloudinary(uri: Uri): String? = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    continuation.resume(resultData["secure_url"] as? String)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resume(null)
                }
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch(context)
    }
    override suspend fun getReportStats(): Result<Int> {
        return try {
            val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
                ?: return Result.failure(Exception("Not authenticated."))

            val response = apiService.getReportStats("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.pendingCount)
            } else {
                Result.failure(Exception("Failed to fetch report stats."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getWasteReports(status: String?, assignedToMe: Boolean): Result<List<WasteReportListItem>> {
        return try {
            val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
                ?: return Result.failure(Exception("Not authenticated."))

            // The 'assignedToMe' parameter is now passed to the ApiService.
            // We pass 'true' if it's true, and 'null' otherwise, so the query parameter is omitted if not needed.
            val response = apiService.getWasteReports("Bearer $token", status, if (assignedToMe) true else null)

            if (response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!
                val domainList = dtoList.map { dto ->
                    WasteReportListItem(
                        id = dto.id,
                        address = dto.address,
                        status = dto.status.replaceFirstChar { it.uppercase() },
                        relativeDate = formatRelativeDate(dto.createdAt)
                    )
                }
                Result.success(domainList)
            } else {
                Result.failure(Exception("Failed to fetch reports."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    private fun formatRelativeDate(date: Date): String {
        val diff = Date().time - date.time
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        return when {
            days == 0L -> "Today"
            days == 1L -> "Yesterday"
            days < 7 -> "$days Days Ago"
            else -> "${days / 7} Weeks Ago"
        }
    }
    override suspend fun acceptReport(reportId: String): Result<Unit> {
        return try {
            val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
                ?: return Result.failure(Exception("Not authenticated."))

            val response = apiService.acceptReport("Bearer $token", reportId)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to accept report. It may have already been assigned."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getReportDetails(reportId: String): Result<ReportDetails> {
        return try {
            val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null) ?: return Result.failure(Exception("Not authenticated."))
            val response = apiService.getReportDetails("Bearer $token", reportId)

            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val domainModel = ReportDetails(
                    id = dto.id,
                    address = dto.address,
                    imageUrl = dto.imageUrl,
                    wasteType = dto.wasteType.replaceFirstChar { it.uppercase() },
                    weight = "Approx. ${dto.weight} KG",
                    status = dto.status.replaceFirstChar { it.uppercase() },
                    latitude = dto.location.coordinates.getOrElse(1) { 0.0 },
                    longitude = dto.location.coordinates.getOrElse(0) { 0.0 },
                    reportedBy = dto.citizenId?.name ?: "Unknown",
                    reportedAt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(dto.createdAt)
                )
                Result.success(domainModel)
            } else {
                Result.failure(Exception("Failed to fetch report details."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun markAsCleaned(reportId: String, cleanedPhotoUri: Uri): Result<Unit> {
        return try {
            val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
                ?: return Result.failure(Exception("Not authenticated."))

            // First, upload the "cleaned" photo to Cloudinary
            val imageUrl = uploadImageToCloudinary(cleanedPhotoUri)
                ?: return Result.failure(Exception("Failed to upload cleaned image."))

            // Then, call our backend with the Cloudinary URL
            val request = MarkAsCleanedRequest(cleanedImageUrl = imageUrl)
            val response = apiService.markAsCleaned("Bearer $token", reportId, request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update report status on server."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getMyReports(status: String?): Result<List<WasteReportListItem>> {
        return try {
            val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
                ?: return Result.failure(Exception("Not authenticated."))

            val response = apiService.getMyReports("Bearer $token", status)

            if (response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!
                val domainList = dtoList.map { dto ->
                    WasteReportListItem(
                        id = dto.id,
                        address = dto.address,
                        status = dto.status.replaceFirstChar { it.uppercase() },
                        relativeDate = formatRelativeDate(dto.createdAt)
                    )
                }
                Result.success(domainList)
            } else {
                Result.failure(Exception("Failed to fetch your reports."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

