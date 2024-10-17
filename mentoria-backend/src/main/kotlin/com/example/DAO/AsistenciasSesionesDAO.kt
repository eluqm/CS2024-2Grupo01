package com.example.DAO


import edu.cram.mentoriapp.Model.AsistenciaSesion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class AsistenciasSesionesDAO(private val connection: Connection) {
    companion object {
        private const val INSERT_ASISTENCIA = "INSERT INTO asistencias_sesiones (sesion_id, mentoriado_id, asistio) VALUES (?, ?, ?)"
        private const val SELECT_ASISTENCIA_BY_ID = "SELECT * FROM asistencias_sesiones WHERE asistencia_id = ?"
        private const val UPDATE_ASISTENCIA = "UPDATE asistencias_sesiones SET sesion_id = ?, mentoriado_id = ?, asistio = ? WHERE asistencia_id = ?"
        private const val DELETE_ASISTENCIA = "DELETE FROM asistencias_sesiones WHERE asistencia_id = ?"
    }

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

    suspend fun read(asistenciaId: Int): AsistenciaSesion = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ASISTENCIA_BY_ID)
        statement.setInt(1, asistenciaId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext AsistenciaSesion(
                asistenciaId = resultSet.getInt("asistencia_id"),
                sesionId = resultSet.getInt("sesion_id"),
                mentoriadoId = resultSet.getInt("mentoriado_id"),
                asistio = resultSet.getBoolean("asistio"),
                horaFechaRegistrada = resultSet.getTimestamp("hora_fecha_registrada").toLocalDateTime()
            )
        } else {
            throw Exception("Asistencia not found")
        }
    }

    suspend fun update(asistenciaId: Int, asistencia: AsistenciaSesion) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_ASISTENCIA)
        statement.setInt(1, asistencia.sesionId)
        statement.setInt(2, asistencia.mentoriadoId)
        statement.setBoolean(3, asistencia.asistio)
        statement.setInt(4, asistenciaId)
        statement.executeUpdate()
    }

    suspend fun delete(asistenciaId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_ASISTENCIA)
        statement.setInt(1, asistenciaId)
        statement.executeUpdate()
    }
}
