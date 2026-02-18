package com.davfer.sopa_de_letras_uneg.ui.screens.lobby

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davfer.sopa_de_letras_uneg.datos.network.SocketManager
import com.davfer.sopa_de_letras_uneg.dominio.logica.BoardGenerator
import com.davfer.sopa_de_letras_uneg.dominio.models.Jugador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID

class LobbyViewModel : ViewModel() {

    // Estado para navegar al juego cuando el servidor lo diga
    private val _navigateToGame = MutableStateFlow<String?>(null) // String es el JSON del juego
    val navigateToGame = _navigateToGame.asStateFlow()

    private val _players = MutableStateFlow<List<Jugador>>(emptyList())
    val players = _players.asStateFlow()

    // Agrega un estado para guardar el código de sala generado
    private val _generatedRoomId = MutableStateFlow("")
    val generatedRoomId = _generatedRoomId.asStateFlow()

    // Estados para mantener el ID de la sala actual y el ID del jugador local
    var currentRoomId: String? = null
        private set
    var localPlayerId: String = UUID.randomUUID().toString()
        private set

    init {
        SocketManager.connect()
        observeSocketEvents()
    }

    private fun observeSocketEvents() {
        viewModelScope.launch {
            SocketManager.observeGameStart().collect { gameJson ->
                // ¡El juego empezó! Navegar a la pantalla de juego pasando el JSON
                _navigateToGame.value = gameJson
            }
        }

        viewModelScope.launch {
            SocketManager.observePlayers().collect { playersJson ->
                // Parsear JSON a lista de objetos Player y actualizar UI
                try {
                    Log.d("APP_DEBUG", "¡Llegaron jugadores nuevos!")
                    val decodedPlayers = Json.decodeFromString<List<Jugador>>(playersJson)
                    _players.value = decodedPlayers
                } catch (e: Exception) {
                    Log.e("LobbyViewModel", "Error al deserializar jugadores: ${e.message}")
                }
            }
        }
    }

    fun onJoinClicked(roomId: String, nickname: String) {
        currentRoomId = roomId
        Log.d("APP_DEBUG", "Intentando unirse a sala: $roomId con nombre: $nickname")

        val player = Jugador(id = localPlayerId, nickname = nickname, colorHex = "#FFFFFF") // Color temporal


        // Verifica si el socket está conectado antes de enviar
        if (SocketManager.isConnected()) { // Tendrás que añadir esta función al Manager
            Log.d("APP_DEBUG", "Socket conectado. Enviando evento...")
            SocketManager.joinRoom(roomId, Json.encodeToString(player))
        } else {
            Log.e("APP_DEBUG", "ERROR: El socket NO está conectado. El botón no hará nada.")
        }
    }

    fun onCreateRoomClicked(nickname: String) {
        if (nickname.isBlank()) return // Validación básica
        val newRoomId = UUID.randomUUID().toString().substring(0, 6).uppercase() // Generar un ID de sala simple
        val player = Jugador(id = localPlayerId, nickname = nickname, colorHex = "#FFFFFF", isHost = true) // Host
        SocketManager.joinRoom(newRoomId, Json.encodeToString(player))
        Log.d("APP_DEBUG", "Sala creada: $newRoomId por $nickname")
    }

    fun onStartGameClicked(roomId: String) {
        // 1. Generamos el tablero
        val generator = BoardGenerator() // Tu clase generadora existente
        val words = listOf("KOTLIN", "ANDROID", "SOCKET", "GAME") // O palabras al azar
        val newBoard = generator.generateBoard(10, words)

        // 2. Serializamos
        val boardJson = Json.encodeToString(newBoard)

        // 3. Enviamos al servidor
        Log.d("APP_DEBUG", "Iniciando juego en sala $roomId...")
        SocketManager.startGame(roomId, boardJson)
    }

    fun onNavigationHandled() {
        _navigateToGame.value = null
    }
}