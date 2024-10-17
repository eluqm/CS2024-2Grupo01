package com.example.service

import com.example.DAO.SesionesDAO
import com.example.model.SesionMentoria
import java.sql.Connection

class SesionesMentoriaService(connection: Connection) {
    private val sesionDAO = SesionesDAO(connection)

    suspend fun create(sesion: SesionMentoria): Int {
        return sesionDAO.create(sesion)
    }

    suspend fun read(id: Int): SesionMentoria? {
        return try {
            sesionDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, sesion: SesionMentoria) {
        sesionDAO.update(id, sesion)
    }

    suspend fun delete(id: Int) {
        sesionDAO.delete(id)
    }
}
