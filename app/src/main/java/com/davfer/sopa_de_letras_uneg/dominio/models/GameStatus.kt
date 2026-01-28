package com.davfer.sopa_de_letras_uneg.dominio.models

data class GameStatus(
    val roomID: String = "",                                                    // El ID de la sala
    val status: EstadosJuego = EstadosJuego.CARGANDO,                           // La bandera
    val tablero: Tablero = Tablero(0, emptyList(), emptyList()),          // Siempre existe, aunque esté vacío
    val jugadores: List<Jugador> = emptyList(),
    val listaPalabras: List<Palabra> = emptyList(),                             //Lista de palabras a buscar
    val tiempo: Int = 300,                                                      // Tiempo en segundos
    val ganador: Jugador? = null,                                                // Nulo mientras se juega
    val seleccionActual: List<Coordenada> = emptyList()                        // Lista de celdas seleccionadas
)

enum class EstadosJuego{ CARGANDO, JUGANDO, TERMINADO }