package com.example.service


import java.sql.Connection
import java.sql.Statement
import kotlinx.coroutines.*

class DeviceTokenService(private val connection: Connection) {
    companion object {
        private const val INSERT_OR_UPDATE_TOKEN = """
            INSERT INTO user_device_tokens (user_id, fcm_token)
            VALUES (?, ?)
            ON CONFLICT (fcm_token) DO UPDATE SET
                user_id = EXCLUDED.user_id
        """

        private const val GET_TOKENS_BY_USER_ID = """
            SELECT fcm_token FROM user_device_tokens WHERE user_id = ?
        """
    }

    // Registrar o actualizar token
    suspend fun registerToken(userId: Int, token: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val statement = connection.prepareStatement(INSERT_OR_UPDATE_TOKEN)
            statement.setInt(1, userId)
            statement.setString(2, token)
            statement.executeUpdate()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Obtener tokens de un usuario
    suspend fun getTokensByUserId(userId: Int): List<String> = withContext(Dispatchers.IO) {
        val tokens = mutableListOf<String>()
        val statement = connection.prepareStatement(GET_TOKENS_BY_USER_ID)
        statement.setInt(1, userId)
        val resultSet = statement.executeQuery()

        while (resultSet.next()) {
            tokens.add(resultSet.getString("fcm_token"))
        }

        tokens
    }
}