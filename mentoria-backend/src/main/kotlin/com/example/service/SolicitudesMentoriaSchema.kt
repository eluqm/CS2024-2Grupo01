package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement
import java.time.LocalDateTime

@Serializable
data class SolicitudMentoria(
    val coordinadorId: Int,
    val mentorId: Int,
    @Contextual
    val fechaSolicitud: LocalDateTime?,
    val estado: String,
    val mensaje: String
)
class SolicitudesMentoriaService(private val connection: Connection) {
    companion object {
        private const val INSERT_SOLICITUD = "INSERT INTO solicitudes_mentoria (coordinador_id, mentor_id, estado, mensaje) VALUES (?, ?, ?, ?)"
        private const val SELECT_SOLICITUD_BY_ID = "SELECT * FROM solicitudes_mentoria WHERE solicitud_id = ?"
        private const val UPDATE_SOLICITUD = "UPDATE solicitudes_mentoria SET coordinador_id = ?, mentor_id = ?, estado = ?, mensaje = ? WHERE solicitud_id = ?"
        private const val DELETE_SOLICITUD = "DELETE FROM solicitudes_mentoria WHERE solicitud_id = ?"
    }

    suspend fun create(solicitud: SolicitudMentoria): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_SOLICITUD, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, solicitud.coordinadorId)
        statement.setInt(2, solicitud.mentorId)
        statement.setString(3, solicitud.estado)
        statement.setString(4, solicitud.mensaje)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted solicitud de mentoria")
        }
    }

    suspend fun read(solicitudId: Int): SolicitudMentoria = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_SOLICITUD_BY_ID)
        statement.setInt(1, solicitudId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext SolicitudMentoria(
                coordinadorId = resultSet.getInt("coordinador_id"),
                mentorId = resultSet.getInt("mentor_id"),
                estado = resultSet.getString("estado"),
                mensaje = resultSet.getString("mensaje"),
                fechaSolicitud = resultSet.getTimestamp("fecha_solicitud").toLocalDateTime()
            )
        } else {
            throw Exception("Solicitud not found")
        }
    }

    suspend fun update(solicitudId: Int, solicitud: SolicitudMentoria) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_SOLICITUD)
        statement.setInt(1, solicitud.coordinadorId)
        statement.setInt(2, solicitud.mentorId)
        statement.setString(3, solicitud.estado)
        statement.setString(4, solicitud.mensaje)
        statement.setInt(5, solicitudId)
        statement.executeUpdate()
    }

    suspend fun delete(solicitudId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_SOLICITUD)
        statement.setInt(1, solicitudId)
        statement.executeUpdate()
    }
}
