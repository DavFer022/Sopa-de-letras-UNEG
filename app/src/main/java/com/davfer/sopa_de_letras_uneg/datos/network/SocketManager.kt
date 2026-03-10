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

    /* IP del PC donde corre el servidor Node.js
    //Con Wifi, Ip de la laptop "http://192.168.0.107:3000"
    //Con el telefono 10.25.119.88:3000*/
    private const val BASE_URL =  "http://10.25.119.88:3000"
    fun connect() {
        if (socket != null && socket!!.connected()) {
            Log.d("APP_DEBUG_SocketManager", "Ya estamos conectados.")
            return
        }

        try {
            Log.d("APP_DEBUG_SocketManager", "Iniciando conexión a $BASE_URL...")
            socket = IO.socket(BASE_URL)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("APP_DEBUG_SocketManager", "¡CONECTADO AL SERVIDOR! ID: ${socket?.id()}")
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e("APP_DEBUG_SocketManager", "Error de conexión: ${args[0]}")
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d("APP_DEBUG_SocketManager", "Desconectado del servidor.")
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e("APP_DEBUG_SocketManager", "Excepción al conectar: ${e.message}")
            e.printStackTrace()
        }
    }

    fun isConnected(): Boolean = socket?.connected() ?: false

    fun joinRoom(roomId: String, playerJson: String, nickname: String) {
        if (!isConnected()) {
            Log.e("APP_DEBUG_SocketManager", "No se puede unir a sala: Socket no conectado.")
            return
        }
        val data = JSONObject()
        data.put("roomId", roomId)
        data.put("nickname", nickname)
        data.put("player", JSONObject(playerJson))
        socket?.emit("join_room", data)
        Log.d("APP_DEBUG_SocketManager", "Evento join_room enviado para sala $roomId")
    }

    fun startGame(roomId: String, gameStateJson: String) {
        val data = JSONObject()
        data.put("roomId", roomId)
        data.put("gameState", gameStateJson)
        socket?.emit("start_game", data)
    }

    fun observePlayers(): Flow<String> = callbackFlow {
        socket?.on("update_players") { args ->
            val data = args[0].toString()
            trySend(data)
        }
        awaitClose { socket?.off("update_players") }
    }

    fun observeGameStart(): Flow<String> = callbackFlow {
        socket?.on("game_started") { args ->
            val gameStateJson = args[0].toString()
            trySend(gameStateJson)
        }
        awaitClose { socket?.off("game_started") }
    }

    fun observeGameStateUpdates(): Flow<String> = callbackFlow {
        socket?.on("game_state_update") { args ->
            val gameStateJson = args[0].toString()
            trySend(gameStateJson)
        }
        awaitClose { socket?.off("game_state_update") }
    }

    fun submitWord(roomId: String, wordText: String, playerId: String) {
        val data = JSONObject()
        data.put("roomId", roomId)
        data.put("wordText", wordText)
        data.put("playerId", playerId)
        socket?.emit("submit_word", data)
    }

    fun toggleReveal(roomId: String) {
        val data = JSONObject()
        data.put("roomId", roomId)
        socket?.emit("toggle_reveal", data)
    }
}
