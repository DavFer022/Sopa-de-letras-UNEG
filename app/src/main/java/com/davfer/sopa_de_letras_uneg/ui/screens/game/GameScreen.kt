package com.davfer.sopa_de_letras_uneg.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davfer.sopa_de_letras_uneg.dominio.models.Coordenada
import com.davfer.sopa_de_letras_uneg.ui.screens.game.viewmodel.GameViewModel

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Tiempo: ${state.tiempo}s", style = MaterialTheme.typography.headlineSmall)
            Text(text = state.status.name, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f) // Hacerlo cuadrado
                .pointerInput(Unit) {
                    // Lógica para detectar el arrastre del dedo
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            val coord = calculateCoordinate(offset, size.width, state.tablero.tamanno)
                            viewModel.onInputStart(coord)
                        },
                        onDrag = { change, _ ->
                            val coord = calculateCoordinate(change.position, size.width, state.tablero.tamanno)
                            viewModel.onInputDrag(coord)
                        },
                        onDragEnd = {
                            viewModel.onInputEnd()
                        }
                    )
                }
        ) {
            val tamanno = state.tablero.tamanno
            if (tamanno > 0) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(tamanno),
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false
                ) {
                    val flatCells = state.tablero.celdas.flatten()
                    items(flatCells) { celda ->
                        val isSelected = state.seleccionActual.contains(celda.coordenada)
                        val isFound = celda.isFound

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    when {
                                        isFound -> Color(0xFF8BC34A) // Verde si ya se encontró
                                        isSelected -> Color(0xFFFFEB3B) // Amarillo si se está seleccionando
                                        else -> Color.LightGray.copy(alpha = 0.3f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = celda.letra.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Palabras por encontrar:", style = MaterialTheme.typography.titleMedium)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            state.listaPalabras.forEach { palabra ->
                val encontrada = palabra.encontradoPor != null
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (encontrada) Color.Gray else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = palabra.texto,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = if (encontrada)
                            MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                        else
                            MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Botón de reinicio
        if (state.status.name == "TERMINADO") {
            Button(onClick = { viewModel.startNewGame() }) {
                Text("Jugar de nuevo")
            }
        }
    }
}

/**
 * Función auxiliar para convertir los pixeles del toque en coordenadas (Fila, Columna)
 */
private fun calculateCoordinate(offset: androidx.compose.ui.geometry.Offset, totalWidth: Int, gridCount: Int): Coordenada {
    val cellSize = totalWidth / gridCount
    val col = (offset.x / cellSize).toInt().coerceIn(0, gridCount - 1)
    val row = (offset.y / cellSize).toInt().coerceIn(0, gridCount - 1)
    return Coordenada(row, col)
}