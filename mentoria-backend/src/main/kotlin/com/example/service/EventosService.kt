package com.example.service

import com.example.DAO.EventosDAO
import com.example.model.Evento
import java.sql.Connection

class EventosService(connection: Connection) {
    private val eventoDAO = EventosDAO(connection)

    suspend fun create(evento: Evento): Int {
        return eventoDAO.create(evento)
    }

    suspend fun read(id: Int): Evento? {
        return try {
            eventoDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, evento: Evento) {
        eventoDAO.update(id, evento)
    }

    suspend fun delete(id: Int) {
        eventoDAO.delete(id)
    }
}
