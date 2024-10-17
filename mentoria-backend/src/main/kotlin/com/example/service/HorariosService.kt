package com.example.service

import com.example.DAO.HorariosDAO
import com.example.model.Horario
import java.sql.Connection

class HorariosService(connection: Connection) {
    private val horarioDAO = HorariosDAO(connection)

    suspend fun create(horario: Horario): Int {
        return horarioDAO.create(horario)
    }

    suspend fun read(id: Int): Horario? {
        return try {
            horarioDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, horario: Horario) {
        horarioDAO.update(id, horario)
    }

    suspend fun delete(id: Int) {
        horarioDAO.delete(id)
    }
}
