package com.example.recyclens.presentation.auth


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import com.example.recyclens.R
import com.example.recyclens.navigation.RecycLensScreens

@Composable
fun AuthScreen(
    navController: NavController
) {
    val viewModel: AuthViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    // Handle navigation events safely
    LaunchedEffect(navigationEvent) {
        when (val event = navigationEvent) {
            is AuthNavigationEvent.NavigateToHome -> {
                val destination = if (event.userRole?.trim().equals("worker", ignoreCase = true)) {
                    RecycLensScreens.WorkerHomeScreen.name
                } else {
                    RecycLensScreens.HomeScreen.name
                }
                navController.navigate(destination) {
                    popUpTo(RecycLensScreens.AuthScreen.name) { inclusive = true }
                }
                viewModel.onNavigationComplete()
            }
            is AuthNavigationEvent.NavigateToCreateProfile -> {
                // --- THIS IS THE FIX ---
                // We now call the createRoute helper to build the correct navigation path
                // providing a default of "citizen" if the role is null.
                val role = state.userType.name.lowercase()
                navController.navigate("${RecycLensScreens.CreateProfileScreen.name}/${role}") {
                    popUpTo(RecycLensScreens.AuthScreen.name) { inclusive = true }
                }
                viewModel.onNavigationComplete()
            }
            null -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppLogo()
            Spacer(modifier = Modifier.height(32.dp))

            if (state.userType == UserType.UNSELECTED) {
                InitialSelectionButtons(onUserTypeSelected = viewModel::onUserTypeSelected)
            } else {
                LoginInputSection(
                    state = state,
                    onPhoneNumberChanged = viewModel::onPhoneNumberChanged,
                    onOtpChanged = viewModel::onOtpChanged,
                    onRequestOtp = viewModel::requestOtp,
                    onVerifyOtp = viewModel::verifyOtp,
                    onBack = { viewModel.onUserTypeSelected(UserType.UNSELECTED) },
                    onEditPhoneNumber = viewModel::editPhoneNumber
                )
            }
        }
    }
}


@Composable
private fun AppLogo() {
    // You need to add a logo to your `res/drawable` folder.
    // I'll use a placeholder for now.
    Image(
        painter = painterResource(id = R.drawable.recyclens_logo),
        contentDescription = "App Logo",
        modifier = Modifier.size(100.dp).clip(shape = RoundedCornerShape(18.dp)),
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "RecycLens",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Keep India Clean in Digital way.",
        fontSize = 16.sp,
        color = Color.Gray
    )
}

@Composable
private fun InitialSelectionButtons(
    onUserTypeSelected: (UserType) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = { onUserTypeSelected(UserType.CITIZEN) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E4B4D), contentColor = Color.White)
        ) {
            Text("Continue +91")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("OR")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onUserTypeSelected(UserType.WORKER) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000),contentColor = Color.White)
        ) {
            Text("Continue as Worker")
        }
    }
}

@Composable
private fun LoginInputSection(
    state: AuthState,
    onPhoneNumberChanged: (String) -> Unit,
    onOtpChanged: (String) -> Unit,
    onRequestOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
    onBack: () -> Unit,
    onEditPhoneNumber: () -> Unit
) {
    val title = if (state.userType == UserType.CITIZEN) "Login as Citizen" else "Login as Worker"

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(24.dp))

        if (!state.isOtpSent) {
            // --- STATE 1: Enter Phone Number ---
            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = onPhoneNumberChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter Mobile Number") },
                prefix = { Text("+91 | ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRequestOtp,
                enabled = state.phoneNumber.length == 10 && !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E4B4D), disabledContainerColor = Color(0xFF0E4B4D).copy(alpha = 0.38f))
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Send OTP")
                }
            }
        } else {
            // --- STATE 2: Enter OTP ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+91 ${state.phoneNumber}",
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                TextButton(onClick = onEditPhoneNumber) {
                    Text("Edit", color = Color(0xFF0E4B4D))
                }
            }

            OutlinedTextField(
                value = state.otp,
                onValueChange = onOtpChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter 6-Digit OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onVerifyOtp,
                enabled = state.otp.length == 6 && !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E4B4D), disabledContainerColor = Color(0xFF0E4B4D).copy(alpha = 0.38f))
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Verify OTP")
                }
            }
        }

        AnimatedVisibility(visible = state.error != null) {
            Text(
                text = state.error ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBack) {
            Text("Back", color = Color.Black)
        }
    }
}
