package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.util.Base64


@Serializable
data class Evento(
    val eventoId: Int? = null,
    val nombre: String,
    val horarioId: Int,
    val descripcion: String?,
    val poster: ByteArray,
    val url: String?,
    val fecha_evento: String
)

class EventosService(private val connection: Connection) {
    companion object {
        private const val INSERT_EVENTO = "INSERT INTO eventos (nombre, horario_id, descripcion, poster, url, fecha_evento) VALUES (?, ?, ?, ?, ?, ?)"
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
        val date = java.sql.Date.valueOf(evento.fecha_evento)  // Convierte el String a Date si es necesario
        statement.setDate(6, date)
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
                url = resultSet.getString("url"),
                fecha_evento = resultSet.getString("fecha_evento")
            )
        } else {
            throw Exception("Evento not found")
        }
    }

    suspend fun readAll(): List<Evento> = withContext(Dispatchers.IO) {
        val eventos = mutableListOf<Evento>()

        // Preparamos la consulta para obtener todos los eventos
        val statement = connection.prepareStatement("select * from eventos order by fecha_evento desc")
        val resultSet = statement.executeQuery()

        // Iteramos sobre el ResultSet para agregar los eventos a la lista
        while (resultSet.next()) {
            val evento = Evento(
                eventoId = resultSet.getInt("evento_id"),
                nombre = resultSet.getString("nombre"),
                horarioId = resultSet.getInt("horario_id"),
                descripcion = resultSet.getString("descripcion"),
                poster = resultSet.getBytes("poster"),
                url = resultSet.getString("url"),
                fecha_evento = resultSet.getString("fecha_evento")
            )
            eventos.add(evento)
        }

        return@withContext eventos
    }


    suspend fun update(eventoId: Int, evento: Evento) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_EVENTO)
        statement.setString(1, evento.nombre)
        statement.setInt(2, evento.horarioId)
        statement.setString(3, evento.descripcion)
        statement.setBytes(4, evento.poster)
        statement.setString(5, evento.url)
        statement.setString(6, evento.fecha_evento)
        statement.setInt(7, eventoId)
        statement.executeUpdate()
    }

    suspend fun delete(eventoId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_EVENTO)
        statement.setInt(1, eventoId)
        statement.executeUpdate()
    }
}
