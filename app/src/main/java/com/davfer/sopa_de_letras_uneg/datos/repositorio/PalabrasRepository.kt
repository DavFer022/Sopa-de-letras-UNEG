package com.davfer.sopa_de_letras_uneg.datos.repositorio

object PalabrasRepository {
    private val categorias = mapOf(
        "INFORMATICA" to listOf(
            "KOTLIN", "ANDROID", "COMPOSE", "SOCKET", "SERVIDOR",
            "MOBILE", "GITHUB", "BRANCH", "MERGE", "TECLADO",
            "LOGCAT", "VIEWMODEL", "REPOSITORIO", "DATABASE", "FIREBASE"
        ),
        "CIUDADES" to listOf(
            "CARACAS", "MADRID", "LONDRES", "TOKIO", "PARIS",
            "BOGOTA", "BERLIN", "ROMA", "LIMA", "QUITO",
            "SANTIAGO", "BRASILIA", "MOSCU", "PEKIN", "SEUL"
        ),
        "COMIDA" to listOf(
            "AREPA", "PIZZA", "PASTA", "HAMBURGUESA", "SUSHI",
            "ENSALADA", "SOPA", "ARROZ", "POLLO", "CARNE",
            "TACO", "BURRITO", "HELADO", "DONA", "FRUTA"
        )
    )

    fun obtenerCategorias(): List<String> = categorias.keys.toList()

    fun obtenerPalabrasPorCategoria(categoria: String, cantidad: Int): List<String> {
        val lista = categorias[categoria] ?: categorias.values.first()
        return lista.shuffled().take(cantidad)
    }
}
