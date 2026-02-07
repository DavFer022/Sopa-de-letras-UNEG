package com.davfer.sopa_de_letras_uneg.ui.screens.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun LobbyScreen(navController: NavController) {
    var roomCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { /* TODO: Navigate to GameScreen with new room */ }) {
            Text(text = "Crear Sala")
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = roomCode,
            onValueChange = { roomCode = it },
            label = { Text("Código de la sala") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { /* TODO: Navigate to GameScreen with existing room */ },
            enabled = roomCode.isNotBlank()
        ) {
            Text(text = "Unirse a Sala")
        }
    }
}
