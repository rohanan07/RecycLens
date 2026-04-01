package com.example.recyclens.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.recyclens.R
import com.example.recyclens.navigation.RecycLensScreens
import com.example.recyclens.ui.theme.Poppins
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = koinViewModel()
) {
    val startDestination by viewModel.startDestination.collectAsState()
    LaunchedEffect(startDestination) {
        startDestination?.let { destination ->
            navController.navigate(destination) {
                // Remove the splash screen from the back stack
                popUpTo(RecycLensScreens.SplashScreen.name) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize().background(color = Color(0xFFFFFFFF)),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.recyclens_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp).clip(shape = RoundedCornerShape(18.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "RecycLens", fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Keep India Clean in Digital Way", fontSize = 14.sp, fontWeight = FontWeight.Normal, fontFamily = Poppins)

        }

        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
            color = Color(0xFF0E4B4D)
        )
    }
}