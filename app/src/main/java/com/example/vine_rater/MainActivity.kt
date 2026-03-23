package com.example.vine_rater

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vine_rater.ui.*
import com.example.vine_rater.ui.theme.VineraterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VineraterTheme {
                WineApp()
            }
        }
    }
}

@Composable
fun WineApp() {
    val navController = rememberNavController()
    val viewModel: WineViewModel = viewModel(factory = WineViewModel.Factory)

    NavHost(navController = navController, startDestination = "wine_list") {
        composable("wine_list") {
            WineListScreen(
                viewModel = viewModel,
                onAddWineClick = { navController.navigate("add_wine") },
                onWineClick = { wine -> navController.navigate("wine_detail/${wine.id}") }
            )
        }
        composable(
            route = "add_wine?wineId={wineId}",
            arguments = listOf(navArgument("wineId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null 
            })
        ) { backStackEntry ->
            val wineId = backStackEntry.arguments?.getString("wineId")?.toIntOrNull()
            AddWineScreen(
                wineId = wineId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "wine_detail/{wineId}",
            arguments = listOf(navArgument("wineId") { type = NavType.IntType })
        ) { backStackEntry ->
            val wineId = backStackEntry.arguments?.getInt("wineId") ?: return@composable
            WineDetailScreen(
                wineId = wineId,
                viewModel = viewModel,
                onEditClick = { id -> navController.navigate("add_wine?wineId=$id") },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
