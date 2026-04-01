package com.example.recyclens.presentation.auth

data class AuthState(
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val otp: String = "",
    val error: String? = null,
    val isOtpSent: Boolean = false,
    val userType: UserType = UserType.UNSELECTED,
    val isVerificationSuccessful: Boolean = false,
)