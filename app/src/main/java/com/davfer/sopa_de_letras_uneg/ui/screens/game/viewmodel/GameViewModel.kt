package com.davfer.sopa_de_letras_uneg.ui.screens.game.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.davfer.sopa_de_letras_uneg.datos.network.SocketManager
import com.davfer.sopa_de_letras_uneg.datos.repositorio.PalabrasRepository
import com.davfer.sopa_de_letras_uneg.dominio.logica.BoardGenerator
import com.davfer.sopa_de_letras_uneg.dominio.models.Coordenada
import com.davfer.sopa_de_letras_uneg.dominio.models.EstadosJuego
import com.davfer.sopa_de_letras_uneg.dominio.models.GameStatus
import com.davfer.sopa_de_letras_uneg.dominio.models.Jugador
import com.davfer.sopa_de_letras_uneg.dominio.models.Palabra
import com.davfer.sopa_de_letras_uneg.dominio.models.Tablero
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.math.max

class GameViewModel(
    // Parametros para multijugador (opcionales)
    private val initialGameStateJson: String? = null,
    private val roomId: String? = null,
    private val localPlayerId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameStatus())
    val uiState: StateFlow<GameStatus> = _uiState.asStateFlow()

    // Dependencia para modo de un jugador
    private val generator = BoardGenerator()
    private var timerJob: Job? = null
    private var dragStartCoordinate: Coordenada? = null

    init {
        if (initialGameStateJson != null && roomId != null && localPlayerId != null) {
            // --- MODO MULTIJUGADOR ---
            initializeMultiplayerGame(initialGameStateJson)
        } else {
            // --- MODO UN JUGADOR ---
            startSinglePlayerGame()
        }
    }

    companion object {
        fun provideFactory(
            initialGameStateJson: String? = null,
            roomId: String? = null,
            localPlayerId: String? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                    return GameViewModel(initialGameStateJson, roomId, localPlayerId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    // --- LÓGICA DE INICIALIZACIÓN ---

    private fun initializeMultiplayerGame(gameStateJson: String) {
        try {
            // 1. Decodificamos el ESTADO COMPLETO,
            val serverState = Json.decodeFromString<GameStatus>(gameStateJson)
            // 2. Aplicamos el estado directamente a la UI
            // Asegúrate de copiar el status como JUGANDO si no viene así
            _uiState.value = serverState.copy(
                status = EstadosJuego.JUGANDO,
                roomID = roomId ?: serverState.roomID // Priorizar el ID que viene de la ruta
            )
            Log.d("GameViewModel", "Juego Multijugador iniciado con éxito. Turno de: ${serverState.turnoActual}")

            observeServerUpdates()
            startTimer()
        } catch (e: Exception) {
            Log.e("APP_DEBUG_GameViewModel", "Error al deserializar estado multijugador: ${e.message}")
            // Considerar un estado de error
        }
    }

    private fun startSinglePlayerGame() {
        _uiState.value = GameStatus(status = EstadosJuego.CARGANDO)
        viewModelScope.launch {
            //val words = listOf("KOTLIN", "ANDROID", "COMPOSE", "SOCKET", "VIEWMODEL")
            val words = PalabrasRepository.obtenerPalabrasAleatorias(5)
            val newBoard = generator.generateBoard(10, words)
            val localPlayer = Jugador(
                id = "single_player",
                nickname = "Tú",
                colorHex = "#4CAF50",
                isHost = true
            )
            _uiState.update {
                it.copy(
                    status = EstadosJuego.JUGANDO,
                    tablero = newBoard,
                    jugadores = listOf(localPlayer),
                    listaPalabras = newBoard.palabras,
                    tiempo = 300,
                )
            }
            startTimer()
        }
    }

    // --- LÓGICA DE RED (MULTIJUGADOR) ---

    /*private fun observeServerUpdates() {
        viewModelScope.launch {
            SocketManager.observeGameStateUpdates().collect { gameStateJson ->
                try {
                    val updatedBoard = Json.decodeFromString<Tablero>(gameStateJson)
                    _uiState.update { it.copy(tablero = updatedBoard, listaPalabras = updatedBoard.palabras) }
                } catch (e: Exception) {
                    Log.e("GameViewModel", "Error al deserializar actualización de estado: ${e.message}")
                }
            }
        }
    }*/
    private fun observeServerUpdates() {
        viewModelScope.launch {
            // Asegúrate que SocketManager escuche "game_updated"
            SocketManager.observeGameStateUpdates().collect { gameStateJson ->
                try {
                    // Ahora decodificamos el OBJETO COMPLETO del juego, no solo el tablero
                    // Necesitas crear una data class GameStateDTO que coincida con lo que manda Node
                    val serverState = Json.decodeFromString<GameStatus>(gameStateJson)

                    _uiState.update {
                        it.copy(
                            tablero = serverState.tablero,
                            listaPalabras = serverState.listaPalabras, // Lista actualizada con encontrados
                            jugadores = serverState.jugadores, // Puntajes actualizados
                            turnoActual = serverState.turnoActual, // Nuevo turno
                            seleccionActual = emptyList() // Limpiamos selección visual
                        )
                    }
                } catch (e: Exception) {
                    Log.e("GameViewModel", "Error sync: ${e.message}")
                }
            }
        }
    }

    // --- LÓGICA DE INPUT (COMÚN) ---

    fun onInputStart(coordinate: Coordenada) {
        // 1. Validar estado general
        if (_uiState.value.status != EstadosJuego.JUGANDO) return

        // 2. NUEVO: Validar si es MI turno (Solo en Multiplayer)
        if (roomId != null) {
            val turnoActual = _uiState.value.turnoActual // Asegúrate que GameStatus tenga este campo
            if (turnoActual != localPlayerId) {
                Log.d("APP_DEBUG_GameViewModel", "No es tu turno. Turno de: $turnoActual")
                return // <--- AQUÍ SE BLOQUEA EL TÁCTIL
            }
        }
        dragStartCoordinate = coordinate
        updateSelection(coordinate)
    }

    fun onInputDrag(currentCoordinate: Coordenada) {
        if (_uiState.value.status != EstadosJuego.JUGANDO) return
        updateSelection(currentCoordinate)
    }

    fun onInputEnd() {
        val currentSelection = _uiState.value.seleccionActual
        if (currentSelection.isEmpty()) {
            dragStartCoordinate = null
            return
        }

        // Construir la palabra formada por la selección
        val board = _uiState.value.tablero
        val stringBuilder = StringBuilder()
        currentSelection.forEach { coord ->
            // Asegurarse de no salir del array
            if(coord.fila < board.tamanno && coord.columna < board.tamanno){
                stringBuilder.append(board.celdas[coord.fila][coord.columna].letra)
            }
        }
        val wordFormed = stringBuilder.toString()

        // Verificar si existe en la lista (Lógica Local rápida)
        val validWord = _uiState.value.listaPalabras.find {
            (it.texto == wordFormed || it.texto == wordFormed.reversed()) && !it.encontrada
        }

        if (roomId != null) {
            // --- LÓGICA MULTIJUGADOR: Enviar al servidor ---
            if (validWord != null) {
                // Si la palabra es válida, avisamos al servidor
                // OJO: validWord.texto siempre debe enviarse en "derecho" (no invertido) si así está en tu DB
                SocketManager.submitWord(roomId, validWord.texto, localPlayerId!!)
            } else {
                // Si seleccionó basura, simplemente limpiamos la selección
                _uiState.update { it.copy(seleccionActual = emptyList()) }
            }
            /*try {
                val selectionJson = Json.encodeToString(currentSelection)
                SocketManager.submitWord(roomId, selectionJson, localPlayerId!!)
                Log.d("GameViewModel", "Palabra seleccionada enviada al servidor.")
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error al serializar selección: ${e.message}")
            }*/
        } else {
            // --- LÓGICA UN JUGADOR: Validar localmente ---
            validateWordLocally(currentSelection)
        }

        // Limpiar selección visual
        dragStartCoordinate = null
        _uiState.update { it.copy(seleccionActual = emptyList()) }
    }

    // --- LÓGICA DE VALIDACIÓN (SOLO PARA UN JUGADOR) ---

    private fun validateWordLocally(selection: List<Coordenada>) {
        val board = _uiState.value.tablero
        val stringBuilder = StringBuilder()
        selection.forEach { coord ->
            if (coord.fila < board.tamanno && coord.columna < board.tamanno) {
                stringBuilder.append(board.celdas[coord.fila][coord.columna].letra)
            }
        }
        val selectedWordText = stringBuilder.toString()
        val reversedWordText = selectedWordText.reversed()

        val foundWord = _uiState.value.listaPalabras.find { word ->
            (word.texto == selectedWordText || word.texto == reversedWordText) && word.encontradoPor == null
        }

        if (foundWord != null) {
            markWordAsFoundLocally(foundWord, selection)
        }
    }

    private fun markWordAsFoundLocally(word: Palabra, selection: List<Coordenada>) {
        val currentPlayer = _uiState.value.jugadores.first()

        _uiState.update { state ->
            val newCells = state.tablero.celdas.map { rowList ->
                rowList.map { cell ->
                    if (selection.contains(cell.coordenada)) {
                        cell.copy(isFound = true, encontradoPorJugadorID = currentPlayer.id)
                    } else {
                        cell
                    }
                }
            }

            val newTargetWords = state.listaPalabras.map { target ->
                if (target.id == word.id) target.copy(encontradoPor = currentPlayer.id) else target
            }

            val allFound = newTargetWords.all { it.encontradoPor != null }
            if (allFound) finishGame(currentPlayer)

            state.copy(
                tablero = state.tablero.copy(celdas = newCells),
                listaPalabras = newTargetWords,
                status = if (allFound) EstadosJuego.TERMINADO else EstadosJuego.JUGANDO
            )
        }
    }

    // --- LÓGICA COMÚN (CRONÓMETRO, CÁLCULO DE LÍNEA, FIN DEL JUEGO) ---

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.status == EstadosJuego.JUGANDO && _uiState.value.tiempo > 0) {
                delay(1000L)
                _uiState.update {
                    if (it.tiempo <= 1) {
                        finishGame(null)
                        it.copy(tiempo = 0)
                    } else {
                        it.copy(tiempo = it.tiempo - 1)
                    }
                }
            }
        }
    }

    private fun updateSelection(end: Coordenada) {
        val start = dragStartCoordinate ?: return
        val cellsInLine = calculateLine(start, end)
        _uiState.update { it.copy(seleccionActual = cellsInLine) }
    }

    private fun calculateLine(start: Coordenada, end: Coordenada): List<Coordenada> {
        val dRow = end.fila - start.fila
        val dCol = end.columna - start.columna
        val isDiagonal = abs(dRow) == abs(dCol)
        val isStraight = dRow == 0 || dCol == 0

        if (!isDiagonal && !isStraight) return listOf(start)

        val steps = max(abs(dRow), abs(dCol))
        if (steps == 0) return listOf(start)

        val stepRow = dRow / steps
        val stepCol = dCol / steps

        val line = mutableListOf<Coordenada>()
        for (i in 0..steps) {
            line.add(Coordenada(start.fila + (i * stepRow), start.columna + (i * stepCol)))
        }
        return line
    }

    private fun finishGame(winner: Jugador?) {
        timerJob?.cancel()
        _uiState.update { it.copy(status = EstadosJuego.TERMINADO, ganador = winner) }
    }
}

