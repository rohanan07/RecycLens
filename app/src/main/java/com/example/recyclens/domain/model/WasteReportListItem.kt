package com.example.recyclens.domain.model

data class WasteReportListItem(
    val id: String,
    val address: String,
    val status: String,
    val relativeDate: String // e.g., "Cleared 3 Days Ago"
)