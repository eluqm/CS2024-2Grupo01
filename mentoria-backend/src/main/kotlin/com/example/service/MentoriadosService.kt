package com.example.service

import com.example.DAO.MentoriadosDAO
import com.example.model.Mentoriado
import java.sql.Connection

class MentoriadosService(connection: Connection) {
    private val mentoriadoDAO = MentoriadosDAO(connection)

    suspend fun create(mentoriado: Mentoriado): Int {
        return mentoriadoDAO.create(mentoriado)
    }

    suspend fun read(id: Int): Mentoriado? {
        return try {
            mentoriadoDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, mentoriado: Mentoriado) {
        mentoriadoDAO.update(id, mentoriado)
    }

    suspend fun delete(id: Int) {
        mentoriadoDAO.delete(id)
    }
}
