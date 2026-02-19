package com.davfer.sopa_de_letras_uneg.ui.navegacion

sealed class AppScreens(val route: String) {
    object HomeScreen : AppScreens("home_screen")
    object InstructionsScreen : AppScreens("instructions_screen")
    object RoleSelectionScreen : AppScreens("role_selection_screen")
    object SinglePlayerGameScreen : AppScreens("game_screen_single") // Ruta para un jugador
    object MultiplayerGameScreen : AppScreens("game_screen_multi/{initialGameStateJson}/{roomId}/{localPlayerId}") // Ruta para multijugador
    object LobbyScreen : AppScreens("lobby_screen/{isHost}")
    object ResultScreen : AppScreens("result_screen")
}
