package com.example.recyclens.presentation.home


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.recyclens.domain.model.WasteReportListItem
import com.example.recyclens.navigation.RecycLensScreens
import com.example.recyclens.ui.theme.Poppins
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerHomeScreen(
    navController: NavController,
    viewModel: WorkerHomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(topBar = {WorkerHomeTopAppBar(navController)}) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // --- Personalized Header ---
            if (state.userProfile != null) {
                Text("Welcome Back!", style = MaterialTheme.typography.titleMedium)
                ProfileSection(
                    name = state.userProfile!!.name ?: "Worker",
                    date = state.currentDate,
                    imageUrl = state.userProfile!!.profileImageUrl
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            TaskFilterTabs(
                selectedTab = state.selectedTab,
                onTabSelected = viewModel::onTabSelected
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else if (state.error != null) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                } else if (state.reports.isEmpty()) {
                    Text("No tasks found for this category.")
                }
                else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.reports,
                            key = { it.id }
                        ) { report ->
                            ReportTaskCard(report = report, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskFilterTabs(selectedTab: Tab, onTabSelected: (Tab) -> Unit) {
    val tabs = listOf(Tab.APPROVED, Tab.MY_TASKS, Tab.DONE)
    TabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {}
    ) {
        tabs.forEach { tab ->
            val isSelected = selectedTab == tab
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                text = { Text(tab.displayName) },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.Gray,
                modifier = if (isSelected) Modifier
                    .padding(horizontal = 4.dp)
                    .background(
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(50)
                    )
                    .clip(RoundedCornerShape(50))
                else Modifier
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(50))
            )
        }
    }
}

@Composable
fun ReportTaskCard(report: WasteReportListItem, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable{
            navController.navigate("${RecycLensScreens.ReportDetailScreen.name}/${report.id}")
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(report.address, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "ID: #${report.id.take(8)} | ${report.status} ${report.relativeDate}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { /* TODO: Navigate to map/details */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Go With")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerHomeTopAppBar(navController: NavController) {
    TopAppBar(
        title = {
            Text(text = "RecycLens", fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = Poppins)
        },
        navigationIcon = {
            IconButton(onClick = {}) {
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