package com.davfer.sopa_de_letras_uneg.ui.screens.game

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
fun RoleSelectionScreen(onRoleSelected: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Selecciona tu rol", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { onRoleSelected(true) }, modifier = Modifier.fillMaxWidth(0.7f)) {
            Text("Crear Sala (Host)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onRoleSelected(false) }, modifier = Modifier.fillMaxWidth(0.7f)) {
            Text("Unirse a Sala (Cliente)")
        }
    }
}