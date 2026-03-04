package com.davfer.sopa_de_letras_uneg.ui.screens.game

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.davfer.sopa_de_letras_uneg.dominio.models.EstadosJuego
import com.davfer.sopa_de_letras_uneg.ui.componentes.GameContent
import com.davfer.sopa_de_letras_uneg.ui.screens.game.viewmodel.GameViewModel

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
                val encodedWinner = Uri.encode(winnerNickname)
                LaunchedEffect(Unit) {
                    navController.navigate("result_screen/$encodedWinner")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGameScreen() {
    GameScreen(navController = rememberNavController())
}