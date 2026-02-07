package com.davfer.sopa_de_letras_uneg.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.davfer.sopa_de_letras_uneg.dominio.models.Celda
import com.davfer.sopa_de_letras_uneg.dominio.models.Coordenada
import com.davfer.sopa_de_letras_uneg.dominio.models.EstadosJuego
import com.davfer.sopa_de_letras_uneg.dominio.models.GameStatus
import com.davfer.sopa_de_letras_uneg.dominio.models.Jugador
import com.davfer.sopa_de_letras_uneg.dominio.models.Palabra
import com.davfer.sopa_de_letras_uneg.dominio.models.Tablero
import com.davfer.sopa_de_letras_uneg.ui.navegacion.AppScreens
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.davfer.sopa_de_letras_uneg.ui.screens.game.viewmodel.GameViewModel
import java.util.concurrent.TimeUnit
import androidx.core.graphics.toColorInt

@Composable
fun GameScreen(navController: NavController, viewModel: GameViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (uiState.status) {
            EstadosJuego.CARGANDO -> {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Text(
                    text = "Generando Sopa de Letras...",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            EstadosJuego.JUGANDO -> {
                GameContent(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
            EstadosJuego.TERMINADO -> {
                val winnerNickname = uiState.ganador?.nickname ?: "Nadie"
                LaunchedEffect(Unit) {
                    navController.navigate("${AppScreens.ResultScreen.route}/$winnerNickname")
                }
            }
        }
    }
}


/**
 * Muestra el contenido principal del juego cuando el estado es JUGANDO.
 */
@Composable
fun GameContent(viewModel: GameViewModel, uiState: GameStatus) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameHeader(timeLeft = uiState.tiempo)
        Spacer(modifier = Modifier.height(16.dp))
        GameBoard(
            viewModel = viewModel,
            tablero = uiState.tablero,
            selection = uiState.seleccionActual,
            jugadores = uiState.jugadores
        )
        Spacer(modifier = Modifier.height(24.dp))
        WordList(palabras = uiState.listaPalabras)
    }
}


/**
 * Muestra el temporizador del juego.
 */
@Composable
fun GameHeader(timeLeft: Int) {
    val minutes = TimeUnit.SECONDS.toMinutes(timeLeft.toLong())
    val seconds = timeLeft - TimeUnit.MINUTES.toSeconds(minutes)
    Text(
        text = String.format("%02d:%02d", minutes, seconds),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

/**
 * Dibuja el tablero y maneja la detección de gestos para la selección de palabras.
 */
@Composable
fun GameBoard(
    viewModel: GameViewModel,
    tablero: Tablero,
    selection: List<Coordenada>,
    jugadores: List<Jugador>
) {
    var cellSize by remember { mutableFloatStateOf(0f) }

    val selectionSet = remember(selection) { selection.toSet() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Asegura que el tablero sea cuadrado
            .background(Color.LightGray)
            .onGloballyPositioned {
                cellSize = it.size.width.toFloat() / tablero.tamanno
            }
            .pointerInput(tablero.tamanno, cellSize) {
                if (cellSize == 0f) return@pointerInput // Evitar división por cero

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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            tablero.celdas.forEach { rowList ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    rowList.forEach { celda ->
                        CellView(
                            modifier = Modifier.weight(1f),
                            celda = celda,
                            isSelected = selectionSet.contains(celda.coordenada),
                            jugadores = jugadores
                        )
                    }
                }
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
    jugadores: List<Jugador>
) {
    val backgroundColor = when {
        celda.isFound -> {
            val player = jugadores.find { it.id == celda.encontradoPorJugadorID }
            val colorString = player?.colorHex ?: "#CCCCCC" // Gris por defecto
            try {
                Color(colorString.toColorInt()).copy(alpha = 0.6f)
            } catch (e: IllegalArgumentException) {
                Color.Gray.copy(alpha = 0.6f)
            }
        }
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .background(backgroundColor)
            .border(0.5.dp, Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = celda.letra.toString(),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Muestra la lista de palabras a encontrar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordList(palabras: List<Palabra>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Palabras a Encontrar", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            palabras.forEach { palabra ->
                val isFound = palabra.encontradoPor != null
                Text(
                    text = palabra.texto,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    textDecoration = if (isFound) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isFound) Color.Gray else MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGameScreen() {
    GameScreen(navController = rememberNavController())
}

