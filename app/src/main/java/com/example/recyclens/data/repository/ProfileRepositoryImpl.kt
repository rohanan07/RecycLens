package com.example.recyclens.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.recyclens.data.remote.UpdateProfileRequest
import com.example.recyclens.data.services.ApiService
import com.example.recyclens.domain.repository.ProfileRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val AUTH_TOKEN_KEY = "auth_token"

class ProfileRepositoryImpl(
    private val apiService: ApiService,
    private val sharedPreferences: SharedPreferences,
    private val context: Context // We need context for Cloudinary
) : ProfileRepository {

    override suspend fun updateUserProfile(name: String, address: String, photoUri: Uri?): Result<Unit> {
        return try {
            val imageUrl = if (photoUri != null) {
                uploadImageToCloudinary(photoUri)
            } else {
                null
            }

            val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
                ?: return Result.failure(Exception("Not authenticated."))

            val response = apiService.updateUserProfile(
                "Bearer $token",
                UpdateProfileRequest(name, address, imageUrl)
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update profile on server."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadImageToCloudinary(uri: Uri): String? = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    continuation.resume(resultData["secure_url"] as? String)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resume(null)
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch(context)
    }
}