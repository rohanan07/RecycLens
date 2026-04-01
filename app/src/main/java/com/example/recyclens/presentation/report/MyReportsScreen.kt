package com.example.recyclens.presentation.report


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.recyclens.domain.model.WasteReportListItem
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(
    navController: NavController,
    viewModel: MyReportsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Reports") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            MyReportsFilterTabs(
                selectedTab = state.selectedTab,
                onTabSelected = viewModel::onTabSelected
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else if (state.error != null) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = state.reports){ report ->
                            MyReportCard(report)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyReportsFilterTabs(selectedTab: MyReportsTab, onTabSelected: (MyReportsTab) -> Unit) {
    val tabs = listOf(MyReportsTab.ALL, MyReportsTab.PENDING, MyReportsTab.CLEARED)
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
                modifier = if (isSelected) Modifier.background(Color(0xFF4CAF50), RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                else Modifier.clip(RoundedCornerShape(50))
            )
        }
    }
}

@Composable
fun MyReportCard(report: WasteReportListItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(report.address, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("ID: #${report.id.take(8)} | ${report.status} ${report.relativeDate}", fontSize = 12.sp, color = Color.Gray)
            }
            StatusChip(status = report.status)
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val backgroundColor = when (status.lowercase()) {
        "cleared" -> Color(0xFF4CAF50) // Green
        "pending", "approved", "assigned" -> Color(0xFFFFA000) // Amber
        "rejected" -> Color(0xFFD32F2F) // Red
        else -> Color.Gray
    }
    Text(
        text = status,
        color = Color.White,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        fontSize = 12.sp
    )
}