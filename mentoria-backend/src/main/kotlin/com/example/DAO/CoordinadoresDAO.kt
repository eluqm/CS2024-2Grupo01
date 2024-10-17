package com.example.DAO

import com.example.model.Coordinador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class CoordinadoresDAO(private val connection: Connection) {
    companion object {
        private const val INSERT_COORDINADOR = "INSERT INTO coordinadores (user_id) VALUES (?)"
        private const val SELECT_COORDINADOR_BY_ID = "SELECT * FROM coordinadores WHERE coordinador_id = ?"
        private const val UPDATE_COORDINADOR = "UPDATE coordinadores SET user_id = ? WHERE coordinador_id = ?"
        private const val DELETE_COORDINADOR = "DELETE FROM coordinadores WHERE coordinador_id = ?"
    }

    suspend fun create(coordinador: Coordinador): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_COORDINADOR, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, coordinador.userId)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted coordinador")
        }
    }

    suspend fun read(coordinadorId: Int): Coordinador = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_COORDINADOR_BY_ID)
        statement.setInt(1, coordinadorId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Coordinador(
                coordinadorId = resultSet.getInt("coordinador_id"),
                userId = resultSet.getInt("user_id")
            )
        } else {
            throw Exception("Coordinador not found")
        }
    }

    suspend fun update(coordinadorId: Int, coordinador: Coordinador) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_COORDINADOR)
        statement.setInt(1, coordinador.userId)
        statement.setInt(2, coordinadorId)
        statement.executeUpdate()
    }

    suspend fun delete(coordinadorId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_COORDINADOR)
        statement.setInt(1, coordinadorId)
        statement.executeUpdate()
    }
}
