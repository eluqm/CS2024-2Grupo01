package com.example.service

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

/**
 * Modelo de datos que representa una escuela en el sistema.
 *
 * @property escuelaId Identificador único de la escuela (auto-generado)
 * @property nombre Nombre de la escuela
 */
@Serializable
data class Escuela(val escuelaId: Int?, val nombre: String)

/**
 * Servicio que gestiona las operaciones CRUD relacionadas con escuelas.
 * Implementa operaciones asíncronas usando corrutinas de Kotlin.
 *
 * @property connection Conexión a la base de datos
 */
class EscuelasService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_ESCUELAS =
            "CREATE TABLE ESCUELAS (ESCUELA_ID SERIAL PRIMARY KEY, NOMBRE VARCHAR(255));"
        private const val SELECT_ESCUELA_BY_ID = "SELECT * FROM escuelas WHERE escuela_id = ?"
        private const val INSERT_ESCUELA = "INSERT INTO escuelas (nombre) VALUES (?)"
        private const val UPDATE_ESCUELA = "UPDATE escuelas SET nombre = ? WHERE escuela_id = ?"
        private const val DELETE_ESCUELA = "DELETE FROM escuelas WHERE escuela_id = ?"
        private const val SELECT_ALL_ESCUELAS = "SELECT * FROM escuelas"
    }

    /**
     * Inicializa el servicio.
     * Contiene código comentado para crear la tabla si es necesario.
     */
    init {
        val statement = connection.createStatement()
        // Descomentar la siguiente línea para crear la tabla si es necesario
        // statement.executeUpdate(CREATE_TABLE_ESCUELAS)
    }

    /**
     * Crea una nueva escuela en la base de datos.
     *
     * @param escuela Datos de la escuela a crear
     * @return ID generado para la nueva escuela
     * @throws Exception Si no se puede recuperar el ID de la escuela insertada
     */
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

    /**
     * Obtiene una escuela por su ID.
     *
     * @param id ID de la escuela a buscar
     * @return Objeto Escuela con los datos de la escuela encontrada
     * @throws Exception Si la escuela no existe
     */
    suspend fun read(id: Int): Escuela = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ESCUELA_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val idSchool = resultSet.getInt("escuela_id")
            val nombre = resultSet.getString("nombre")
            return@withContext Escuela(idSchool, nombre)
        } else {
            throw Exception("Record not found")
        }
    }

    /**
     * Obtiene todas las escuelas registradas en el sistema.
     *
     * @return Lista de todas las escuelas
     */
    suspend fun readAll(): List<Escuela> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ALL_ESCUELAS)
        val resultSet = statement.executeQuery()

        val escuelas = mutableListOf<Escuela>()

        while (resultSet.next()) {
            val id = resultSet.getInt("escuela_id")
            val nombre = resultSet.getString("nombre")
            escuelas.add(Escuela(id, nombre))
        }

        return@withContext escuelas
    }

    /**
     * Actualiza los datos de una escuela existente.
     *
     * @param id ID de la escuela a actualizar
     * @param escuela Nuevos datos de la escuela
     */
    suspend fun update(id: Int, escuela: Escuela) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_ESCUELA)
        statement.setString(1, escuela.nombre)
        statement.setInt(2, id)
        statement.executeUpdate()
    }

    /**
     * Elimina una escuela por su ID.
     *
     * @param id ID de la escuela a eliminar
     */
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_ESCUELA)
        statement.setInt(1, id)
        statement.executeUpdate()
    }
}