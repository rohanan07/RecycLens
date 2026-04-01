package com.example.recyclens.domain.use_cases.auth

import com.example.recyclens.domain.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class UpdateFcmTokenUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(phoneNumber: String): Result<Unit> {
        return try {
            // Get the latest FCM token from Firebase
            val token = FirebaseMessaging.getInstance().token.await()
            // Send the token to our backend
            repository.updateFcmToken(phoneNumber, token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}