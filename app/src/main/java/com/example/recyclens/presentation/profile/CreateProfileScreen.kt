package com.example.recyclens.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.recyclens.navigation.RecycLensScreens
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProfileScreen(
    navController: NavController,
    viewModel: CreateProfileViewModel = koinViewModel(),
    userRole: String,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isProfileCreated) {
        if (state.isProfileCreated) {
            // --- THIS IS THE FIX ---
            // Use the userRole to decide the final destination
            val destination = if (userRole.equals("worker", ignoreCase = true)) {
                RecycLensScreens.WorkerHomeScreen.name
            } else {
                RecycLensScreens.HomeScreen.name
            }
            navController.navigate(destination) {
                // Clear the entire back stack
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(modifier = Modifier.statusBarsPadding()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))
            Text(
                "Take a moment to fill your profile details",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            StepIndicator(currentStep = state.currentStep)

            Spacer(modifier = Modifier.height(32.dp))

            when (state.currentStep) {
                1 -> NameStep(
                    name = state.name,
                    onNameChanged = viewModel::onNameChanged,
                    onNext = viewModel::onNextStep
                )
                2 -> AddressStep(
                    address = state.address,
                    onAddressChanged = viewModel::onAddressChanged,
                    onNext = viewModel::onNextStep
                )
                3 -> PhotoStep(
                    photoUri = state.photoUri,
                    onPhotoSelected = viewModel::onPhotoSelected,
                    onSubmit = viewModel::submitProfile,
                    isLoading = state.isLoading
                )
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int) {
    val progress by animateFloatAsState(targetValue = (currentStep - 1) / 2f, label = "progress")
    LinearProgressIndicator(
    progress = { progress },
    modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
    color = ProgressIndicatorDefaults.linearColor,
    trackColor = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
    )
}

@Composable
fun NameStep(name: String, onNameChanged: (String) -> Unit, onNext: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Step 1: What should we call you?", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNext,
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E4B4D), contentColor = Color.White)
        ) {
            Text("Next")
        }
    }
}

@Composable
fun AddressStep(address: String, onAddressChanged: (String) -> Unit, onNext: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Step 2: Where are you located?", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChanged,
            label = { Text("Full Address") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNext, enabled = address.isNotBlank(),modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E4B4D), contentColor = Color.White)) {
            Text("Next")
        }
    }
}

@Composable
fun PhotoStep(
    photoUri: Uri?,
    onPhotoSelected: (Uri?) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onPhotoSelected(uri)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Step 3: Add a profile photo (Optional)", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = "Upload Photo",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onSubmit, enabled = !isLoading,modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E4B4D), contentColor = Color.White)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Finish & Submit")
            }
        }
    }
}