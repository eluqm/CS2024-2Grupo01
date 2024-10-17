package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

data class Evento(
    val nombre: String,
    val horarioId: Int,
    val descripcion: String?,
    val poster: ByteArray,
    val url: String?
)

class EventosService(private val connection: Connection) {
    companion object {
        private const val INSERT_EVENTO = "INSERT INTO eventos (nombre, horario_id, descripcion, poster, url) VALUES (?, ?, ?, ?, ?)"
        private const val SELECT_EVENTO_BY_ID = "SELECT * FROM eventos WHERE evento_id = ?"
        private const val UPDATE_EVENTO = "UPDATE eventos SET nombre = ?, horario_id = ?, descripcion = ?, poster = ?, url = ? WHERE evento_id = ?"
        private const val DELETE_EVENTO = "DELETE FROM eventos WHERE evento_id = ?"
    }

    suspend fun create(evento: Evento): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_EVENTO, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, evento.nombre)
        statement.setInt(2, evento.horarioId)
        statement.setString(3, evento.descripcion)
        statement.setBytes(4, evento.poster)
        statement.setString(5, evento.url)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted evento")
        }
    }

    suspend fun read(eventoId: Int): Evento = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_EVENTO_BY_ID)
        statement.setInt(1, eventoId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Evento(
                nombre = resultSet.getString("nombre"),
                horarioId = resultSet.getInt("horario_id"),
                descripcion = resultSet.getString("descripcion"),
                poster = resultSet.getBytes("poster"),
                url = resultSet.getString("url")
            )
        } else {
            throw Exception("Evento not found")
        }
    }

    suspend fun update(eventoId: Int, evento: Evento) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_EVENTO)
        statement.setString(1, evento.nombre)
        statement.setInt(2, evento.horarioId)
        statement.setString(3, evento.descripcion)
        statement.setBytes(4, evento.poster)
        statement.setString(5, evento.url)
        statement.setInt(6, eventoId)
        statement.executeUpdate()
    }

    suspend fun delete(eventoId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_EVENTO)
        statement.setInt(1, eventoId)
        statement.executeUpdate()
    }
}
