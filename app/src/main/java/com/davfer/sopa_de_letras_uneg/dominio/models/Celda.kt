package com.davfer.sopa_de_letras_uneg.dominio.models

import kotlinx.serialization.Serializable

@Serializable
data class Celda(
    val coordenada: Coordenada,
    val letra: Char,
    val isFound: Boolean = false,                   // ¿Ya es parte de una palabra encontrada?
    val encontradoPorJugadorID: String? = null      // ¿Quién la encontró? (Para pintarla de su color)
)