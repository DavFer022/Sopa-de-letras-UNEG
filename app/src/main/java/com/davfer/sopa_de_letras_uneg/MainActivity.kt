package com.davfer.sopa_de_letras_uneg

import HomeScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.davfer.sopa_de_letras_uneg.ui.screens.game.GameScreen
import com.davfer.sopa_de_letras_uneg.ui.screens.game.InstructionsScreen
import com.davfer.sopa_de_letras_uneg.ui.screens.game.RoleSelectionScreen
import com.davfer.sopa_de_letras_uneg.ui.screens.game.viewmodel.GameViewModel
import com.davfer.sopa_de_letras_uneg.ui.navegacion.AppNavigation
import com.davfer.sopa_de_letras_uneg.ui.theme.Sopa_de_letras_UNEGTheme

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Instructions : Screen("instructions")
    object RoleSelection : Screen("role_selection")
    object Game : Screen("game")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Sopa_de_letras_UNEGTheme {
               Scaffold(modifier = Modifier.fillMaxSize()) {innerPadding->
                   AppNavigation( modifier = Modifier.padding(innerPadding))
               }
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route // Pantalla inicial
                ) {
                    // Ruta: Inicio
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onNavigateToJugar = { navController.navigate(Screen.RoleSelection.route) },
                            onNavigateToHowTo = { navController.navigate(Screen.Instructions.route) }
                        )
                    }

                    // Ruta: Instrucciones
                    composable(Screen.Instructions.route) {
                        InstructionsScreen(onBack = { navController.popBackStack() })
                    }

                    // Ruta: Selección de Rol
                    composable(Screen.RoleSelection.route) {
                        RoleSelectionScreen(
                            onNavigateToGame = { isServer ->
                                navController.navigate(Screen.Game.route)
                            }
                        )
                    }

                    // Ruta:  Juego
                    composable(Screen.Game.route) {
                        val gameViewModel: GameViewModel = viewModel()
                        GameScreen(viewModel = gameViewModel)
                    }
                }
            }

            }
        }
    }

