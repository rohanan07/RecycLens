package com.example.recyclens.domain.use_cases.profile


import com.example.recyclens.domain.model.UserProfile
import com.example.recyclens.domain.repository.AuthRepository

class GetUserProfileUseCase(
    private val repository: AuthRepository
) {
    /**
     * Executes the use case.
     * @return A Result object containing the UserProfile on success, or an Exception on failure.
     */
    suspend operator fun invoke(): Result<UserProfile> {
        return repository.getUserProfile()
    }
}