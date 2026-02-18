package com.davfer.sopa_de_letras_uneg.ui.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.davfer.sopa_de_letras_uneg.ui.screens.game.GameScreen
import com.davfer.sopa_de_letras_uneg.ui.screens.home.HomeScreen
import com.davfer.sopa_de_letras_uneg.ui.screens.results.ResultScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.davfer.sopa_de_letras_uneg.ui.navegacion.AppScreens
import com.davfer.sopa_de_letras_uneg.ui.screens.game.viewmodel.GameViewModel
import com.davfer.sopa_de_letras_uneg.ui.screens.lobby.LobbyScreen

@Composable
fun AppNavigation(modifier: Modifier ) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreens.HomeScreen.route) {
        composable(route = AppScreens.HomeScreen.route) {
            HomeScreen(navController)
        }
        composable(route = AppScreens.SinglePlayerGameScreen.route) {
            GameScreen(navController = navController, viewModel = GameViewModel()) // Llama sin argumentos
        }
        composable(
            route = AppScreens.MultiplayerGameScreen.route,
            arguments = listOf(
                navArgument("initialGameStateJson") { type = NavType.StringType },
                navArgument("roomId") { type = NavType.StringType },
                navArgument("localPlayerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val initialGameStateJson = backStackEntry.arguments?.getString("initialGameStateJson")
            val roomId = backStackEntry.arguments?.getString("roomId")
            val localPlayerId = backStackEntry.arguments?.getString("localPlayerId")

            // Asegurarse de que los argumentos no son nulos para el modo multijugador
            if (initialGameStateJson != null && roomId != null && localPlayerId != null) {
                GameScreen(
                    navController = navController,
                    viewModel = GameViewModel(
                        initialGameStateJson = initialGameStateJson,
                        roomId = roomId,
                        localPlayerId = localPlayerId
                    )
                )
            } else {
                // Opcional: Manejar el caso de error, por ejemplo, volver al lobby.
                // Por ahora, simplemente no se carga la pantalla de juego.
            }
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

