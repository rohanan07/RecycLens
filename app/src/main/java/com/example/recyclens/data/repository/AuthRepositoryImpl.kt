package com.example.recyclens.data.repository

import android.content.SharedPreferences

import com.example.recyclens.data.services.ApiService
import com.example.recyclens.domain.repository.AuthRepository
import com.google.gson.Gson
import androidx.core.content.edit
import com.example.recyclens.data.remote.OtpRequestDto
import com.example.recyclens.data.remote.UpdateFcmTokenRequest
import com.example.recyclens.data.remote.UserDataDto
import com.example.recyclens.data.remote.VerifyOtpRequest
import com.example.recyclens.domain.model.UserProfile
import com.example.recyclens.domain.use_cases.auth.VerifyOtpResult

private const val AUTH_TOKEN_KEY = "auth_token"

class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val sharedPreferences: SharedPreferences // To save the auth token
) : AuthRepository {

    companion object {
        private const val AUTH_TOKEN_KEY = "auth_token"
    }

    override suspend fun requestOtp(phoneNumber: String, role: String): Result<Unit> {
        return try {
            val response = apiService.requestOtp(OtpRequestDto(phoneNumber, role))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                // Try to parse the error body from the backend
                val errorBody = response.errorBody()?.string()
                val message = try {
                    Gson().fromJson(errorBody, Map::class.java)["message"] as? String
                } catch (e: Exception) {
                    null
                } ?: "An unknown error occurred."
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Could not connect to the server. Please check your network connection."))
        }
    }

    override suspend fun verifyOtp(phoneNumber: String, otp: String): Result<VerifyOtpResult> {
        return try {
            val response = apiService.verifyOtp(VerifyOtpRequest(phoneNumber, otp))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                // Save the token from the top level of the response
                sharedPreferences.edit { putString(AUTH_TOKEN_KEY, body.token) }

                // --- THIS IS THE CHANGE ---
                // Create the result object using data from the nested 'user' object
                val result = VerifyOtpResult(
                    isNewUser = body.user.isNewUser,
                    role = body.user.role
                )
                Result.success(result)
            } else {
                Result.failure(Exception("Invalid OTP or server error."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkAuthStatus(): Result<Boolean> {
        val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
        return Result.success(!token.isNullOrEmpty())
    }

    override suspend fun logout(): Result<Unit> {
        sharedPreferences.edit { remove(AUTH_TOKEN_KEY) }
        return Result.success(Unit)
    }

    // We will implement these later with a real API call
    override suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
                ?: return Result.failure(Exception("Not authenticated."))

            val response = apiService.getUserProfile("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                // --- THIS IS THE FIX ---
                // We must correctly map the 'profilePhotoUrl' from the DTO
                // to the 'profileImageUrl' in our domain model.
                val userProfile = UserProfile(
                    name = dto.name?: "",
                    profileImageUrl = dto.profilePhotoUrl
                )
                Result.success(userProfile)
            } else {
                Result.failure(Exception("Failed to fetch user profile."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun updateFcmToken(phoneNumber: String, token: String): Result<Unit> {
        return try {
            val response = apiService.updateFcmToken(UpdateFcmTokenRequest(phoneNumber, token))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update FCM token on server."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getMe(): Result<UserDataDto> {
        return try {
            val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
                ?: return Result.failure(Exception("Not authenticated."))

            val response = apiService.getMe("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // If the token is invalid or expired, the server will return an error.
                // We clear the bad token to force a login next time.
                sharedPreferences.edit { remove(AUTH_TOKEN_KEY) }
                Result.failure(Exception("Session expired. Please log in again."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}