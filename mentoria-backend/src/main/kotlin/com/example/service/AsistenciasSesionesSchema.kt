package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

import java.sql.Connection
import java.sql.Statement
import java.time.LocalDateTime

@Serializable
data class AsistenciaSesion(
    val sesionId: Int,
    val mentoriadoId: Int,
    val asistio: Boolean,
    @Contextual
    val horaFechaRegistrada: LocalDateTime?
)

class AsistenciasSesionesService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_ASISTENCIAS_SESIONES =
            "CREATE TABLE ASISTENCIAS_SESIONES (ASISTENCIA_ID SERIAL PRIMARY KEY, SESION_ID INT, MENTORIADO_ID INT, ASISTIO BOOLEAN, HORA_FECHA_REGISTRADA TIMESTAMP);"
        private const val INSERT_ASISTENCIA = "INSERT INTO asistencias_sesiones (sesion_id, mentoriado_id, asistio) VALUES (?, ?, ?)"
        private const val SELECT_ASISTENCIA_BY_ID = "SELECT * FROM asistencias_sesiones WHERE asistencia_id = ?"
        private const val UPDATE_ASISTENCIA = "UPDATE asistencias_sesiones SET sesion_id = ?, mentoriado_id = ?, asistio = ? WHERE asistencia_id = ?"
        private const val DELETE_ASISTENCIA = "DELETE FROM asistencias_sesiones WHERE asistencia_id = ?"
    }

    init {
        val statement = connection.createStatement()
        // Descomentar la siguiente línea para crear la tabla si es necesario
        // statement.executeUpdate(CREATE_TABLE_ASISTENCIAS_SESIONES)
    }

    // Crear nueva asistencia
    suspend fun create(asistencia: AsistenciaSesion): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_ASISTENCIA, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, asistencia.sesionId)
        statement.setInt(2, asistencia.mentoriadoId)
        statement.setBoolean(3, asistencia.asistio)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted asistencia")
        }
    }

    // Leer una asistencia
    suspend fun read(asistenciaId: Int): AsistenciaSesion = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ASISTENCIA_BY_ID)
        statement.setInt(1, asistenciaId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext AsistenciaSesion(
                sesionId = resultSet.getInt("sesion_id"),
                mentoriadoId = resultSet.getInt("mentoriado_id"),
                asistio = resultSet.getBoolean("asistio"),
                horaFechaRegistrada = resultSet.getTimestamp("hora_fecha_registrada")?.toLocalDateTime()
            )
        } else {
            throw Exception("Asistencia not found")
        }
    }

    // Actualizar una asistencia
    suspend fun update(asistenciaId: Int, asistencia: AsistenciaSesion) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_ASISTENCIA)
        statement.setInt(1, asistencia.sesionId)
        statement.setInt(2, asistencia.mentoriadoId)
        statement.setBoolean(3, asistencia.asistio)
        statement.setInt(4, asistenciaId)
        statement.executeUpdate()
    }

    // Eliminar una asistencia
    suspend fun delete(asistenciaId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_ASISTENCIA)
        statement.setInt(1, asistenciaId)
        statement.executeUpdate()
    }
}
