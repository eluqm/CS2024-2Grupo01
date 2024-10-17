package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement
@Serializable
data class Mentor(
    val userId: Int
)
class MentoresService(private val connection: Connection) {
    companion object {
        private const val INSERT_MENTOR = "INSERT INTO mentores (user_id) VALUES (?)"
        private const val SELECT_MENTOR_BY_ID = "SELECT * FROM mentores WHERE mentor_id = ?"
        private const val UPDATE_MENTOR = "UPDATE mentores SET user_id = ? WHERE mentor_id = ?"
        private const val DELETE_MENTOR = "DELETE FROM mentores WHERE mentor_id = ?"
    }

    suspend fun create(mentor: Mentor): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_MENTOR, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, mentor.userId)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted mentor")
        }
    }

    suspend fun read(mentorId: Int): Mentor = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_MENTOR_BY_ID)
        statement.setInt(1, mentorId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Mentor(
                userId = resultSet.getInt("user_id")
            )
        } else {
            throw Exception("Mentor not found")
        }
    }

    suspend fun update(mentorId: Int, mentor: Mentor) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_MENTOR)
        statement.setInt(1, mentor.userId)
        statement.setInt(3, mentorId)
        statement.executeUpdate()
    }

    suspend fun delete(mentorId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_MENTOR)
        statement.setInt(1, mentorId)
        statement.executeUpdate()
    }
}
