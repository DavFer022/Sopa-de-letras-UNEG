package com.davfer.sopa_de_letras_uneg.ui.screens.lobby
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
fun LobbyScreen(
    navController: NavController,
    isHost: Boolean,
    lobbyViewModel: LobbyViewModel = viewModel()
) {
    var roomCodeInput by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    val players by lobbyViewModel.players.collectAsState()
    val navigateToGame by lobbyViewModel.navigateToGame.collectAsState()
    val generatedRoomId by lobbyViewModel.generatedRoomId.collectAsState()

    // Manejar la navegación cuando el juego comience
    LaunchedEffect(navigateToGame) {
        navigateToGame?.let { gameJson ->
            // 1. CODIFICAMOS el JSON para que sea seguro en la URL
            val encodedJson = Uri.encode(gameJson)
            // 2. Verificamos que los IDs no sean nulos
            val roomId = lobbyViewModel.currentRoomId ?: ""
            val playerId = lobbyViewModel.localPlayerId
            // 3. Construimos la ruta
            val route = "game_screen_multi/$encodedJson/$roomId/$playerId"

           /* val route = AppScreens.MultiplayerGameScreen.route
                .replace("{initialGameStateJson}", encodedJson)
                .replace("{roomId}", roomId)
                .replace("{localPlayerId}", playerId)*/
            Log.d("APP_DEBUG", "Navegando a la ruta: $route")
            navController.navigate(route)
            lobbyViewModel.onNavigationHandled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isHost) "Configurar Sala" else "Unirse a Sala",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Tu Apodo") },
            modifier = Modifier.fillMaxWidth(),
            enabled = players.isEmpty() // Bloquear si ya se unió/creó
        )

        if (isHost) {
            if (generatedRoomId.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Código de la sala: $generatedRoomId",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (generatedRoomId.isEmpty()) {
                Button(
                    onClick = { lobbyViewModel.onCreateRoomClicked(nickname) },
                    enabled = nickname.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear Sala")
                }
            } else {
                Button(
                    onClick = { lobbyViewModel.onStartGameClicked(generatedRoomId) },
                    enabled = players.size >= 1, // Puedes exigir más jugadores si quieres
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Empezar Juego")
                }
            }
        } else {
            // CLIENTE
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = roomCodeInput,
                onValueChange = { roomCodeInput = it },
                label = { Text("Código de la sala") },
                modifier = Modifier.fillMaxWidth(),
                enabled = players.isEmpty()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { lobbyViewModel.onJoinClicked(roomCodeInput, nickname) },
                enabled = nickname.isNotBlank() && roomCodeInput.isNotBlank() && players.isEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unirse")
            }
            
            if (players.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Esperando a que el Host inicie...", style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (players.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Jugadores en la sala (${players.size}):", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            players.forEach { player ->
                Text(text = "• ${player.nickname} ${if (player.isHost) "(Host)" else ""}")
            }
        }
    }
}
