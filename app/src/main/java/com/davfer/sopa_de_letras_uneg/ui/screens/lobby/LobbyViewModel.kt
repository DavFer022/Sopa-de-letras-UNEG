package com.davfer.sopa_de_letras_uneg.ui.screens.lobby

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davfer.sopa_de_letras_uneg.datos.network.SocketManager
import com.davfer.sopa_de_letras_uneg.datos.repositorio.PalabrasRepository
import com.davfer.sopa_de_letras_uneg.dominio.logica.BoardGenerator
import com.davfer.sopa_de_letras_uneg.dominio.models.EstadosJuego
import com.davfer.sopa_de_letras_uneg.dominio.models.GameStatus
import com.davfer.sopa_de_letras_uneg.dominio.models.Jugador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID

class LobbyViewModel : ViewModel() {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val _navigateToGame = MutableStateFlow<String?>(null)
    val navigateToGame = _navigateToGame.asStateFlow()
    private val _players = MutableStateFlow<List<Jugador>>(emptyList())
    val players = _players.asStateFlow()
    private val _generatedRoomId = MutableStateFlow("")
    val generatedRoomId = _generatedRoomId.asStateFlow()

    // --- CONFIGURACIONES DEL HOST ---
    private val _tiempoPorTurno = MutableStateFlow(10)
    val tiempoPorTurno = _tiempoPorTurno.asStateFlow()

    private val _censuraActiva = MutableStateFlow(false)
    val censuraActiva = _censuraActiva.asStateFlow()

    private val _tamannoTablero = MutableStateFlow(10)
    val tamannoTablero = _tamannoTablero.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow("INFORMATICA")
    val categoriaSeleccionada = _categoriaSeleccionada.asStateFlow()

    private val _cantidadPalabras = MutableStateFlow(5)
    val cantidadPalabras = _cantidadPalabras.asStateFlow()

    fun updateConfig(tiempo: Int, censura: Boolean, tamanno: Int, categoria: String, cantidad: Int) {
        _tiempoPorTurno.value = tiempo
        _censuraActiva.value = censura
        _tamannoTablero.value = tamanno
        _categoriaSeleccionada.value = categoria
        _cantidadPalabras.value = cantidad
    }

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
                try {
                    json.decodeFromString<GameStatus>(gameJson)
                    _navigateToGame.value = gameJson
                } catch (e: Exception) {
                    Log.e("APP_DEBUG", "Error al procesar inicio de juego: ${e.message}")
                }
            }
        }

        viewModelScope.launch {
            SocketManager.observePlayers().collect { playersJson ->
                try {
                    val decodedPlayers = json.decodeFromString<List<Jugador>>(playersJson)
                    _players.value = decodedPlayers
                } catch (e: Exception) {
                    Log.e("LobbyViewModel", "Error al deserializar jugadores: ${e.message}")
                }
            }
        }
    }

    fun onJoinClicked(roomId: String, nickname: String) {
        currentRoomId = roomId
        val player = Jugador(id = localPlayerId, nickname = nickname, colorHex = "#FFFFFF")
        if (SocketManager.isConnected()) {
            SocketManager.joinRoom(roomId, json.encodeToString(player), nickname)
        } else {
            Log.e("APP_DEBUG", "Cliente no conectado. Reintentando...")
            SocketManager.connect()
        }
    }

    fun onCreateRoomClicked(nickname: String) {
        if (nickname.isBlank()) return

        if (!SocketManager.isConnected()) {
            Log.e("APP_DEBUG", "Host no conectado al socket. Reintentando conexión...")
            SocketManager.connect()
            // No retornamos, socket.io encolará el evento si la conexión se establece rápido, 
            // pero es mejor avisar al usuario.
            return 
        }

        val newRoomId = UUID.randomUUID().toString().substring(0, 6).uppercase()
        currentRoomId = newRoomId
        _generatedRoomId.value = newRoomId
        val player = Jugador(id = localPlayerId, nickname = nickname, colorHex = "#FFFFFF", isHost = true)
        SocketManager.joinRoom(newRoomId, json.encodeToString(player), nickname)
        Log.d("APP_DEBUG", "Sala creada por Host: $newRoomId")
    }

    fun onStartGameClicked(roomId: String) {
        val generator = BoardGenerator()
        val words = PalabrasRepository.obtenerPalabrasPorCategoria(_categoriaSeleccionada.value, _cantidadPalabras.value)
        val newBoard = generator.generateBoard(_tamannoTablero.value, words)

        val initialGameState = GameStatus(
            roomID = roomId,
            status = EstadosJuego.JUGANDO,
            tablero = newBoard,
            jugadores = _players.value,
            listaPalabras = newBoard.palabras,
            tiempo = 300,
            turnoActual = null,
            tiempoPorTurno = _tiempoPorTurno.value,
            tiempoRestanteTurno = _tiempoPorTurno.value,
            censuraActiva = _censuraActiva.value,
            categoria = _categoriaSeleccionada.value
        )

        SocketManager.startGame(roomId, json.encodeToString(initialGameState))
    }

    fun onNavigationHandled() {
        _navigateToGame.value = null
    }
}
