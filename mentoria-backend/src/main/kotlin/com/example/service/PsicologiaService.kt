package com.example.service

import com.example.DAO.PsicologiaDAO
import edu.cram.mentoriapp.Model.Psicologia
import java.sql.Connection

class PsicologiaService(connection: Connection) {
    private val psicologiaDAO = PsicologiaDAO(connection)

    suspend fun create(psicologia: Psicologia): Int {
        return psicologiaDAO.create(psicologia)
    }

    suspend fun read(id: Int): Psicologia? {
        return try {
            psicologiaDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, psicologia: Psicologia) {
        psicologiaDAO.update(id, psicologia)
    }

    suspend fun delete(id: Int) {
        psicologiaDAO.delete(id)
    }
}
