package com.example.recyclens.data.services

import com.example.recyclens.data.remote.CreateReportRequest
import com.example.recyclens.data.remote.MarkAsCleanedRequest

import com.example.recyclens.data.remote.OtpRequestDto
import com.example.recyclens.data.remote.ReportDetailDto
import com.example.recyclens.data.remote.ReportStatsDto
import com.example.recyclens.data.remote.UpdateFcmTokenRequest
import com.example.recyclens.data.remote.UpdateProfileRequest
import com.example.recyclens.data.remote.UserDataDto
import com.example.recyclens.data.remote.UserDto
import com.example.recyclens.data.remote.UserProfileDto

import com.example.recyclens.data.remote.VerifyImageResponse
import com.example.recyclens.data.remote.VerifyOtpRequest

import com.example.recyclens.data.remote.VerifyOtpResponse

import com.example.recyclens.data.remote.WasteReportDto

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("/api/auth/request-otp")
    suspend fun requestOtp(@Body request: OtpRequestDto): Response<Unit>

    @POST("/api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("/api/users/update-fcm-token")
    suspend fun updateFcmToken(@Body request: UpdateFcmTokenRequest): Response<Unit>

    @PUT("/api/users/profile")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<Unit>

    @GET("/api/users/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<UserProfileDto>

    @Multipart
    @POST("/api/reports/verify-image")
    suspend fun verifyWasteImage(
        @Header("Authorization") token: String,
        @Part("image_url") imageUrl: String
    ): Response<VerifyImageResponse>

    @POST("/api/reports")
    suspend fun createWasteReport(
        @Header("Authorization") token: String,
        @Body request: CreateReportRequest
    ): Response<Unit>

    @GET("/api/reports/stats")
    suspend fun getReportStats(@Header("Authorization") token: String): Response<ReportStatsDto>

    @GET("/api/reports")
    suspend fun getWasteReports(
        @Header("Authorization") token: String,
        @Query("status") status: String?,
        // --- NEW PARAMETER ---
        @Query("assignedToMe") assignedToMe: Boolean?
    ): Response<List<WasteReportDto>>

    @GET("/api/users/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<UserDataDto>

    @PUT("/api/reports/{id}/approve")
    suspend fun approveReport(
        @Header("Authorization") token: String,
        @Path("id") reportId: String
    ): Response<Unit>

    @PUT("/api/reports/{id}/assign")
    suspend fun assignReport(
        @Header("Authorization") token: String,
        @Path("id") reportId: String,
        @Body workerIdBody: Map<String, String> // e.g., {"workerId": "..."}
    ): Response<Unit>

    @PUT("/api/reports/{id}/accept")
    suspend fun acceptReport(
        @Header("Authorization") token: String,
        @Path("id") reportId: String
    ): Response<Unit>

    @GET("/api/reports/{id}")
    suspend fun getReportDetails(
        @Header("Authorization") token: String,
        @Path("id") reportId: String
    ): Response<ReportDetailDto>

    @PUT("/api/reports/{id}/clean")
    suspend fun markAsCleaned(
        @Header("Authorization") token: String,
        @Path("id") reportId: String,
        @Body request: MarkAsCleanedRequest
    ): Response<Unit>

    @GET("/api/reports/my-reports")
    suspend fun getMyReports(
        @Header("Authorization") token: String,
        @Query("status") status: String?
    ): Response<List<WasteReportDto>>
}