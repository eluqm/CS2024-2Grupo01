package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

data class Psicologia(
    val userId: Int
)


class PsicologiaService(private val connection: Connection) {
    companion object {
        private const val INSERT_PSICOLOGIA = "INSERT INTO psicologia (user_id) VALUES (?)"
        private const val SELECT_PSICOLOGIA_BY_ID = "SELECT * FROM psicologia WHERE psicologia_id = ?"
        private const val UPDATE_PSICOLOGIA = "UPDATE psicologia SET user_id = ? WHERE psicologia_id = ?"
        private const val DELETE_PSICOLOGIA = "DELETE FROM psicologia WHERE psicologia_id = ?"
    }

    suspend fun create(psicologia: Psicologia): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_PSICOLOGIA, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, psicologia.userId)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted psicologia")
        }
    }

    suspend fun read(psicologiaId: Int): Psicologia = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_PSICOLOGIA_BY_ID)
        statement.setInt(1, psicologiaId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Psicologia(
                userId = resultSet.getInt("user_id")
            )
        } else {
            throw Exception("Psicologia not found")
        }
    }

    suspend fun update(psicologiaId: Int, psicologia: Psicologia) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_PSICOLOGIA)
        statement.setInt(1, psicologia.userId)
        statement.setInt(2, psicologiaId)
        statement.executeUpdate()
    }

    suspend fun delete(psicologiaId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_PSICOLOGIA)
        statement.setInt(1, psicologiaId)
        statement.executeUpdate()
    }
}
