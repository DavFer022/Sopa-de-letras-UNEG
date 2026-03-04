package com.davfer.sopa_de_letras_uneg.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.davfer.sopa_de_letras_uneg.dominio.models.Palabra
import java.util.concurrent.TimeUnit
import kotlin.collections.forEach

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

/**
 * Muestra el temporizador del juego.
 */
@Composable
fun GameHeader(timeLeftTotal: Int, timeLeftTurn: Int) {
    val minutes = TimeUnit.SECONDS.toMinutes(timeLeftTotal.toLong())
    val seconds = timeLeftTotal - TimeUnit.MINUTES.toSeconds(minutes)
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = String.format("Tiempo Total: %02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = "Tiempo de Turno: ${timeLeftTurn}s",
            style = MaterialTheme.typography.headlineMedium,
            color = if (timeLeftTurn <= 3) Color.Red else MaterialTheme.colorScheme.primary
        )
    }
}
