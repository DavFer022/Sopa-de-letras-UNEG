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
    private val initialGameStateJson: String? = null,
    private val roomId: String? = null,
    val localPlayerId: String? = null
) : ViewModel() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val _uiState = MutableStateFlow(GameStatus())
    val uiState: StateFlow<GameStatus> = _uiState.asStateFlow()

    private val generator = BoardGenerator()
    private var timerJob: Job? = null
    private var dragStartCoordinate: Coordenada? = null

    init {
        if (initialGameStateJson != null && roomId != null && localPlayerId != null) {
            initializeMultiplayerGame(initialGameStateJson)
        } else {
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

    private fun initializeMultiplayerGame(gameStateJson: String) {
        try {
            val serverState = json.decodeFromString<GameStatus>(gameStateJson)
            _uiState.value = serverState.copy(
                status = EstadosJuego.JUGANDO,
                roomID = roomId ?: serverState.roomID
            )
            Log.d("GameViewModel", "Juego Multijugador iniciado. Turno: ${serverState.turnoActual}")
            observeServerUpdates()
            startTimer()
        } catch (e: Exception) {
            Log.e("APP_DEBUG", "Error deserialización inicial: ${e.message}")
        }
    }

    private fun startSinglePlayerGame() {
        _uiState.value = GameStatus(status = EstadosJuego.CARGANDO)
        viewModelScope.launch {
            val words = PalabrasRepository.obtenerPalabrasPorCategoria("INFORMATICA", 5)
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

    private fun observeServerUpdates() {
        viewModelScope.launch {
            SocketManager.observeGameStateUpdates().collect { gameStateJson ->
                try {
                    Log.d("APP_DEBUG", "Update recibido: $gameStateJson")
                    val serverState = json.decodeFromString<GameStatus>(gameStateJson)
                    _uiState.update { currentState ->
                        currentState.copy(
                            tablero = serverState.tablero,
                            listaPalabras = serverState.listaPalabras,
                            jugadores = serverState.jugadores,
                            turnoActual = serverState.turnoActual,
                            status = serverState.status,
                            ganador = serverState.ganador,
                            revelarRespuestas = serverState.revelarRespuestas,
                            isEmpate = serverState.isEmpate,
                            seleccionActual = emptyList(),
                            tiempoRestanteTurno = serverState.tiempoRestanteTurno
                        )
                    }
                } catch (e: Exception) {
                    Log.e("APP_DEBUG", "Error al procesar update del servidor: ${e.message}")
                }
            }
        }
    }

    fun onInputStart(coordinate: Coordenada) {
        if (_uiState.value.status != EstadosJuego.JUGANDO) return
        if (roomId != null) {
            val turnoActual = _uiState.value.turnoActual
            if (turnoActual != localPlayerId) {
                Log.d("APP_DEBUG_GameViewModel", "No es tu turno. Turno de: $turnoActual")
                return
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

        val board = _uiState.value.tablero
        val stringBuilder = StringBuilder()
        currentSelection.forEach { coord ->
            if(coord.fila < board.tamanno && coord.columna < board.tamanno){
                stringBuilder.append(board.celdas[coord.fila][coord.columna].letra)
            }
        }
        val wordFormed = stringBuilder.toString()
        val validWord = _uiState.value.listaPalabras.find {
            (it.texto == wordFormed || it.texto == wordFormed.reversed()) && !it.encontrada
        }

        if (roomId != null) {
            if (validWord != null) {
                SocketManager.submitWord(roomId, validWord.texto, localPlayerId!!)
            } else {
                _uiState.update { it.copy(seleccionActual = emptyList()) }
            }
        } else {
            validateWordLocally(currentSelection)
        }

        dragStartCoordinate = null
        _uiState.update { it.copy(seleccionActual = emptyList()) }
    }

    fun onRevealToggleClicked() {
        if (roomId != null) {
            SocketManager.toggleReveal(roomId)
        } else {
            // Lógica local para un jugador
            _uiState.update { it.copy(revelarRespuestas = !it.revelarRespuestas) }
        }
    }

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
