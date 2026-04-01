package com.example.recyclens.data.remote

data class RequestOtpRequest(
    val phoneNumber: String,
    val role: String,
    val fcmToken: String?
)

/**
 * DTO for the POST /api/auth/verify-otp endpoint.
 */
data class VerifyOtpRequest(
    val phoneNumber: String,
    val otp: String
)
data class OtpRequestDto(
    val phoneNumber: String,
    val role: String
)
/**
 * DTO representing the successful response from the /api/auth/verify-otp endpoint.
 */
data class VerifyOtpResponse(
    val token: String,
    val user: UserDataDto
)

/**
 * DTO for the POST /api/users/update-fcm-token endpoint.
 */
data class UpdateFcmTokenRequest(
    val phoneNumber: String,
    val fcmToken: String
)

data class UserDataDto(
    val id: String,
    val phoneNumber: String,
    val role: String?,
    val name: String?,
    val address: String?,
    val profilePhotoUrl: String?,
    val isNewUser: Boolean
)