package com.davfer.sopa_de_letras_uneg.ui.navegacion

sealed class AppScreens(val route: String) {
    object HomeScreen : AppScreens("home_screen")
    object GameScreen : AppScreens("game_screen")
    object LobbyScreen : AppScreens("lobby_screen")
    object ResultScreen : AppScreens("result_screen")
}
