package com.davfer.sopa_de_letras_uneg.ui.screens.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.davfer.sopa_de_letras_uneg.ui.navegacion.AppScreens

@Composable
fun ResultScreen(navController: NavController, winner: String?) {
    val isEmpate = winner == "Empate"

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "¡Juego Terminado!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isEmpate) {
            Text(text = "¡Es un Empate!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.secondary)
            Text(text = "Ambos jugadores jugaron excelente", style = MaterialTheme.typography.bodyMedium)
        } else {
            Text(text = "Ganador: ${winner ?: "Nadie"}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            navController.navigate(AppScreens.HomeScreen.route) {
                popUpTo(AppScreens.HomeScreen.route) {
                    inclusive = true
                }
            }
        }) {
            Text(text = "Volver al Inicio")
        }
    }
}
