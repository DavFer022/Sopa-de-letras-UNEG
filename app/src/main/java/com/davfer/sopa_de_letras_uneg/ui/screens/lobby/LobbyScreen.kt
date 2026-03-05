package com.davfer.sopa_de_letras_uneg.ui.screens.lobby
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davfer.sopa_de_letras_uneg.datos.repositorio.PalabrasRepository
import com.davfer.sopa_de_letras_uneg.ui.navegacion.AppScreens

@OptIn(ExperimentalMaterial3Api::class)
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

    // Estados de configuración
    val tiempoPorTurno by lobbyViewModel.tiempoPorTurno.collectAsState()
    val censuraActiva by lobbyViewModel.censuraActiva.collectAsState()
    val tamannoTablero by lobbyViewModel.tamannoTablero.collectAsState()
    val categoriaSeleccionada by lobbyViewModel.categoriaSeleccionada.collectAsState()
    val cantidadPalabras by lobbyViewModel.cantidadPalabras.collectAsState()

    var isDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(navigateToGame) {
        navigateToGame?.let { gameJson ->
            val encodedJson = Uri.encode(gameJson)
            val roomId = lobbyViewModel.currentRoomId ?: ""
            val playerId = lobbyViewModel.localPlayerId
            navController.navigate("game_screen_multi/$encodedJson/$roomId/$playerId")
            lobbyViewModel.onNavigationHandled()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = if (isHost) "Configurar Sala" else "Unirse a Sala", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Tu Apodo") },
            modifier = Modifier.fillMaxWidth(),
            enabled = players.isEmpty()
        )

        if (isHost && generatedRoomId.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Código: $generatedRoomId", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            
            // --- PERSONALIZACIÓN ---
            Spacer(modifier = Modifier.height(16.dp))
            
            // Categoría
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = categoriaSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    PalabrasRepository.obtenerCategorias().forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                lobbyViewModel.updateConfig(tiempoPorTurno, censuraActiva, tamannoTablero, cat, cantidadPalabras)
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Tamaño del Tablero
            Text("Tamaño del tablero: ${tamannoTablero}x${tamannoTablero}", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = tamannoTablero.toFloat(),
                onValueChange = { 
                    val newSize = it.toInt()
                    val maxWords = (newSize - 3).coerceAtLeast(3)
                    lobbyViewModel.updateConfig(tiempoPorTurno, censuraActiva, newSize, categoriaSeleccionada, cantidadPalabras.coerceAtMost(maxWords))
                },
                valueRange = 8f..15f,
                steps = 7
            )

            // Cantidad de Palabras
            Text("Cantidad de palabras: $cantidadPalabras", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = cantidadPalabras.toFloat(),
                onValueChange = { lobbyViewModel.updateConfig(tiempoPorTurno, censuraActiva, tamannoTablero, categoriaSeleccionada, it.toInt()) },
                valueRange = 3f..(tamannoTablero - 3).toFloat().coerceAtLeast(3f),
                steps = (tamannoTablero - 6).coerceAtLeast(0)
            )

            // Tiempo por turno
            Text("Tiempo por turno: ${tiempoPorTurno}s", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = tiempoPorTurno.toFloat(),
                onValueChange = { lobbyViewModel.updateConfig(it.toInt(), censuraActiva, tamannoTablero, categoriaSeleccionada, cantidadPalabras) },
                valueRange = 5f..30f,
                steps = 5
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ocultar tablero fuera de turno")
                Switch(checked = censuraActiva, onCheckedChange = { lobbyViewModel.updateConfig(tiempoPorTurno, it, tamannoTablero, categoriaSeleccionada, cantidadPalabras) })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isHost) {
            if (generatedRoomId.isEmpty()) {
                Button(onClick = { lobbyViewModel.onCreateRoomClicked(nickname) }, enabled = nickname.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Crear Sala") }
            } else {
                Button(onClick = { lobbyViewModel.onStartGameClicked(generatedRoomId) }, enabled = players.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text("Empezar Juego") }
            }
        } else {
            if (players.isEmpty()) {
                OutlinedTextField(value = roomCodeInput, onValueChange = { roomCodeInput = it }, label = { Text("Código de sala") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { lobbyViewModel.onJoinClicked(roomCodeInput, nickname) }, enabled = nickname.isNotBlank() && roomCodeInput.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Unirse") }
            } else {
                Text(text = "Esperando al Host...", style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (players.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Jugadores (${players.size}):", style = MaterialTheme.typography.titleMedium)
            players.forEach { Text("• ${it.nickname} ${if (it.isHost) "(Host)" else ""}") }
        }
    }
}
