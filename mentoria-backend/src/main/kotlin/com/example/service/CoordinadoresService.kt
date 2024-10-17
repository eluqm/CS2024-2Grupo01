package com.example.service

import com.example.DAO.CoordinadoresDAO
import com.example.model.Coordinador
import java.sql.Connection

class CoordinadoresService(connection: Connection) {
    private val coordinadorDAO = CoordinadoresDAO(connection)

    suspend fun create(coordinador: Coordinador): Int {
        return coordinadorDAO.create(coordinador)
    }

    suspend fun read(id: Int): Coordinador? {
        return try {
            coordinadorDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, coordinador: Coordinador) {
        coordinadorDAO.update(id, coordinador)
    }

    suspend fun delete(id: Int) {
        coordinadorDAO.delete(id)
    }
}
