package com.example.recyclens.presentation.report

import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recyclens.domain.use_cases.report.CreateReportUseCase
import com.example.recyclens.domain.use_cases.report.VerifyImageUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ReportWasteEvent {
    data class ShowToast(val message: String) : ReportWasteEvent()
}

data class ReportWasteState(
    // UI State
    val currentStep: Int = 1, // 1: Details, 2: Location, 3: Submit
    val isLoading: Boolean = false,
    val error: String? = null,
    val isReportSubmitted: Boolean = false,

    // Image State
    val photoUri: Uri? = null,
    val isImageVerifying: Boolean = false,
    val isImageVerified: Boolean = false,
    val verifiedImageUrl: String? = null,

    // Form Data
    val weight: String = "",
    val wasteType: String = "",

    // Location Data
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "Fetching address..."
)

class ReportWasteViewModel(
    private val verifyImageUseCase: VerifyImageUseCase,
    private val createReportUseCase: CreateReportUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ReportWasteState())
    val state = _state.asStateFlow()
    private val _eventFlow = MutableSharedFlow<ReportWasteEvent>()
    val eventFlow = _eventFlow.asSharedFlow()
    fun onPhotoSelected(uri: Uri?) {
        if (uri == null) return
        Log.d("ReportWasteVM", "Photo selected: $uri")
        _state.update { it.copy(photoUri = uri, isImageVerified = false, verifiedImageUrl = null, error = null) }
        verifyImage(uri)
    }

    private fun verifyImage(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isImageVerifying = true) }
            try {
                val result = verifyImageUseCase(uri)
                result.onSuccess { res ->
                    Log.d("ReportWasteVM", "Verification success: $res")
                    if(res.confidenceScore >= 80) {
                        _state.update {
                            it.copy(
                                isImageVerifying = false,
                                isImageVerified = true,
                                verifiedImageUrl = res.imageUrl,
                                wasteType = res.detectedWasteType
                            )
                        }
                    }
                    else {
                        // Confidence is too low, reject the image
                        _eventFlow.emit(ReportWasteEvent.ShowToast("Low confidence score. Please upload another image."))
                        _state.update {
                            it.copy(
                                isImageVerifying = false,
                                error = "Verification failed (confidence too low). Please upload a clearer image.",
                                photoUri = null // Clear the photo URI to reset the UI
                            )
                        }
                    }
                }.onFailure { exception ->
                    _eventFlow.emit(ReportWasteEvent.ShowToast( "Low confidence score, please upload another image"))
                    Log.e("ReportWasteVM", "Verification failed: ${exception.message}")
                    _state.update { it.copy(isImageVerifying = false, error = exception.message) } // do NOT clear photoUri
                }
            } catch (e: Exception) {
                Log.e("ReportWasteVM", "Exception during verification: ${e.message}")
                _state.update { it.copy(isImageVerifying = false, error = e.message) }
            }
        }
    }
    fun onWeightChanged(newWeight: String) {
        if (newWeight.all { it.isDigit() }) {
            _state.update { it.copy(weight = newWeight) }
        }
    }

    fun onWasteTypeChanged(newType: String) {
        _state.update { it.copy(wasteType = newType) }
    }

    fun onLocationUpdate(lat: Double, long: Double, geocoder: Geocoder) {
        _state.update { it.copy(latitude = lat, longitude = long) }
        fetchAddress(lat, long, geocoder)
    }

    private fun fetchAddress(lat: Double, long: Double, geocoder: Geocoder) {
        try {
            val addresses = geocoder.getFromLocation(lat, long, 1)
            val addressText = addresses?.firstOrNull()?.let {
                "${it.thoroughfare ?: ""}, ${it.locality ?: ""}, ${it.subAdminArea ?: ""}, ${it.postalCode ?: ""}"
            } ?: "Address not found"
            _state.update { it.copy(address = addressText) }
        } catch (e: Exception) {
            _state.update { it.copy(address = "Could not fetch address") }
        }
    }

    fun onNextStep() {
        val currentStep = _state.value.currentStep
        if (currentStep < 3) {
            _state.update { it.copy(currentStep = currentStep + 1, error = null) }
        }
    }

    fun onPreviousStep() {
        val currentStep = _state.value.currentStep
        if (currentStep > 1) {
            _state.update { it.copy(currentStep = currentStep - 1, error = null) }
        }
    }

    fun submitReport() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val currentState = _state.value
            if (currentState.verifiedImageUrl == null) {
                _state.update { it.copy(isLoading = false, error = "Image not verified.") }
                return@launch
            }

            createReportUseCase(
                imageUrl = currentState.verifiedImageUrl,
                latitude = currentState.latitude,
                longitude = currentState.longitude,
                address = currentState.address,
                wasteType = currentState.wasteType,
                weight = currentState.weight.toIntOrNull() ?: 0
            ).onSuccess {
                _state.update { it.copy(isLoading = false, isReportSubmitted = true) }
            }.onFailure { exception ->
                _state.update { it.copy(isLoading = false, error = exception.message) }
            }
        }
    }
}