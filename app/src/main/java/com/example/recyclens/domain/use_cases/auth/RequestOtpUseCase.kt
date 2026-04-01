package com.example.recyclens.domain.use_cases.auth

import com.example.recyclens.domain.repository.AuthRepository

class RequestOtpUseCase(
    private val repository: AuthRepository
) {
    /**
     * Invokes the use case to request an OTP for a given phone number and role.
     * @param phoneNumber The 10-digit mobile number.
     * @param role The role of the user (e.g., "citizen", "worker").
     * @return A Result indicating success or failure.
     */
    suspend operator fun invoke(phoneNumber: String, role: String): Result<Unit> {
        if (phoneNumber.length != 10 || !phoneNumber.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("Phone number must be 10 digits."))
        }
        if (role.isBlank()) {
            return Result.failure(IllegalArgumentException("User role must be specified."))
        }
        return repository.requestOtp(phoneNumber, role)
    }
}