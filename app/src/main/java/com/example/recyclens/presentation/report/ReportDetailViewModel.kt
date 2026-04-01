package com.example.recyclens.presentation.report


import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recyclens.domain.model.ReportDetails
import com.example.recyclens.domain.use_cases.report.AcceptReportUseCase
import com.example.recyclens.domain.use_cases.report.GetReportDetailsUseCase
import com.example.recyclens.domain.use_cases.report.MarkAsCleanedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportDetailState(
    val isLoading: Boolean = true,
    val report: ReportDetails? = null,
    val error: String? = null,
    val isAccepting: Boolean = false,
    val taskAccepted: Boolean = false,
    val cleanedPhotoUri: Uri? = null,
    val isCleaning: Boolean = false,
    val taskCleaned: Boolean = false
)

class ReportDetailViewModel(
    private val getReportDetailsUseCase: GetReportDetailsUseCase,
    private val acceptReportUseCase: AcceptReportUseCase,
    private val markAsCleanedUseCase: MarkAsCleanedUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reportId: String = savedStateHandle.get<String>("reportId")!!
    private val _state = MutableStateFlow(ReportDetailState())
    val state = _state.asStateFlow()

    init {
        fetchDetails()
    }

    fun fetchDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getReportDetailsUseCase(reportId)
                .onSuccess { reportDetails ->
                    _state.update { it.copy(isLoading = false, report = reportDetails) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    fun onAcceptTask() {
        viewModelScope.launch {
            _state.update { it.copy(isAccepting = true) }
            acceptReportUseCase(reportId)
                .onSuccess {
                    _state.update { it.copy(isAccepting = false, taskAccepted = true) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isAccepting = false, error = exception.message) }
                }
        }
    }
    fun onCleanedPhotoSelected(uri: Uri?) {
        _state.update { it.copy(cleanedPhotoUri = uri) }
    }

    fun onMarkAsCleaned() {
        viewModelScope.launch {
            _state.update { it.copy(isCleaning = true, error = null) }
            markAsCleanedUseCase(reportId, state.value.cleanedPhotoUri)
                .onSuccess {
                    _state.update { it.copy(isCleaning = false, taskCleaned = true) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isCleaning = false, error = exception.message) }
                }
        }
    }
}