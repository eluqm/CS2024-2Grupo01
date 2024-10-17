package com.example.service

import com.example.DAO.SolicitudesMentoriaDAO
import com.example.model.SolicitudMentoria
import java.sql.Connection

class SolicitudesMentoriaService(connection: Connection) {
    private val solicitudDAO = SolicitudesMentoriaDAO(connection)

    suspend fun create(solicitud: SolicitudMentoria): Int {
        return solicitudDAO.create(solicitud)
    }

    suspend fun read(id: Int): SolicitudMentoria? {
        return try {
            solicitudDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, solicitud: SolicitudMentoria) {
        solicitudDAO.update(id, solicitud)
    }

    suspend fun delete(id: Int) {
        solicitudDAO.delete(id)
    }
}
