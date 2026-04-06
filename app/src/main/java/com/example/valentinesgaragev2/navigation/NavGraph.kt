// navigation/NavGraph.kt
package com.example.valentinesgaragev2.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.valentinesgaragev2.screens.*

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") {
            MainMenu(navController)
        }

        composable("checkin") {
            CheckInScreen(navController)
        }

        composable("trucks") {
            TruckListScreen(navController)
        }

        composable("reports") {
            ReportsScreen(navController)
        }

        composable("truck_management") {
            TruckManagementScreen(navController)
        }

        composable(
            "checkin_detail/{checkInId}",
            arguments = listOf(navArgument("checkInId") { type = NavType.LongType })
        ) { backStackEntry ->
            val checkInId = backStackEntry.arguments?.getLong("checkInId") ?: 0L
            CheckInDetailScreen(navController, checkInId)
        }
    }
}