package com.davfer.sopa_de_letras_uneg.datos.repositorio

object PalabrasRepository {
    // Lista maestra de palabras (puedes añadir todas las que quieras)
    private val bancoDePalabras = listOf(
        "KOTLIN", "ANDROID", "COMPOSE", "SOCKET", "SERVIDOR",
        "MOBILE", "BORRAR", "PERA", "ESTUDIO", "TELEFONO",
        "LOGCAT", "GITHUB", "BRANCH", "MERGE", "TECLADO"
    )

    /**
     * Retorna una lista de palabras aleatorias sin repetir.
     */
    fun obtenerPalabrasAleatorias(cantidad: Int): List<String> {
        return bancoDePalabras.shuffled().take(cantidad)
    }
}