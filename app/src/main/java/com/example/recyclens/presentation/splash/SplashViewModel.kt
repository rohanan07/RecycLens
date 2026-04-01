package com.example.recyclens.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recyclens.domain.use_cases.auth.GetLoggedInUserUseCase
import com.example.recyclens.navigation.RecycLensScreens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val getLoggedInUserUseCase: GetLoggedInUserUseCase // <-- Use the new use case
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            getLoggedInUserUseCase().onSuccess { user ->
                _startDestination.value = when {
                    user == null -> RecycLensScreens.AuthScreen.name // Not logged in
                    user.role.equals("worker", ignoreCase = true) -> RecycLensScreens.WorkerHomeScreen.name // Is a worker
                    else -> RecycLensScreens.HomeScreen.name // Is a citizen
                }
            }.onFailure {
                // On any failure (e.g., bad token, network error), default to the login screen.
                _startDestination.value = RecycLensScreens.AuthScreen.name
            }
        }
    }
}