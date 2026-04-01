package com.example.recyclens.domain.repository


import com.example.recyclens.data.remote.UserDataDto
import com.example.recyclens.domain.model.UserProfile
import com.example.recyclens.domain.use_cases.auth.VerifyOtpResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /**
     * Sends a request to the backend to send an OTP to the given phone number.
     * @param phoneNumber The 10-digit mobile number.
     * @param role The role of the user, e.g., "citizen" or "worker".
     * @return A Result indicating success or failure.
     */
    suspend fun requestOtp(phoneNumber: String, role: String): Result<Unit>

    /**
     * Verifies the OTP submitted by the user.
     * @param phoneNumber The 10-digit mobile number.
     * @param otp The 6-digit OTP entered by the user.
     * @return A Result containing a Boolean: true if verification is successful, false otherwise.
     */
    suspend fun verifyOtp(phoneNumber: String, otp: String): Result<VerifyOtpResult>

    /**
     * Checks if a user is currently logged in (e.g., has a valid auth token).
     * @return A Result containing a Boolean: true if logged in, false otherwise.
     */
    suspend fun checkAuthStatus(): Result<Boolean>

    /**
     * Logs the current user out by clearing their session/token.
     * @return A Result indicating success or failure.
     */
    suspend fun logout(): Result<Unit>

    /**
     * Retrieves the profile of the currently logged-in user.
     * In a real implementation, this would fetch details from a secure local storage
     * or a network call.
     * @return A Result containing the UserProfile.
     */
    suspend fun getUserProfile(): Result<UserProfile>
    suspend fun updateFcmToken(phoneNumber: String, token: String): Result<Unit>
    suspend fun getMe(): Result<UserDataDto>
}