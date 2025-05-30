package com.Rivet.netwarriorlauncher

import kotlinx.coroutines.*
import java.net.*
import android.util.Log
import org.json.JSONObject
import java.io.IOException

data class HealthData(
    val batteryLevel: Int,
    val chargingStatus: Int,
    val timestamp: Long
)

class HealthDataListener {
    private var socket: DatagramSocket? = null
    private var isListening = false
    private var listenerJob: Job? = null

    companion object {
        private const val SERVER_PORT = 4001
        private const val BUFFER_SIZE = 1024
        private const val TAG = "HealthDataListener"
    }

    interface HealthDataCallback {
        fun onHealthDataReceived(healthData: HealthData)
        fun onError(error: String)
    }

    fun startListening(callback: HealthDataCallback) {
        if (isListening) {
            Log.w(TAG, "Already listening for health data")
            return
        }

        listenerJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                socket = DatagramSocket(SERVER_PORT)
                isListening = true
                Log.i(TAG, "Started listening on port $SERVER_PORT")

                val buffer = ByteArray(BUFFER_SIZE)

                while (isListening && !Thread.currentThread().isInterrupted) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket?.receive(packet)

                        val jsonData = String(packet.data, 0, packet.length)
                        Log.i(TAG, "Received health data: $jsonData")

                        val healthData = parseHealthData(jsonData)
                        if (healthData != null) {
                            withContext(Dispatchers.Main) {
                                callback.onHealthDataReceived(healthData)
                            }
                        }
                        // Send "OK" response back to client
                        try {
                            val response = "OK".toByteArray()
                            val responsePacket = DatagramPacket(
                                response,
                                response.size,
                                packet.address,  // Send back to sender's address
                                packet.port      // Send back to sender's port
                            )
                            socket?.send(responsePacket)
                            Log.i(TAG, "Sent OK response to ${packet.address}:${packet.port}")
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to send response: ${e.message}")
                        }

                    } catch (e: SocketTimeoutException) {
                        // Timeout is normal, continue listening
                        continue
                    } catch (e: IOException) {
                        if (isListening) {
                            Log.e(TAG, "Socket error: ${e.message}")
                            withContext(Dispatchers.Main) {
                                callback.onError("Socket error: ${e.message}")
                            }
                        }
                        break
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to start listening: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback.onError("Failed to start listening: ${e.message}")
                }
            }
        }
    }

    fun stopListening() {
        isListening = false
        socket?.close()
        listenerJob?.cancel()
        Log.i(TAG, "Stopped listening for health data")
    }

    private fun parseHealthData(jsonString: String): HealthData? {
        return try {
            val json = JSONObject(jsonString)
            HealthData(
                batteryLevel = json.getInt("battery_level"),
                chargingStatus = json.getInt("charging_status"),
                timestamp = json.getLong("timestamp")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON: ${e.message}")
            null
        }
    }
}