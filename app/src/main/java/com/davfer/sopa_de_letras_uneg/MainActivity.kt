package com.davfer.sopa_de_letras_uneg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.davfer.sopa_de_letras_uneg.ui.navegacion.AppNavigation
import com.davfer.sopa_de_letras_uneg.ui.theme.Sopa_de_letras_UNEGTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Sopa_de_letras_UNEGTheme {
               Scaffold(modifier = Modifier.fillMaxSize()) {innerPadding->
                   AppNavigation( modifier = Modifier.padding(innerPadding))
               }
            }
        }
    }
}

