package com.example.recyclens.presentation.home

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recyclens.domain.use_cases.auth.LogoutUseCase
import com.example.recyclens.domain.use_cases.profile.GetUserProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.edit
import com.example.recyclens.domain.model.UserProfile
import com.example.recyclens.domain.use_cases.report.GetReportStatsUseCase

private const val PERMISSIONS_REQUESTED_KEY = "has_requested_permissions"

data class HomeScreenState(
    val userProfile: UserProfile? = null,
    val currentDate: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val shouldRequestPermissions: Boolean = false,
    val pendingReportsCount: Int = 0
)

class HomeScreenViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sharedPreferences: SharedPreferences,
    private val getReportStatsUseCase: GetReportStatsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeScreenState())
    val state = _state.asStateFlow()

    init {
        loadData()
        checkIfPermissionsShouldBeRequested()
    }

    private fun checkIfPermissionsShouldBeRequested() {
        val hasRequested = sharedPreferences.getBoolean(PERMISSIONS_REQUESTED_KEY, false)
        if (!hasRequested) {
            _state.update { it.copy(shouldRequestPermissions = true) }
        }
    }
    fun onPermissionsRequested() {
        sharedPreferences.edit { putBoolean(PERMISSIONS_REQUESTED_KEY, true) }
        _state.update { it.copy(shouldRequestPermissions = false) }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
            val date = dateFormat.format(Date())

            // Fetch profile and stats in parallel for better performance
            val profileResult = getUserProfileUseCase()
            val statsResult = getReportStatsUseCase()

            profileResult.onSuccess { profile ->
                statsResult.onSuccess { pendingCount ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            userProfile = profile,
                            currentDate = date,
                            pendingReportsCount = pendingCount
                        )
                    }
                }.onFailure {
                    // If stats fail, still show the profile
                    _state.update {
                        it.copy(isLoading = false, userProfile = profile, currentDate = date)
                    }
                }
            }.onFailure { exception ->
                _state.update { it.copy(isLoading = false, error = exception.message) }
            }
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            logoutUseCase()
            // Navigation will be handled by observing the auth state in a higher-level component if needed
        }
    }
}