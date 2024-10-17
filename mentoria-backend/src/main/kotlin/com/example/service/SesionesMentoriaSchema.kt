package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement
@Serializable
data class SesionMentoria(
    val grupoId: Int,
    val horaProgramada: String,
    val estado: String,
    val temaSesion: String,
    val notas: String?,
    val fotografia: ByteArray
)
class SesionesMentoriaService(private val connection: Connection) {
    companion object {
        private const val INSERT_SESION = "INSERT INTO sesiones (grupo_id, hora_programada, estado, tema_sesion , notas, fotografia) VALUES (?, ?, ?, ?, ?, ?)"
        private const val SELECT_SESION_BY_ID = "SELECT * FROM sesiones WHERE sesion_id = ?"
        private const val UPDATE_SESION = "UPDATE sesiones SET grupo_id = ?, hora_programada = ?, estado = ?, tema_sesion = ? , notas = ?, fotografia = ? WHERE grupo_id = ?"
        private const val DELETE_SESION = "DELETE FROM sesiones WHERE sesion_id = ?"
    }

    suspend fun create(sesion: SesionMentoria): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_SESION, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, sesion.grupoId)
        statement.setString(2, sesion.horaProgramada)
        statement.setString(3,sesion.estado)
        statement.setString(4,sesion.temaSesion)
        statement.setString(5,sesion.notas)
        statement.setBytes(6,sesion.fotografia)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted sesion")
        }
    }

    suspend fun read(sesionId: Int): SesionMentoria = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_SESION_BY_ID)
        statement.setInt(1, sesionId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext SesionMentoria(
                grupoId = resultSet.getInt("grupo_id"),
                horaProgramada = resultSet.getString("hora_programada"),
                estado = resultSet.getString("estado"),
                temaSesion = resultSet.getString("tema_sesion"),
                notas  = resultSet.getString("notas"),
                fotografia = resultSet.getBytes("fotografia")
            )
        } else {
            throw Exception("Sesion not found")
        }
    }

    suspend fun update(sesionId: Int, sesion: SesionMentoria) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_SESION)
        statement.setInt(1, sesion.grupoId)
        statement.setString(2, sesion.horaProgramada)
        statement.setString(3,sesion.estado)
        statement.setString(4,sesion.temaSesion)
        statement.setString(5,sesion.notas)
        statement.setBytes(6,sesion.fotografia)
        statement.setInt(7, sesionId)
        statement.executeUpdate()
    }

    suspend fun delete(sesionId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_SESION)
        statement.setInt(1, sesionId)
        statement.executeUpdate()
    }
}
