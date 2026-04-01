package com.example.recyclens.domain.repository

import android.net.Uri

interface ProfileRepository {
    suspend fun updateUserProfile(
        name: String,
        address: String,
        photoUri: Uri?
    ): Result<Unit>
}