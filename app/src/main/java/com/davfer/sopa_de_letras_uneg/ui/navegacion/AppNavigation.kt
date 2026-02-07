package com.davfer.sopa_de_letras_uneg.ui.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.davfer.sopa_de_letras_uneg.ui.screens.game.GameScreen
import com.davfer.sopa_de_letras_uneg.ui.screens.home.HomeScreen
import com.davfer.sopa_de_letras_uneg.ui.screens.lobby.LobbyScreen
import com.davfer.sopa_de_letras_uneg.ui.screens.results.ResultScreen

@Composable
fun AppNavigation(modifier: Modifier ) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreens.HomeScreen.route) {
        composable(route = AppScreens.HomeScreen.route) {
            HomeScreen(navController)
        }
        composable(route = AppScreens.GameScreen.route) {
            GameScreen(navController = navController, viewModel = viewModel())
        }
        composable(route = AppScreens.LobbyScreen.route) {
            LobbyScreen(navController = navController)
        }
        composable(route = AppScreens.ResultScreen.route + "/{winner}") { backStackEntry ->
            val winner = backStackEntry.arguments?.getString("winner")
            ResultScreen(navController = navController, winner = winner)
        }
    }
}

