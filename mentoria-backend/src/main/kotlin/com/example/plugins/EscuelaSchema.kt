package com.example.plugins

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

@Serializable
data class Escuela(val nombre: String)

class EscuelasService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_ESCUELAS =
            "CREATE TABLE ESCUELAS (ESCUELA_ID SERIAL PRIMARY KEY, NOMBRE VARCHAR(255));"
        private const val SELECT_ESCUELA_BY_ID = "SELECT nombre FROM escuelas WHERE escuela_id = ?"
        private const val INSERT_ESCUELA = "INSERT INTO escuelas (nombre) VALUES (?)"
        private const val UPDATE_ESCUELA = "UPDATE escuelas SET nombre = ? WHERE escuela_id = ?"
        private const val DELETE_ESCUELA = "DELETE FROM escuelas WHERE escuela_id = ?"
    }

    init {
        val statement = connection.createStatement()
        // Descomentar la siguiente línea para crear la tabla si es necesario
        // statement.executeUpdate(CREATE_TABLE_ESCUELAS)
    }

    // Crear nueva escuela
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

    // Leer una escuela
    suspend fun read(id: Int): Escuela = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ESCUELA_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val nombre = resultSet.getString("nombre")
            return@withContext Escuela(nombre)
        } else {
            throw Exception("Record not found")
        }
    }

    // Actualizar una escuela
    suspend fun update(id: Int, escuela: Escuela) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_ESCUELA)
        statement.setString(1, escuela.nombre)
        statement.setInt(2, id)
        statement.executeUpdate()
    }

    // Eliminar una escuela
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_ESCUELA)
        statement.setInt(1, id)
        statement.executeUpdate()
    }
}
