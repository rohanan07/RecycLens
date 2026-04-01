package com.example.recyclens.domain.use_cases.profile

import android.net.Uri
import com.example.recyclens.domain.repository.ProfileRepository

class UpdateProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(name: String, address: String, photoUri: Uri?): Result<Unit> {
        if (name.isBlank() || address.isBlank()) {
            return Result.failure(IllegalArgumentException("Name and address cannot be empty."))
        }
        return repository.updateUserProfile(name, address, photoUri)
    }
}