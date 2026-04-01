package com.example.recyclens.presentation.report

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.recyclens.utils.ComposeFileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ReportDetailScreen(
    navController: NavController,
    reportId: String,
    viewModel: ReportDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
// Add camera permission to the list of permissions to request.
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA
            // Location permissions are not needed on this screen
        )
    )
    LaunchedEffect(state.taskAccepted, state.taskCleaned) {
        if (state.taskAccepted || state.taskCleaned) {
            // Navigate back to the worker home screen after accepting or cleaning
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(
                    text = "Error: ${state.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.report != null) {
                val report = state.report!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    AsyncImage(
                        model = report.imageUrl,
                        contentDescription = "Waste Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Location", style = MaterialTheme.typography.titleLarge)
                    Text(report.address, fontSize = 18.sp)
                    Text("Lat: ${report.latitude}, Lng: ${report.longitude}", fontSize = 12.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    val reportLocation = LatLng(report.latitude, report.longitude)
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(reportLocation, 16f)
                    }
                    val markerState = rememberMarkerState(position = reportLocation)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState
                        ) {
                            Marker(
                                state = markerState,
                                title = "Waste Location",
                                snippet = report.address
                            )
                        }
                        // Button to open in the native Google Maps app
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailItem(label = "Reported By", value = report.reportedBy)
                    DetailItem(label = "Reported At", value = report.reportedAt)
                    DetailItem(label = "Waste Type", value = report.wasteType)
                    DetailItem(label = "Approx. Weight", value = report.weight)
                    DetailItem(label = "Current Status", value = report.status, isStatus = true)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Conditionally show the correct action button based on the report's status
                    when (report.status.lowercase()) {
                        "approved" -> {
                            Button(
                                onClick = viewModel::onAcceptTask,
                                enabled = !state.isAccepting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                if (state.isAccepting) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else {
                                    Text("Accept Task")
                                }
                            }
                        }
                        "assigned" -> {
                            MarkAsCleanedSection(
                                cleanedPhotoUri = state.cleanedPhotoUri,
                                onPhotoSelected = viewModel::onCleanedPhotoSelected,
                                onSubmit = viewModel::onMarkAsCleaned,
                                isLoading = state.isCleaning,
                                onAskPermission = { permissionsState.launchMultiplePermissionRequest() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarkAsCleanedSection(
    cleanedPhotoUri: Uri?,
    onPhotoSelected: (Uri?) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean,
    onAskPermission: () -> Unit // Receive the permission launcher
) {
    val context = LocalContext.current
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onPhotoSelected(uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { onPhotoSelected(it) }
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Choose Image Source") },
            text = { Text("Select a source for the cleaned site photo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        onAskPermission() // Request camera permission
                        val uri = ComposeFileProvider.getImageUri(context)
                        cameraImageUri = uri
                        cameraLauncher.launch(uri)
                    }
                ) {
                    Text("Camera")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Text("Gallery")
                }
            }
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Upload Proof of Cleaning", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                .clickable { showImageSourceDialog = true }, // Trigger the dialog
            contentAlignment = Alignment.Center
        ) {
            if (cleanedPhotoUri != null) {
                AsyncImage(
                    model = cleanedPhotoUri,
                    contentDescription = "Cleaned Area Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.PhotoCamera, "Upload", modifier = Modifier.size(48.dp))
                    Text("Upload Photo of Cleaned Site")
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            enabled = !isLoading && cleanedPhotoUri != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Submit & Mark as Cleaned")
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, isStatus: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$label:", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f))
        Text(value, modifier = Modifier.weight(0.6f), color = if (isStatus) Color.Blue else Color.Unspecified)
    }
}