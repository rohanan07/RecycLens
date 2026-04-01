package com.example.recyclens.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.util.CoilUtils.result
import com.example.recyclens.domain.use_cases.auth.RequestOtpUseCase
import com.example.recyclens.domain.use_cases.auth.UpdateFcmTokenUseCase
import com.example.recyclens.domain.use_cases.auth.VerifyOtpResult
import com.example.recyclens.domain.use_cases.auth.VerifyOtpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AuthNavigationEvent {
    data class NavigateToHome(val userRole: String?) : AuthNavigationEvent() // Now carries the role
    object NavigateToCreateProfile : AuthNavigationEvent()
}

class AuthViewModel(
    private val requestOtpUseCase: RequestOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val updateFcmTokenUseCase: UpdateFcmTokenUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    private val _navigationEvent = MutableStateFlow<AuthNavigationEvent?>(null)
    val navigationEvent = _navigationEvent.asStateFlow()


    fun onPhoneNumberChanged(newNumber: String) {
        // Allow only digits and limit to 10 characters.
        if (newNumber.all { it.isDigit() } && newNumber.length <= 10) {
            _state.update { it.copy(phoneNumber = newNumber, error = null) }
        }
    }

    fun onOtpChanged(newOtp: String) {
        if (newOtp.all { it.isDigit() } && newOtp.length <= 6) {
            _state.update { it.copy(otp = newOtp, error = null) }
        }
    }


    fun onUserTypeSelected(type: UserType) {
        // Reset state when user type changes
        _state.update { it.copy(userType = type, phoneNumber = "", error = null, isOtpSent = false) }
    }

    fun requestOtp() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val role = when (state.value.userType) {
                UserType.CITIZEN -> "citizen"
                UserType.WORKER -> "worker"
                UserType.UNSELECTED -> {
                    _state.update { it.copy(isLoading = false, error = "An unexpected error occurred.") }
                    return@launch // Should not happen
                }
            }

            requestOtpUseCase(state.value.phoneNumber, role)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isOtpSent = true) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }
    fun verifyOtp() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            verifyOtpUseCase(state.value.phoneNumber, state.value.otp)
                .onSuccess { result: VerifyOtpResult ->
                    // --- ADDED DEBUG LOG ---
                    Log.d("AuthViewModelDebug", "Role received from backend: '${result.role}'")

                    updateFcmTokenUseCase(state.value.phoneNumber).onSuccess {
                        handleNavigation(result)
                    }.onFailure {
                        handleNavigation(result)
                    }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }
    private fun handleNavigation(result: VerifyOtpResult) {
        if (result.isNewUser) {
            _navigationEvent.value = AuthNavigationEvent.NavigateToCreateProfile
        } else {
            _navigationEvent.value = AuthNavigationEvent.NavigateToHome(result.role)
        }
        _state.update { it.copy(isLoading = false, isVerificationSuccessful = true) }
    }
    fun editPhoneNumber() {
        // Allows user to go back and change their number
        _state.update { it.copy(isOtpSent = false, otp = "", error = null) }
    }
    fun onNavigationComplete() {
        _navigationEvent.value = null
        _state.update { it.copy(isLoading = false) }
    }
}