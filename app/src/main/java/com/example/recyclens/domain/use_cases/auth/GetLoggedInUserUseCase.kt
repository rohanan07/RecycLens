package com.example.recyclens.domain.use_cases.auth

import com.example.recyclens.data.remote.UserDataDto
import com.example.recyclens.domain.repository.AuthRepository

class GetLoggedInUserUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<UserDataDto?> {
        // First, check if there's a token. If not, the user is not logged in.
        val isLoggedIn = repository.checkAuthStatus().getOrDefault(false)
        if (!isLoggedIn) {
            return Result.success(null)
        }
        // If logged in, fetch the full user data.
        return repository.getMe()
    }
}