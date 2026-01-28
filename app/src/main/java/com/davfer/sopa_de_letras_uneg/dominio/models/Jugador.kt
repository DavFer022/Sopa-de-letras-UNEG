package com.davfer.sopa_de_letras_uneg.dominio.models

import kotlinx.serialization.Serializable

@Serializable
data class Jugador (
    val id: String,           // El Socket ID (ej: "H12_asd9")
    val nickname: String,     // Nombre visible (ej: "Juan123")
    val puntaje: Int = 0,       // Puntaje actual
    val colorHex: String,     // Color asignado (ej: "#FF0000" para rojo)
    val isHost: Boolean = false // ¿Es quien creó la sala?
)