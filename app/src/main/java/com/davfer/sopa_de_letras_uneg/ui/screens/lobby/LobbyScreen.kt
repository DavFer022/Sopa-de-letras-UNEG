package com.davfer.sopa_de_letras_uneg.ui.screens.lobby
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.davfer.sopa_de_letras_uneg.ui.navegacion.AppScreens
import com.davfer.sopa_de_letras_uneg.ui.screens.game.viewmodel.GameViewModel
import com.davfer.sopa_de_letras_uneg.ui.screens.lobby.LobbyViewModel

@Composable
fun LobbyScreen(navController: NavController, lobbyViewModel: LobbyViewModel = viewModel()) {
    var roomCode by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") } // Nuevo estado para el nickname
    val players by lobbyViewModel.players.collectAsState()
    val navigateToGame by lobbyViewModel.navigateToGame.collectAsState()

    // Manejar la navegación cuando el juego comience
    LaunchedEffect(navigateToGame) {
        navigateToGame?.let { gameJson ->
            // Reemplazar los placeholders en la ruta con los valores reales
            val route = AppScreens.MultiplayerGameScreen.route
                .replace("{initialGameStateJson}", gameJson)
                .replace("{roomId}", lobbyViewModel.currentRoomId ?: "")
                .replace("{localPlayerId}", lobbyViewModel.localPlayerId)
            navController.navigate(route)
            lobbyViewModel.onNavigationHandled() // Resetear el estado de navegación
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Tu Apodo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                Log.d("LobbyScreen", "$nickname Intentando crear una sala: $roomCode")
                lobbyViewModel.onCreateRoomClicked(nickname) },
            enabled = nickname.isNotBlank()
        ) {
            Text(text = "Crear Sala")
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = roomCode,
            onValueChange = { roomCode = it },
            label = { Text("Código de la sala") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                Log.d("LobbyScreen", "Intentando unirse a la sala: $roomCode")
                lobbyViewModel.onJoinClicked(roomCode, nickname) },
            enabled = roomCode.isNotBlank() && nickname.isNotBlank()
        ) {
            Text(text = "Unirse a Sala")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Jugadores en la sala (${players.size}):")
        players.forEach { player ->
            Text(text = "${player.nickname} (Puntaje: ${player.puntaje})")
        }
    }
}
