package com.example.service

import com.example.DAO.GruposDAO
import com.example.model.Grupo
import java.sql.Connection

class GruposService(connection: Connection) {
    private val grupoDAO = GruposDAO(connection)

    suspend fun create(grupo: Grupo): Int {
        return grupoDAO.create(grupo)
    }

    suspend fun read(id: Int): Grupo? {
        return try {
            grupoDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, grupo: Grupo) {
        grupoDAO.update(id, grupo)
    }

    suspend fun delete(id: Int) {
        grupoDAO.delete(id)
    }
}
