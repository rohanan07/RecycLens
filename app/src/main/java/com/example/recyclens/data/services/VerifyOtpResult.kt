package com.example.recyclens.data.services

data class VerifyOtpResult(
    val isNewUser: Boolean,
    val role: String // <-- Add the role field
)