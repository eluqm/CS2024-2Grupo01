package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

/**
 * Modelo de datos que representa un mentoriado en el sistema.
 *
 * @property userId Identificador del usuario asociado al mentoriado
 */
@Serializable
data class Mentoriado(
    val userId: Int
)

/**
 * Servicio que gestiona las operaciones CRUD relacionadas con mentoriados.
 * Implementa operaciones asíncronas usando corrutinas de Kotlin.
 *
 * @property connection Conexión a la base de datos
 */
@Serializable
class MentoriadosService(private val connection: Connection) {
    companion object {
        private const val INSERT_MENTORIADO = "INSERT INTO mentoriados (user_id) VALUES (?)"
        private const val SELECT_MENTORIADO_BY_ID = "SELECT * FROM mentoriados WHERE mentoriado_id = ?"
        private const val UPDATE_MENTORIADO = "UPDATE mentoriados SET user_id = ? WHERE mentoriado_id = ?"
        private const val DELETE_MENTORIADO = "DELETE FROM mentoriados WHERE mentoriado_id = ?"
    }

    /**
     * Crea un nuevo mentoriado en la base de datos.
     *
     * @param mentoriado Datos del mentoriado a crear
     * @return ID generado para el nuevo mentoriado
     * @throws Exception Si no se puede recuperar el ID del mentoriado insertado
     */
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

    /**
     * Obtiene un mentoriado por su ID.
     *
     * @param mentoriadoId ID del mentoriado a buscar
     * @return Objeto Mentoriado con los datos del mentoriado encontrado
     * @throws Exception Si el mentoriado no existe
     */
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

    /**
     * Actualiza los datos de un mentoriado existente.
     *
     * @param mentoriadoId ID del mentoriado a actualizar
     * @param mentoriado Nuevos datos del mentoriado
     */
    suspend fun update(mentoriadoId: Int, mentoriado: Mentoriado) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_MENTORIADO)
        statement.setInt(1, mentoriado.userId)
        statement.setInt(2, mentoriadoId)
        statement.executeUpdate()
    }

    /**
     * Elimina un mentoriado por su ID.
     *
     * @param mentoriadoId ID del mentoriado a eliminar
     */
    suspend fun delete(mentoriadoId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_MENTORIADO)
        statement.setInt(1, mentoriadoId)
        statement.executeUpdate()
    }
}