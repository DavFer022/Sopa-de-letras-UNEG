package com.davfer.sopa_de_letras_uneg.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToSolo: () -> Unit,
    onNavigateToMulti: () -> Unit,
    onNavigateToHowTo: () -> Unit
) {
    var showGameOptions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Sopa de Letras", style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(32.dp))

        if (!showGameOptions) {
            Button(onClick = { showGameOptions = true }, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text("Jugar")
            }
        } else {
            Button(onClick = onNavigateToSolo, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text("Solo")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onNavigateToMulti, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text("Multijugador")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToHowTo, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text("¿Cómo se juega?")
        }
    }
}