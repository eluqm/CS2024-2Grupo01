package com.example.service

import com.example.DAO.NotificacionesDAO
import com.example.model.Notificacion
import java.sql.Connection

class NotificacionesService(connection: Connection) {
    private val notificacionDAO = NotificacionesDAO(connection)

    suspend fun create(notificacion: Notificacion): Int {
        return notificacionDAO.create(notificacion)
    }

    suspend fun read(id: Int): Notificacion? {
        return try {
            notificacionDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, notificacion: Notificacion) {
        notificacionDAO.update(id, notificacion)
    }

    suspend fun delete(id: Int) {
        notificacionDAO.delete(id)
    }
}
