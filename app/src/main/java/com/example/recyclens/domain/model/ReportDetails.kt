package com.example.recyclens.domain.model

data class ReportDetails(
    val id: String,
    val address: String,
    val imageUrl: String,
    val wasteType: String,
    val weight: String, // Formatted as "Approx. X KG"
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val reportedBy: String,
    val reportedAt: String
)