package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

/**
 * Modelo de datos que representa un coordinador en el sistema.
 *
 * @property userId Identificador del usuario asociado al coordinador
 */
@Serializable
data class Coordinador(
    val userId: Int
)

/**
 * Servicio que gestiona las operaciones CRUD relacionadas con coordinadores.
 * Implementa operaciones asíncronas usando corrutinas de Kotlin.
 *
 * @property connection Conexión a la base de datos
 */
class CoordinadoresService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_COORDINADORES =
            "CREATE TABLE COORDINADORES (COORDINADOR_ID SERIAL PRIMARY KEY, USER_ID INT);"
        private const val INSERT_COORDINADOR = "INSERT INTO coordinadores (user_id) VALUES (?)"
        private const val SELECT_COORDINADOR_BY_ID = "SELECT * FROM coordinadores WHERE coordinador_id = ?"
        private const val UPDATE_COORDINADOR = "UPDATE coordinadores SET user_id = ? WHERE coordinador_id = ?"
        private const val DELETE_COORDINADOR = "DELETE FROM coordinadores WHERE coordinador_id = ?"
    }

    /**
     * Inicializa el servicio.
     * Contiene código comentado para crear la tabla si es necesario.
     */
    init {
        val statement = connection.createStatement()
        // Descomentar la siguiente línea para crear la tabla si es necesario
        // statement.executeUpdate(CREATE_TABLE_COORDINADORES)
    }

    /**
     * Crea un nuevo coordinador en la base de datos.
     *
     * @param coordinador Datos del coordinador a crear
     * @return ID generado para el nuevo coordinador
     * @throws Exception Si no se puede recuperar el ID del coordinador insertado
     */
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

    /**
     * Obtiene un coordinador por su ID.
     *
     * @param coordinadorId ID del coordinador a buscar
     * @return Objeto Coordinador con los datos del coordinador encontrado
     * @throws Exception Si el coordinador no existe
     */
    suspend fun read(coordinadorId: Int): Coordinador = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_COORDINADOR_BY_ID)
        statement.setInt(1, coordinadorId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Coordinador(
                userId = resultSet.getInt("user_id")
            )
        } else {
            throw Exception("Coordinador not found")
        }
    }

    /**
     * Actualiza los datos de un coordinador existente.
     *
     * @param coordinadorId ID del coordinador a actualizar
     * @param coordinador Nuevos datos del coordinador
     */
    suspend fun update(coordinadorId: Int, coordinador: Coordinador) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_COORDINADOR)
        statement.setInt(1, coordinador.userId)
        statement.setInt(2, coordinadorId)
        statement.executeUpdate()
    }

    /**
     * Elimina un coordinador por su ID.
     *
     * @param coordinadorId ID del coordinador a eliminar
     */
    suspend fun delete(coordinadorId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_COORDINADOR)
        statement.setInt(1, coordinadorId)
        statement.executeUpdate()
    }
}