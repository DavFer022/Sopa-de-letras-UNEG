package com.davfer.sopa_de_letras_uneg.dominio.logica

import com.davfer.sopa_de_letras_uneg.dominio.models.Celda
import com.davfer.sopa_de_letras_uneg.dominio.models.Coordenada
import com.davfer.sopa_de_letras_uneg.dominio.models.Palabra
import kotlin.random.Random
import com.davfer.sopa_de_letras_uneg.dominio.models.Tablero

class BoardGenerator {

    fun generateBoard(size: Int, wordsToHide: List<String>): Tablero {
        // 1. Crear matriz temporal vacía (null representa espacio libre)
        val tempGrid = Array(size) { arrayOfNulls<Char>(size) }
        val placedWords = mutableListOf<Palabra>()

        // 2. Ordenar palabras por longitud (las largas son más difíciles de ubicar)
        val sortedWords = wordsToHide.sortedByDescending { it.length }

        for (wordText in sortedWords) {
            var placed = false
            var attempts = 0

            // Intentamos colocar la palabra 100 veces
            while (!placed && attempts < 100) {
                attempts++

                // Elegir posición y dirección al azar
                val startRow = Random.nextInt(size)
                val startCol = Random.nextInt(size)
                val direction = Direction.entries.toTypedArray().random()

                if (canPlace(tempGrid, wordText, startRow, startCol, direction, size)) {
                    // Si cabe, la colocamos y guardamos el registro
                    val endRow = startRow + (direction.dRow * (wordText.length - 1))
                    val endCol = startCol + (direction.dCol * (wordText.length - 1))

                    placeWord(tempGrid, wordText, startRow, startCol, direction)

                    placedWords.add(
                        Palabra(
                            id = wordText,
                            texto = wordText,
                            inicio = Coordenada(startRow, startCol),
                            final = Coordenada(endRow, endCol)
                        )
                    )
                    placed = true
                }
            }
            if (!placed) {
                println("⚠️ Alerta: No se pudo colocar la palabra '$wordText'")
            }
        }

        // 3. Rellenar espacios vacíos y construir el objeto Board final
        val finalCells = List(size) { row ->
            List(size) { col ->
                val char = tempGrid[row][col] ?: ('A'..'Z').random()
                Celda(
                    coordenada = Coordenada(row, col),
                    letra = char
                )
            }
        }

        return Tablero(size, finalCells, placedWords)
    }

    // --- Helpers Privados ---

    private fun canPlace(
        grid: Array<Array<Char?>>,
        word: String,
        row: Int,
        col: Int,
        dir: Direction,
        size: Int
    ): Boolean {
        // Verificar límites finales
        val endRow = row + (dir.dRow * (word.length - 1))
        val endCol = col + (dir.dCol * (word.length - 1))

        if (endRow !in 0 until size || endCol !in 0 until size) return false

        // Verificar colisiones letra por letra
        for (i in word.indices) {
            val r = row + (dir.dRow * i)
            val c = col + (dir.dCol * i)
            val cellContent = grid[r][c]

            // Solo es válido si está vacío O si tiene la MISMA letra (cruce)
            if (cellContent != null && cellContent != word[i]) {
                return false
            }
        }
        return true
    }

    private fun placeWord(
        grid: Array<Array<Char?>>,
        word: String,
        row: Int,
        col: Int,
        dir: Direction
    ) {
        for (i in word.indices) {
            val r = row + (dir.dRow * i)
            val c = col + (dir.dCol * i)
            grid[r][c] = word[i]
        }
    }
}


// Direcciones posibles (dRow, dCol)
enum class Direction(val dRow: Int, val dCol: Int) {
    HORIZONTAL(0, 1),
    VERTICAL(1, 0),
    DIAGONAL(1, 1), // Puedes agregar (-1, 1) para diagonal inversa, etc.
    HINVERSO(0,-1),
    VINVERSO(-1,0),
    DIAGONALINVERSO(-1,-1)
}


//Funcion main para probar la generacion
fun main() {
    val generator = BoardGenerator()
    val words = listOf("ANDROID", "KOTLIN", "SOCKET", "JUEGO", "COMPOSE", "SERVER")

    println("--- Generando Sopa de Letras ---")
    val board = generator.generateBoard(10, words)

    // Imprimir en consola para Persona A
    println("  0 1 2 3 4 5 6 7 8 9") // Encabezado de columnas
    board.celdas.forEachIndexed { rowIndex, rowCells ->
        print("$rowIndex ") // Encabezado de fila
        rowCells.forEach { cell ->
            print("${cell.letra} ")
        }
        println()
    }

    println("\n--- Soluciones ---")
    board.palabras.forEach {
        println("${it.texto}: Start(${it.inicio.fila},${it.inicio.columna}) -> End(${it.final.fila},${it.final.columna})")
    }
}