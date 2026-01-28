package com.davfer.sopa_de_letras_uneg.ui.screens.game.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davfer.sopa_de_letras_uneg.dominio.logica.BoardGenerator
import com.davfer.sopa_de_letras_uneg.dominio.models.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

class GameViewModel : ViewModel() {

    // 1. Estado UI (La única verdad)
    private val _uiState = MutableStateFlow(GameStatus())
    val uiState: StateFlow<GameStatus> = _uiState.asStateFlow()

    // Dependencias (En un app real usarías Inyección de Dependencias como Hilt)
    private val generator = BoardGenerator()
    private var timerJob: Job? = null

    // Variables temporales para el gesto de arrastre
    private var dragStartCoordinate: Coordenada? = null

    init {
        startNewGame()
    }

    fun startNewGame() {
        // Reiniciar estado
        _uiState.value = GameStatus(status = EstadosJuego.CARGANDO )

        // Simular carga y generar tablero
        viewModelScope.launch {
            // Lista de palabras (luego vendrá de una configuración o API)
            val words = listOf("KOTLIN", "ANDROID", "COMPOSE", "SOCKET", "VIEWMODEL")

            // Generar tablero
            val newBoard = generator.generateBoard(10, words)

            // Crear Jugador Local (Nosotros)
            val localPlayer = Jugador(
                id = "local_player",
                nickname = "Yo",
                colorHex = "#4CAF50", // Verde Material
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

    // --- Lógica del Cronómetro ---
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.status == EstadosJuego.JUGANDO && _uiState.value.tiempo > 0) {
                delay(1000L)
                _uiState.update {
                    if (it.tiempo <= 1) {
                        // Se acabó el tiempo
                        finishGame(null) // Nadie gana por tiempo (o gana quien tenga más puntos)
                        it.copy(tiempo = 0)
                    } else {
                        it.copy(tiempo = it.tiempo - 1)
                    }
                }
            }
        }
    }

    // --- Lógica de Input (Gestos) ---

    // 1. El usuario toca la primera celda
    fun onInputStart(coordinate: Coordenada) {
        if (_uiState.value.status != EstadosJuego.JUGANDO) return
        dragStartCoordinate = coordinate
        updateSelection(coordinate)
    }

    // 2. El usuario arrastra el dedo a una nueva celda
    fun onInputDrag(currentCoordinate: Coordenada) {
        if (_uiState.value.status != EstadosJuego.JUGANDO) return
        updateSelection(currentCoordinate)
    }

    // 3. El usuario levanta el dedo -> Validar palabra
    fun onInputEnd() {
        val currentSelection = _uiState.value.seleccionActual
        if (currentSelection.isEmpty()) return

        validateWord(currentSelection)

        // Limpiar selección visual
        dragStartCoordinate = null
        _uiState.update { it.copy(seleccionActual = emptyList()) }
    }

    // --- Matemáticas y Validación ---

    private fun updateSelection(end: Coordenada) {
        val start = dragStartCoordinate ?: return

        // Algoritmo para trazar línea entre dos puntos en una cuadrícula
        // Solo permitimos líneas: Horizontal, Vertical o Diagonal Perfecta
        val cellsInLine = calculateLine(start, end)

        _uiState.update { it.copy(seleccionActual = cellsInLine) }
    }

    private fun calculateLine(start: Coordenada, end: Coordenada): List<Coordenada> {
        val dRow = end.fila - start.fila
        val dCol = end.columna- start.columna

        // Verificar si es una línea válida (Horiz, Vert, o Diagonal)
        // Diagonal válida es cuando abs(deltaRow) == abs(deltaCol)
        val isDiagonal = abs(dRow) == abs(dCol)
        val isStraight = dRow == 0 || dCol == 0

        if (!isDiagonal && !isStraight) {
            // Si el usuario hace un movimiento "chueco", solo seleccionamos el inicio (o nada)
            return listOf(start)
        }

        // Calcular los pasos para llenar la lista
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

    private fun validateWord(selection: List<Coordenada>) {
        val state = _uiState.value
        val board = state.tablero

        // 1. Construir la palabra formada por las celdas seleccionadas
        val stringBuilder = StringBuilder()
        selection.forEach { coord ->

            // Proteccion contra indices fuera de rango
            if (coord.fila < board.tamanno && coord.columna < board.tamanno) {
                // Buscar la celda en el tablero (asumiendo matriz cuadrada segura)
                val char = board.celdas[coord.fila][coord.columna].letra
                stringBuilder.append(char)
            }
        }
        val selectedWordText = stringBuilder.toString()
        val reversedWordText = selectedWordText.reversed() // Soporte para palabras al revés

        Log.d("DEBUG_GAME", "Texto seleccionado: $selectedWordText")

        // 2. Buscar si coincide con alguna palabra objetivo NO encontrada aún
        // Debemos buscar tanto el texto normal como el invertido

        // Debug 2: ¿Contra qué estamos comparando?
        state.listaPalabras.forEach {
            Log.d("DEBUG_GAME", "Objetivo: ${it.texto} (Encontrada: ${it.encontradoPor})")
        }

        val foundWord = state.listaPalabras.find { word ->
            (word.texto == selectedWordText || word.texto == reversedWordText) &&
                    word.encontradoPor == null
        }

        if (foundWord != null) {
            Log.d("DEBUG_GAME", "¡EXITO! Palabra encontrada: ${foundWord.texto}. Iniciando actualización...")
            // ¡PALABRA ENCONTRADA!
            markWordAsFound(foundWord, selection)
        } else {
            Log.d("DEBUG_GAME", "FALLO: No hubo coincidencia.")
        }
    }

    private fun markWordAsFound(word: Palabra, selection: List<Coordenada>) {
        val currentPlayer = _uiState.value.jugadores.firstOrNull() ?: return // Local player

        _uiState.update { state ->
            // A. Marcar celdas como encontradas en el tablero
            // Necesitamos recrear las celdas porque son inmutables (Data Classes)
            val newCells = state.tablero.celdas.map { rowList ->
                rowList.map { cell ->
                    if (selection.contains(cell.coordenada)) {
                        cell.copy(isFound = true, encontradoPorJugadorID = currentPlayer.id)
                    } else {
                        cell
                    }
                }
            }

            // B. Actualizar lista de palabras (Marcar esta palabra como encontrada)
            //DEBUGS
            val newTargetWords = state.listaPalabras.map {  target->
                if (target.id == word.id) {
                    Log.d("DEBUG_GAME", "Marcando palabra ${target.texto} como encontrada")
                    target.copy(encontradoPor = currentPlayer.id)
                } else {
                    target
                }
            }

            // C. Verificar Victoria
            val allFound = newTargetWords.all { it.encontradoPor != null }
            val newStatus = if (allFound) EstadosJuego.TERMINADO else EstadosJuego.JUGANDO
            if (allFound) finishGame(currentPlayer)

            state.copy(
                tablero = state.tablero.copy(celdas = newCells),
                listaPalabras = newTargetWords,
                status = newStatus
            )
        }
    }

    private fun finishGame(winner: Jugador?) {
        timerJob?.cancel()
        _uiState.update { it.copy(status = EstadosJuego.TERMINADO, ganador = winner) }
    }
}