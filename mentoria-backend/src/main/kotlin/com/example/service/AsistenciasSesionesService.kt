package com.example.service

import com.example.DAO.AsistenciasSesionesDAO
import edu.cram.mentoriapp.Model.AsistenciaSesion
import java.sql.Connection

class AsistenciasSesionesService(connection: Connection) {
    private val asistenciaDAO = AsistenciasSesionesDAO(connection)

    suspend fun create(asistencia: AsistenciaSesion): Int {
        return asistenciaDAO.create(asistencia)
    }

    suspend fun read(id: Int): AsistenciaSesion? {
        return try {
            asistenciaDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, asistencia: AsistenciaSesion) {
        asistenciaDAO.update(id, asistencia)
    }

    suspend fun delete(id: Int) {
        asistenciaDAO.delete(id)
    }
}
