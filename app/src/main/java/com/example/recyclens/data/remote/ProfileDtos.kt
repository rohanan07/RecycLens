package com.example.recyclens.data.remote

import com.google.gson.annotations.SerializedName

/**
 * DTO for the PUT /api/users/profile endpoint.
 */
data class UpdateProfileRequest(
    val name: String,
    val address: String,
    val profilePhotoUrl: String?
)

/**
 * DTO representing the successful response from the GET /api/users/profile endpoint.
 */
data class UserProfileDto(
    val name: String?,
    val profilePhotoUrl: String?
)

/**
 * A more detailed DTO for user information, mapping MongoDB's '_id'.
 */
data class UserDto(
    @SerializedName("_id")
    val id: String,
    val phoneNumber: String,
    val role: String,
    val profilePhotoUrl: String?
)