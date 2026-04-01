package com.example.recyclens.presentation.report

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.outlined.MyLocation
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.recyclens.navigation.RecycLensScreens
import com.example.recyclens.utils.ComposeFileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ReportWasteScreen(
    navController: NavController,
    viewModel: ReportWasteViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val geocoder = remember(context) { Geocoder(context, Locale.getDefault()) }
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ReportWasteEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    // --- LOCATION LOGIC ---
    @SuppressLint("MissingPermission")
    fun requestLocationUpdate() {
        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                viewModel.onLocationUpdate(location.latitude, location.longitude, geocoder)
            }
        }
    }

    // Handle permissions
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA
        )
    )

    // Request location as soon as permissions are granted
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            requestLocationUpdate()
        }
    }
    LaunchedEffect(state.isReportSubmitted) {
        if (state.isReportSubmitted) {
            navController.navigate(RecycLensScreens.ReportSuccessScreen.name) {
                // Pop up to the home screen to clear the report flow from the back stack
                popUpTo(RecycLensScreens.HomeScreen.name) { inclusive = false }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Waste Site") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.currentStep > 1) viewModel.onPreviousStep()
                        else navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Crossfade(targetState = state.currentStep) { step ->
                when (step) {
                    1 -> Step1_Details(state, viewModel, onAskPermission = {
                        permissionsState.launchMultiplePermissionRequest()
                    })
                    2 -> Step2_Location(state, viewModel, onGetLocation = {
                        if (!permissionsState.allPermissionsGranted) {
                            permissionsState.launchMultiplePermissionRequest()
                        } else {
                            requestLocationUpdate()
                        }
                    })
                    3 -> Step3_Submit(state, viewModel)
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1_Details(state: ReportWasteState, viewModel: ReportWasteViewModel, onAskPermission: () -> Unit) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.onPhotoSelected(uri)
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri?.let { viewModel.onPhotoSelected(it) }
            }
        }
    )

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Choose Image Source") },
            text = { Text("Select a source for your waste photo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        onAskPermission() // Ensure camera permission is requested
                        val uri = ComposeFileProvider.getImageUri(context)
                        imageUri = uri
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


    Column(modifier = Modifier.fillMaxWidth()) {
        Text("1. Take Photo  2. Confirm Location  3. Submit", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        Text("Capture Proof of Dumping", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable { showImageSourceDialog = true }, // This triggers the dialog
            contentAlignment = Alignment.Center
        ) {
            if (state.photoUri != null) {
                AsyncImage(
                    model = state.photoUri,
                    contentDescription = "Selected Waste Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (state.isImageVerifying) {
                    CircularProgressIndicator()
                }
                if (state.isImageVerified) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color.Green,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(32.dp)
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.PhotoCamera, "Upload", modifier = Modifier.size(48.dp))
                    Text("Upload from Gallery / Camera")
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        val wasteTypes = listOf("e_waste", "domestic_waste", "construction_waste", "biomedical_waste", "industrial_waste", "agricultural_waste", "hazardous_waste", "plastic_waste", "litter", "unknown")
        var expanded by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = state.weight,
            onValueChange = viewModel::onWeightChanged,
            label = { Text("Weight unit (Approx. in KG)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = state.wasteType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Waste type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                wasteTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            viewModel.onWasteTypeChanged(type.lowercase())
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = viewModel::onNextStep,
            enabled = state.isImageVerified && state.weight.isNotBlank() && state.wasteType.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Confirm Location")
        }
    }
}

@Composable
fun Step2_Location(state: ReportWasteState, viewModel: ReportWasteViewModel, onGetLocation: () -> Unit) {
    val initialLocation = LatLng(18.5204, 73.8567) // Default to Pune
    val geocoder = Geocoder(LocalContext.current, Locale.getDefault())
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 15f)
    }

    // This effect moves the map camera to the user's fetched location once
    LaunchedEffect(state.latitude, state.longitude) {
        if (state.latitude != 0.0 && state.longitude != 0.0) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(state.latitude, state.longitude), 17f)
        }
    }

    // This effect listens for when the user stops moving the map and updates the address
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(key1 = Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            snapshotFlow { cameraPositionState.isMoving }.collectLatest { isMoving ->
                if (!isMoving) {
                    val newTarget = cameraPositionState.position.target
                    viewModel.onLocationUpdate(newTarget.latitude, newTarget.longitude, geocoder)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("1. Take Photo  2. Confirm Location  3. Submit", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        Text("Refine GPS Location", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        // --- UPDATED MAP VIEW ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Increased height for better interaction
                .clip(RoundedCornerShape(12.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            )
            // Center marker icon that stays in the middle while the map moves
            Icon(
                imageVector = Icons.Default.PinDrop,
                contentDescription = "Selected Location Pin",
                modifier = Modifier.align(Alignment.Center).size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
        // --- END OF UPDATE ---

        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onGetLocation) {
            Icon(Icons.Outlined.MyLocation, contentDescription = "Get Location", modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Get Current Location")
        }

        Spacer(Modifier.height(16.dp))
        Text("CONFIRMED ADDRESS", fontWeight = FontWeight.Bold)
        Text(state.address)
        Text("GPS Coordinates: ${"%.4f".format(state.latitude)}, ${"%.4f".format(state.longitude)}", style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.weight(1f))
        Button(
            onClick = viewModel::onNextStep,
            enabled = state.address != "Fetching address..." && state.address != "Could not fetch address",
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Final Step: Submit")
        }
    }
}


@Composable
fun Step3_Submit(state: ReportWasteState, viewModel: ReportWasteViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("1. Take Photo  2. Confirm Location  3. Submit", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        Text("Report Details", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, "Photo Authenticity", tint = Color.Green)
                Spacer(Modifier.width(8.dp))
                Text("Photo Authenticity")
            }
            AsyncImage(model = state.photoUri, contentDescription = "Waste Preview", modifier = Modifier.fillMaxWidth().height(150.dp), contentScale = ContentScale.Crop)
        }
        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("GPS Location", fontWeight = FontWeight.Bold)
                Text(state.address)
                Text("GPS Coordinates: ${state.latitude}, ${state.longitude}", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.weight(1f))
        Text("By clicking 'Submit Report', you confirm the information is accurate and non-spam.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = viewModel::submitReport,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("SUBMIT REPORT")
            }
        }
    }
}