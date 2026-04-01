package com.example.recyclens.presentation.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recyclens.domain.model.UserProfile
import com.example.recyclens.domain.model.WasteReportListItem
import com.example.recyclens.domain.use_cases.profile.GetUserProfileUseCase

import com.example.recyclens.domain.use_cases.report.GetWasteReportsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class Tab(val displayName: String, val statusQuery: String?, val assignedToMe: Boolean) {
    object APPROVED : Tab("Approved Tasks", "approved", false)
    object MY_TASKS : Tab("My Tasks", "assigned", true)
    object DONE : Tab("Done Tasks", "cleaned", true)
}

data class WorkerHomeState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile? = null,
    val currentDate: String = "",
    val reports: List<WasteReportListItem> = emptyList(),
    val error: String? = null,
    val selectedTab: Tab = Tab.APPROVED
)

class WorkerHomeViewModel(
    private val getWasteReportsUseCase: GetWasteReportsUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WorkerHomeState())
    val state = _state.asStateFlow()

    init {
        loadInitialData()
    }

    fun onTabSelected(tab: Tab) {
        _state.update { it.copy(selectedTab = tab) }
        fetchReports(tab)
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
            val date = dateFormat.format(Date())

            // Fetch profile in parallel with the initial list of reports
            val profileResult = getUserProfileUseCase()
            val reportsResult = getWasteReportsUseCase(state.value.selectedTab.statusQuery, state.value.selectedTab.assignedToMe)

            profileResult.onSuccess { profile ->
                reportsResult.onSuccess { reports ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            userProfile = profile,
                            currentDate = date,
                            reports = reports
                        )
                    }
                }.onFailure { ex -> _state.update { it.copy(isLoading = false, error = ex.message) } }
            }.onFailure { ex -> _state.update { it.copy(isLoading = false, error = ex.message) } }
        }
    }

    private fun fetchReports(tab: Tab) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getWasteReportsUseCase(tab.statusQuery, tab.assignedToMe)
                .onSuccess { reports ->
                    _state.update { it.copy(isLoading = false, reports = reports) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }
}


