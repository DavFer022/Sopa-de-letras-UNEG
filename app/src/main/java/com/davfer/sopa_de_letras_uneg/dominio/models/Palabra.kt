package com.davfer.sopa_de_letras_uneg.dominio.models

import kotlinx.serialization.Serializable

@Serializable
data class Palabra (
    val id: String,                         // Identificador único (ej: "word_1")
    val texto : String,                     // El texto visible (ej: "ANDROID")
    val inicio: Coordenada,                 // Dónde empieza en el tablero
    val final: Coordenada,                  // Dónde termina
    val encontradoPor: String? = null,       // ID del jugador que la encontró (null si nadie la ha hallado)
    val encontrada: Boolean = false
)