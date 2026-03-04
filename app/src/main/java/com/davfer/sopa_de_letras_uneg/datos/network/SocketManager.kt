package com.davfer.sopa_de_letras_uneg.datos.network

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject

object SocketManager {
    private var socket: Socket? = null

    // IMPORTANTE:
    // Si usas Emulador: "http://10.0.2.2:3000"
    // Si usas Celular físico: La IP local de tu PC ("http://192.168.0.107:3000")
    private const val BASE_URL = "http://192.168.0.107:3000"

    fun connect() {
        try {
            socket = IO.socket(BASE_URL)
            socket?.connect()
            Log.d("SocketManager", "Intentando conectar...")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun isConnected(): Boolean = socket?.connected() ?: false

    fun joinRoom(
        roomId: String,
        playerJson: String,
        nickname: String
    ) {
        val data = JSONObject()
        data.put("roomId", roomId)
        data.put("nickname", nickname)
        data.put("player", JSONObject(playerJson)) // Enviar el objeto Jugador serializado
        socket?.emit("join_room", data)
    }

    fun startGame(roomId: String, gameStateJson: String) {
        val data = JSONObject()
        data.put("roomId", roomId)
        data.put("gameState", gameStateJson) // Envías el JSON del tablero
        socket?.emit("start_game", data)
    }

    // Escuchar actualizaciones de jugadores (Flow para Compose)
    fun observePlayers(): Flow<String> = callbackFlow {
        socket?.on("update_players") { args ->
            val data = args[0].toString() // JSON Array de jugadores
            trySend(data)
        }
        awaitClose { socket?.off("update_players") }
    }


    // Escuchar inicio de juego
    fun observeGameStart(): Flow<String> = callbackFlow {
        socket?.on("game_started") { args ->
            val gameStateJson = args[0].toString()
            trySend(gameStateJson)
        }
        awaitClose { socket?.off("game_started") }
    }

    // Escuchar actualizaciones del estado del juego
    fun observeGameStateUpdates(): Flow<String> = callbackFlow {
        socket?.on("game_state_update") { args ->
            val gameStateJson = args[0].toString()
            trySend(gameStateJson)
        }
        awaitClose { socket?.off("game_state_update") }
    }

    // Enviar palabra seleccionada al servidor
    fun submitWord(roomId: String, wordText: String, playerId: String) {
        val data = JSONObject()
        data.put("roomId", roomId)
        data.put("wordText", wordText)
        data.put("playerId", playerId)
        socket?.emit("submit_word", data)
    }
}