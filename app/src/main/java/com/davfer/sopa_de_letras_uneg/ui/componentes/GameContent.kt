package com.davfer.sopa_de_letras_uneg.ui.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davfer.sopa_de_letras_uneg.dominio.models.GameStatus
import com.davfer.sopa_de_letras_uneg.ui.componentes.GameBoard
import com.davfer.sopa_de_letras_uneg.ui.componentes.GameHeader
import com.davfer.sopa_de_letras_uneg.ui.componentes.WordList
import com.davfer.sopa_de_letras_uneg.ui.screens.game.viewmodel.GameViewModel


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