package com.example.service

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.sql.Connection

/**
 * Modelo de datos que representa un token FCM de dispositivo de usuario.
 *
 * @property fcmToken Token FCM del dispositivo
 */
@Serializable
data class FCMToken(val fcmToken: String)

/**
 * Servicio que gestiona las operaciones relacionadas con tokens FCM de dispositivos.
 *
 * @property connection Conexión a la base de datos
 */
class TokensService(private val connection: Connection) {
    companion object {
        private const val SELECT_TOKENS_PSICOLOGIA =
            "SELECT udt.fcm_token FROM usuarios u JOIN user_device_tokens udt ON u.user_id = udt.user_id WHERE u.tipo_usuario = 'psicologia'"
        private const val SELECT_TOKENS_BY_HORARIO =
            "SELECT udt.fcm_token FROM grupos g JOIN usuarios u ON g.jefe_id = u.user_id JOIN user_device_tokens udt ON u.user_id = udt.user_id WHERE g.horario_id = ?"
        private const val SELECT_TOKENS_BY_GRUPO_HORARIO =
            "SELECT udt.fcm_token FROM user_device_tokens udt JOIN usuarios u ON udt.user_id = u.user_id JOIN miembros_grupo mg ON u.user_id = mg.user_id JOIN grupos g ON mg.grupo_id = g.grupo_id WHERE g.horario_id = ?"
    }

    /**
     * Obtiene todos los tokens FCM de usuarios de tipo psicologia.
     *
     * @return Lista de tokens FCM
     */
    suspend fun getTokensPsicologia(): List<FCMToken> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_TOKENS_PSICOLOGIA)
        val resultSet = statement.executeQuery()

        val tokens = mutableListOf<FCMToken>()
        while (resultSet.next()) {
            val fcmToken = resultSet.getString("fcm_token")
            tokens.add(FCMToken(fcmToken))
        }

        return@withContext tokens
    }

    /**
     * Obtiene los tokens FCM de los jefes de grupos con un horario específico.
     *
     * @param horarioId ID del horario
     * @return Lista de tokens FCM
     */
    suspend fun getTokensByHorario(horarioId: Int): List<FCMToken> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_TOKENS_BY_HORARIO)
        statement.setInt(1, horarioId)
        val resultSet = statement.executeQuery()

        val tokens = mutableListOf<FCMToken>()
        while (resultSet.next()) {
            val fcmToken = resultSet.getString("fcm_token")
            tokens.add(FCMToken(fcmToken))
        }

        return@withContext tokens
    }

    /**
     * Obtiene los tokens FCM de todos los miembros de grupos con un horario específico.
     *
     * @param horarioId ID del horario
     * @return Lista de tokens FCM
     */
    suspend fun getTokensByGrupoHorario(horarioId: Int): List<FCMToken> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_TOKENS_BY_GRUPO_HORARIO)
        statement.setInt(1, horarioId)
        val resultSet = statement.executeQuery()

        val tokens = mutableListOf<FCMToken>()
        while (resultSet.next()) {
            val fcmToken = resultSet.getString("fcm_token")
            tokens.add(FCMToken(fcmToken))
        }

        return@withContext tokens
    }
}