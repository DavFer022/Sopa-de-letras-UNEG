package com.davfer.sopa_de_letras_uneg.ui.navegacion


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.davfer.sopa_de_letras_uneg.ui.screens.game.GameScreen
import com.davfer.sopa_de_letras_uneg.ui.screens.results.ResultScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.davfer.sopa_de_letras_uneg.ui.screens.home.InstructionsScreen
import com.davfer.sopa_de_letras_uneg.ui.screens.game.RoleSelectionScreen
import com.davfer.sopa_de_letras_uneg.ui.screens.game.viewmodel.GameViewModel
import com.davfer.sopa_de_letras_uneg.ui.screens.home.HomeScreen
import com.davfer.sopa_de_letras_uneg.ui.screens.lobby.LobbyScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreens.HomeScreen.route) {
        composable(route = AppScreens.HomeScreen.route) {
            HomeScreen(
                onNavigateToSolo = { navController.navigate(AppScreens.SinglePlayerGameScreen.route) },
                onNavigateToMulti = { navController.navigate(AppScreens.RoleSelectionScreen.route) },
                onNavigateToHowTo = { navController.navigate(AppScreens.InstructionsScreen.route) }
            )
        }
        composable(route = AppScreens.InstructionsScreen.route) {
            InstructionsScreen(onBack = { navController.popBackStack() })
        }
        composable(route = AppScreens.RoleSelectionScreen.route) {
            RoleSelectionScreen(
                onRoleSelected = { isHost ->
                    navController.navigate(AppScreens.LobbyScreen.route.replace("{isHost}", isHost.toString()))
                }
            )
        }
        composable(route = AppScreens.SinglePlayerGameScreen.route) {
            GameScreen(navController = navController, viewModel = viewModel()) // Llama sin argumentos
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

            if (initialGameStateJson != null && roomId != null && localPlayerId != null) {
                GameScreen(
                    navController = navController,
                    viewModel = viewModel(
                        factory = GameViewModel.provideFactory(
                            initialGameStateJson = initialGameStateJson,
                            roomId = roomId,
                            localPlayerId = localPlayerId
                        )
                    )
                )
            } else {
                // Manejar el caso de error, por ejemplo, volver al lobby.
                navController.popBackStack()
            }
        }
        composable(
            route = AppScreens.LobbyScreen.route,
            arguments = listOf(navArgument("isHost") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isHost = backStackEntry.arguments?.getBoolean("isHost") ?: false
            LobbyScreen(navController = navController, isHost = isHost)
        }
        composable(route = AppScreens.ResultScreen.route + "/{winner}") { backStackEntry ->
            val winner = backStackEntry.arguments?.getString("winner")
            ResultScreen(navController = navController, winner = winner)
        }
    }
}

