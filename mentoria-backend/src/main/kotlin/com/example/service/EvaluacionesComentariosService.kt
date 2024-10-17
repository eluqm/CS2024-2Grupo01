package com.example.service

import com.example.DAO.EvaluacionesComentariosDAO
import com.example.model.EvaluacionComentario
import java.sql.Connection

class EvaluacionesComentariosService(connection: Connection) {
    private val evaluacionDAO = EvaluacionesComentariosDAO(connection)

    suspend fun create(evaluacion: EvaluacionComentario): Int {
        return evaluacionDAO.create(evaluacion)
    }

    suspend fun read(id: Int): EvaluacionComentario? {
        return try {
            evaluacionDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, evaluacion: EvaluacionComentario) {
        evaluacionDAO.update(id, evaluacion)
    }

    suspend fun delete(id: Int) {
        evaluacionDAO.delete(id)
    }
}
