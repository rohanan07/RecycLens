package com.example.recyclens.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recyclens.domain.use_cases.profile.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateProfileState(
    val currentStep: Int = 1, // 1 for Name, 2 for Address, 3 for Photo
    val name: String = "",
    val address: String = "",
    val photoUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isProfileCreated: Boolean = false
)

class CreateProfileViewModel(
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateProfileState())
    val state = _state.asStateFlow()

    fun onNameChanged(newName: String) {
        _state.update { it.copy(name = newName) }
    }

    fun onAddressChanged(newAddress: String) {
        _state.update { it.copy(address = newAddress) }
    }

    fun onPhotoSelected(uri: Uri?) {
        _state.update { it.copy(photoUri = uri) }
    }

    fun onNextStep() {
        if (state.value.currentStep < 3) {
            _state.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun onPreviousStep() {
        if (state.value.currentStep > 1) {
            _state.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun submitProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            updateProfileUseCase(
                name = state.value.name,
                address = state.value.address,
                photoUri = state.value.photoUri
            ).onSuccess {
                _state.update { it.copy(isLoading = false, isProfileCreated = true) }
            }.onFailure { exception ->
                _state.update { it.copy(isLoading = false, error = exception.message) }
            }
        }
    }
}