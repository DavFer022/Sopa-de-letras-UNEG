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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onNavigateToJugar: () -> Unit, onNavigateToHowTo: () -> Unit) {
    Column(

        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Sopa de Letras", style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNavigateToJugar, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text("Jugar")
        }
        Button(onClick = onNavigateToHowTo, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text("¿Cómo se juega?")
        }
    }
}