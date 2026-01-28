package com.davfer.sopa_de_letras_uneg.dominio.models

import kotlinx.serialization.Serializable


@Serializable
data class Coordenada(
    val fila : Int,     // Fila X en la que se encuentra la letra
    val columna : Int   // Columna Y en la que se encuentra la letra

)
