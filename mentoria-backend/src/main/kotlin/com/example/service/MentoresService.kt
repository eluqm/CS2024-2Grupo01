package com.example.service

import com.example.DAO.MentoresDAO
import com.example.model.Mentor
import java.sql.Connection

class MentoresService(connection: Connection) {
    private val mentorDAO = MentoresDAO(connection)

    suspend fun create(mentor: Mentor): Int {
        return mentorDAO.create(mentor)
    }

    suspend fun read(id: Int): Mentor? {
        return try {
            mentorDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, mentor: Mentor) {
        mentorDAO.update(id, mentor)
    }

    suspend fun delete(id: Int) {
        mentorDAO.delete(id)
    }
}
