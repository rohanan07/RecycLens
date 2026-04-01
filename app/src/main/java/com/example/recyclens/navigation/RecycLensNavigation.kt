package com.example.recyclens.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recyclens.presentation.auth.AuthScreen
import com.example.recyclens.presentation.home.HomeScreen
import com.example.recyclens.presentation.home.WorkerHomeScreen
import com.example.recyclens.presentation.notifications.NotificationScreen
import com.example.recyclens.presentation.profile.CreateProfileScreen
import com.example.recyclens.presentation.report.MyReportsScreen
import com.example.recyclens.presentation.report.ReportDetailScreen
import com.example.recyclens.presentation.report.ReportSuccessScreen
import com.example.recyclens.presentation.report.ReportWasteScreen
import com.example.recyclens.presentation.splash.SplashScreen

@Composable
fun RecycLensNavigation(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = RecycLensScreens.SplashScreen.name ){
        composable(RecycLensScreens.SplashScreen.name) {
            SplashScreen(navController)
        }
        composable(RecycLensScreens.AuthScreen.name) {
            AuthScreen(navController)
        }
        composable(RecycLensScreens.HomeScreen.name) {
            HomeScreen(navController)
        }
        composable(RecycLensScreens.NotificationScreen.name) {
            NotificationScreen()
        }
        composable(
            route = "${RecycLensScreens.CreateProfileScreen.name}/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "citizen"
            CreateProfileScreen(navController = navController, userRole = role)
        }
        composable(RecycLensScreens.ReportWasteScreen.name) {
            ReportWasteScreen(navController)
        }
        composable(RecycLensScreens.WorkerHomeScreen.name) {
            WorkerHomeScreen(navController)
        }
        composable(
            "${RecycLensScreens.ReportDetailScreen.name}/{reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
            ReportDetailScreen(navController, reportId = reportId)
        }
        composable(RecycLensScreens.MyReportsScreen.name) {
            MyReportsScreen(navController)
        }
        composable(RecycLensScreens.ReportSuccessScreen.name) {
            ReportSuccessScreen(navController)
        }
    }

}