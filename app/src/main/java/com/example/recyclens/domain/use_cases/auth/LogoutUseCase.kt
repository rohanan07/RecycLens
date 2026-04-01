package com.example.recyclens.domain.use_cases.auth

import com.example.recyclens.domain.repository.AuthRepository

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.logout()
}