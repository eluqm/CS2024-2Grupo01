package com.example.DAO

import com.example.model.Escuela
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class EscuelasDAO(private val connection: Connection) {
    companion object {
        private const val INSERT_ESCUELA = "INSERT INTO escuelas (nombre) VALUES (?)"
        private const val SELECT_ESCUELA_BY_ID = "SELECT * FROM escuelas WHERE escuela_id = ?"
        private const val UPDATE_ESCUELA = "UPDATE escuelas SET nombre = ? WHERE escuela_id = ?"
        private const val DELETE_ESCUELA = "DELETE FROM escuelas WHERE escuela_id = ?"
    }

    suspend fun create(escuela: Escuela): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_ESCUELA, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, escuela.nombre)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted school")
        }
    }

    suspend fun read(escuelaId: Int): Escuela = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ESCUELA_BY_ID)
        statement.setInt(1, escuelaId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Escuela(
                escuelaId = resultSet.getInt("escuela_id"),
                nombre = resultSet.getString("nombre")
            )
        } else {
            throw Exception("School not found")
        }
    }

    suspend fun update(escuelaId: Int, escuela: Escuela) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_ESCUELA)
        statement.setString(1, escuela.nombre)
        statement.setInt(2, escuelaId)
        statement.executeUpdate()
    }

    suspend fun delete(escuelaId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_ESCUELA)
        statement.setInt(1, escuelaId)
        statement.executeUpdate()
    }
}
