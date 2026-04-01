package com.example.recyclens.presentation.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recyclens.domain.model.WasteReportListItem
import com.example.recyclens.domain.use_cases.report.GetMyReportsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class MyReportsTab(val displayName: String, val statusQuery: String?) {
    object ALL : MyReportsTab("All", null)
    object PENDING : MyReportsTab("Pending", "pending")
    object CLEARED : MyReportsTab("Cleared", "cleaned")
}

data class MyReportsState(
    val isLoading: Boolean = true,
    val reports: List<WasteReportListItem> = emptyList(),
    val error: String? = null,
    val selectedTab: MyReportsTab = MyReportsTab.ALL
)

class MyReportsViewModel(
    private val getMyReportsUseCase: GetMyReportsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyReportsState())
    val state = _state.asStateFlow()

    init {
        fetchMyReports(MyReportsTab.ALL.statusQuery)
    }

    fun onTabSelected(tab: MyReportsTab) {
        _state.update { it.copy(selectedTab = tab) }
        fetchMyReports(tab.statusQuery)
    }

    private fun fetchMyReports(status: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getMyReportsUseCase(status)
                .onSuccess { reports ->
                    _state.update { it.copy(isLoading = false, reports = reports) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }
}