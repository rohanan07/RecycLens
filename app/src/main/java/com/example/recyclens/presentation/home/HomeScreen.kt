package com.example.recyclens.presentation.home

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.recyclens.R
import com.example.recyclens.navigation.RecycLensScreens
import com.example.recyclens.ui.theme.Poppins
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    val permissionsState = rememberMultiplePermissionsState(permissions = permissionsToRequest)

    // This effect will trigger the permission request if the ViewModel says so.
    LaunchedEffect(state.shouldRequestPermissions) {
        if (state.shouldRequestPermissions) {
            permissionsState.launchMultiplePermissionRequest()
            // We immediately mark it as handled so it doesn't run again on configuration change.
            viewModel.onPermissionsRequested()
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.60f)) {
                Column(modifier = Modifier.fillMaxHeight()) {
                    // Drawer Header
                    if (state.userProfile != null) {
                        DrawerHeader(
                            name = state.userProfile!!.name?: "",
                            imageUrl = state.userProfile!!.profileImageUrl
                        )
                    }
                    Divider()
                    // Drawer Items
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "My Profile") },
                        label = { Text("My Profile") },
                        selected = false,
                        onClick = { /* TODO: Navigate to Profile Screen */ }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.Description, contentDescription = "My Reports") },
                        label = { Text("My Reports") },
                        selected = false,
                        onClick = { /* TODO: Navigate to My Reports Screen */ }
                    )
                    Spacer(modifier = Modifier.weight(1f)) // Pushes logout to the bottom
                    // Logout Item
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.Logout, contentDescription = "Logout") },
                        label = { Text("Logout") },
                        selected = false,
                        onClick = {
                            viewModel.onLogoutClicked()
                            navController.navigate(RecycLensScreens.AuthScreen.name) {
                                popUpTo(0) // Clear entire back stack
                            }
                        }
                    )
                }
            }
        }
    ){
    Scaffold(topBar = {
        HomeTopAppBar(
            navController = navController,
            onMenuClick = {
                scope.launch {
                    drawerState.open()
                }
            })
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    Text(
                        text = "Error: ${state.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.userProfile != null -> {
                    HomeScreenContent(state = state, navController = navController)
                }
            }
        }
    }
    }
}

@Composable
fun DrawerHeader(name: String, imageUrl: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .error(R.drawable.coin)
                .placeholder(R.drawable.recyclens_logo)
                .build(),
            contentDescription = "Profile Photo",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HomeScreenContent(state: HomeScreenState, navController: NavController) {
    // This dummy data should be moved to the ViewModel
    val stats = DashboardStats(
        todaysReports = 0,
        rejected = 0,
        approved = 0
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Welcome Back!",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        ProfileSection(
            name = state.userProfile!!.name?: "",
            date = state.currentDate,
            imageUrl = state.userProfile.profileImageUrl
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Your report creates a verified action.",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        ReportWasteCard(onClick = { navController.navigate(route = RecycLensScreens.ReportWasteScreen.name)})
        Spacer(modifier = Modifier.height(20.dp))
        CityAccountabilityDashboard(stats)
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.blank_page, // Using placeholder
                title = "My Reports",
                subtitle = "${state.pendingReportsCount} Pending",
                onClick = { navController.navigate(RecycLensScreens.MyReportsScreen.name) }
            )
            ActionCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.coin,
                title = "Citizen Reward",
                subtitle = "30 pts",
                onClick = { /* TODO: Show coming soon message */ }
            )
        }
    }
}

@Composable
fun ProfileSection(name: String, date: String, imageUrl: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .error(R.drawable.ic_launcher_foreground) // Fallback image
                .placeholder(R.drawable.ic_launcher_foreground)
                .build(),
            contentDescription = "Profile Photo",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = date, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ReportWasteCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0E4B4D) // A shade of green
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.PhotoCamera,
                contentDescription = "Report Waste",
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "REPORT WASTE",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "AI verification + GPS Pin. Zero spam.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    title: String,
    subtitle: String,
    tag: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            //.aspectRatio(1f) // Makes the card a square
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        //colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        shadowElevation = 2.dp
    ) {
        Box() {
            tag?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
            Column(
                modifier = Modifier
                  //  .fillMaxSize()
                    .padding(16.dp),
                //verticalArrangement = Arrangement.Bottom
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun CityAccountabilityDashboard(stats: DashboardStats) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        //colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "City Accountability Dashboard",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(count = stats.todaysReports, label = "Today's reports", color = Color(0xFF4CAF50))
                StatItem(count = stats.approved, label = "Approved", color = MaterialTheme.colorScheme.onSurface)
                StatItem(count = stats.rejected, label = "Rejected", color = Color(0xFFE53935))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Data Source: Municipal Authority Pune",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun StatItem(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(navController: NavController, onMenuClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = "RecycLens", fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = Poppins)
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF607D8B))
            }
        },
        actions = {
            Icon(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(24.dp)
                    .clickable(onClick = { navController.navigate(RecycLensScreens.NotificationScreen.name) }),
                imageVector = Icons.Default.Notifications,
                contentDescription = "notifications",
                tint = Color(0xFF000000)
            )
        }
    )
}