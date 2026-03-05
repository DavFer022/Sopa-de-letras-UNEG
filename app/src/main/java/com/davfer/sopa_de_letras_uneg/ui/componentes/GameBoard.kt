package com.davfer.sopa_de_letras_uneg.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.davfer.sopa_de_letras_uneg.dominio.models.Celda
import com.davfer.sopa_de_letras_uneg.dominio.models.Coordenada
import com.davfer.sopa_de_letras_uneg.dominio.models.Jugador
import com.davfer.sopa_de_letras_uneg.dominio.models.Tablero
import com.davfer.sopa_de_letras_uneg.ui.screens.game.viewmodel.GameViewModel

/**
 * Dibuja el tablero y maneja la detección de gestos para la selección de palabras.
 */
@Composable
fun GameBoard(
    viewModel: GameViewModel,
    tablero: Tablero,
    selection: List<Coordenada>,
    jugadores: List<Jugador>,
    turnoActualId: String?,
    localPlayerId: String?,
    censuraActiva: Boolean,
    revelarRespuestas: Boolean
) {
    var cellSize by remember { mutableFloatStateOf(0f) }
    val isMyTurn = turnoActualId == localPlayerId || localPlayerId == "single_player"
    val showCensorship = censuraActiva && !isMyTurn

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color.LightGray)
            .onGloballyPositioned {
                cellSize = it.size.width.toFloat() / tablero.tamanno
            }
            .pointerInput(tablero.tamanno, cellSize, isMyTurn) {
                if (cellSize == 0f || !isMyTurn) return@pointerInput 

                detectDragGestures(
                    onDragStart = { offset ->
                        val col = (offset.x / cellSize).toInt().coerceIn(0, tablero.tamanno - 1)
                        val row = (offset.y / cellSize).toInt().coerceIn(0, tablero.tamanno - 1)
                        viewModel.onInputStart(Coordenada(row, col))
                    },
                    onDrag = { change, _ ->
                        val col = (change.position.x / cellSize).toInt().coerceIn(0, tablero.tamanno - 1)
                        val row = (change.position.y / cellSize).toInt().coerceIn(0, tablero.tamanno - 1)
                        viewModel.onInputDrag(Coordenada(row, col))
                    },
                    onDragEnd = {
                        viewModel.onInputEnd()
                    }
                )
            }
    ) {
        // Tablero Real
        Column(modifier = Modifier.fillMaxSize()) {
            tablero.celdas.forEach { rowList ->
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    rowList.forEach { celda ->
                        CellView(
                            modifier = Modifier.weight(1f),
                            celda = celda,
                            isSelected = selection.contains(celda.coordenada),
                            jugadores = jugadores,
                            revelarRespuestas = revelarRespuestas
                        )
                    }
                }
            }
        }

        // Overlay de Censura
        if (showCensorship) {
            val nicknameTurno = jugadores.find { it.id == turnoActualId }?.nickname ?: "otro jugador"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Es el turno de\n$nicknameTurno",
                    color = Color.White,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

/**
 * Dibuja una celda individual del tablero.
 */
@Composable
fun CellView(
    modifier: Modifier = Modifier,
    celda: Celda,
    isSelected: Boolean,
    jugadores: List<Jugador>,
    revelarRespuestas: Boolean = false
) {
    val backgroundColor = when {
        celda.isFound -> {
            val player = jugadores.find { it.id == celda.encontradoPorJugadorID }
            val colorString = player?.colorHex ?: "#CCCCCC"
            try {
                Color(colorString.toColorInt()).copy(alpha = 0.6f)
            } catch (e: IllegalArgumentException) {
                Color.Gray.copy(alpha = 0.6f)
            }
        }
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }

    // Lógica Académica: Si se revela, ocultar letras que no son parte de palabras
    val letraAMostrar = if (revelarRespuestas && !celda.isPartOfWord) "" else celda.letra.toString()

    Box(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .background(backgroundColor)
            .border(0.5.dp, Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letraAMostrar,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}