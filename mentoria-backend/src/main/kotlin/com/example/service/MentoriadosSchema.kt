package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

data class Mentoriado(
    val userId: Int
)@Serializable
class MentoriadosService(private val connection: Connection) {
    companion object {
        private const val INSERT_MENTORIADO = "INSERT INTO mentoriados (user_id) VALUES (?)"
        private const val SELECT_MENTORIADO_BY_ID = "SELECT * FROM mentoriados WHERE mentoriado_id = ?"
        private const val UPDATE_MENTORIADO = "UPDATE mentoriados SET user_id = ? WHERE mentoriado_id = ?"
        private const val DELETE_MENTORIADO = "DELETE FROM mentoriados WHERE mentoriado_id = ?"
    }

    suspend fun create(mentoriado: Mentoriado): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_MENTORIADO, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, mentoriado.userId)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted mentoriado")
        }
    }

    suspend fun read(mentoriadoId: Int): Mentoriado = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_MENTORIADO_BY_ID)
        statement.setInt(1, mentoriadoId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Mentoriado(
                userId = resultSet.getInt("user_id")
            )
        } else {
            throw Exception("Mentoriado not found")
        }
    }
    suspend fun update(mentoriadoId: Int, mentoriado: Mentoriado) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_MENTORIADO)
        statement.setInt(1, mentoriado.userId)
        statement.setInt(2, mentoriadoId)
        statement.executeUpdate()
    }
    suspend fun delete(mentoriadoId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_MENTORIADO)
        statement.setInt(1, mentoriadoId)
        statement.executeUpdate()
    }
}
