package com.example.service

import com.example.DAO.EscuelasDAO
import com.example.model.Escuela
import java.sql.Connection

class EscuelasService(connection: Connection) {
    private val escuelaDAO = EscuelasDAO(connection)

    suspend fun create(escuela: Escuela): Int {
        return escuelaDAO.create(escuela)
    }

    suspend fun read(id: Int): Escuela? {
        return try {
            escuelaDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, escuela: Escuela) {
        escuelaDAO.update(id, escuela)
    }

    suspend fun delete(id: Int) {
        escuelaDAO.delete(id)
    }
}
