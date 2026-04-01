package com.example.recyclens.domain.use_cases.auth

import com.example.recyclens.domain.repository.AuthRepository

class CheckAuthStatusUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return repository.checkAuthStatus()
    }
}