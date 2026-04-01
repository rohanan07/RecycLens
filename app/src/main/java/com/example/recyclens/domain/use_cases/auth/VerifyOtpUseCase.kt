package com.example.recyclens.domain.use_cases.auth

import com.example.recyclens.domain.repository.AuthRepository

data class VerifyOtpResult(
    val isNewUser: Boolean,
    val role: String?
)

class VerifyOtpUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(phoneNumber: String, otp: String): Result<VerifyOtpResult> {
        if (otp.length != 6) {
            return Result.failure(IllegalArgumentException("OTP must be 6 digits."))
        }
        return repository.verifyOtp(phoneNumber, otp)
    }
}