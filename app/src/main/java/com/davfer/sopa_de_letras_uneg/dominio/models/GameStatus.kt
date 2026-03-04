package com.davfer.sopa_de_letras_uneg.dominio.models

import kotlinx.serialization.Serializable

@Serializable
data class GameStatus(
    val roomID: String = "",                                                    // El ID de la sala
    val status: EstadosJuego = EstadosJuego.CARGANDO,                           // La bandera
    val tablero: Tablero = Tablero(0, emptyList(), emptyList()),          // Siempre existe, aunque esté vacío
    val jugadores: List<Jugador> = emptyList(),
    val listaPalabras: List<Palabra> = emptyList(),                             //Lista de palabras a buscar
    val tiempo: Int = 300,                                                      // Tiempo total en segundos
    val ganador: Jugador? = null,                                                // Nulo mientras se juega
    val seleccionActual: List<Coordenada> = emptyList(),                    // Lista de celdas seleccionadas
    val turnoActual: String? = null,                                         // ID del jugador actual
    val tiempoPorTurno: Int = 10,                                            // Configuración: Segundos por turno
    val tiempoRestanteTurno: Int = 10,                                       // Estado: Cuenta regresiva del turno
    val censuraActiva: Boolean = false                                       // Configuración: ¿Ocultar tablero?
)

enum class EstadosJuego{ CARGANDO, JUGANDO, TERMINADO }