package com.davfer.sopa_de_letras_uneg.dominio.models

import kotlinx.serialization.Serializable

@Serializable
data class Tablero(
    val tamanno: Int = 10,              // Tamaño (ej: 10x10)
    val celdas: List<List<Celda>> ,      // La matriz real de celdas
    val palabras: List<Palabra>          // Lista de palabras encontradas
)


