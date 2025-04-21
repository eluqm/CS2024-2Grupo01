package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

/**
 * Modelo de datos que representa un usuario con rol de psicología en el sistema.
 *
 * @property userId Identificador del usuario asociado
 */
@Serializable
data class Psicologia(
    val userId: Int
)

/**
 * Servicio que gestiona las operaciones CRUD relacionadas con usuarios de psicología.
 * Implementa operaciones asíncronas usando corrutinas de Kotlin.
 *
 * @property connection Conexión a la base de datos
 */
class PsicologiaService(private val connection: Connection) {
    companion object {
        private const val INSERT_PSICOLOGIA = "INSERT INTO psicologia (user_id) VALUES (?)"
        private const val SELECT_PSICOLOGIA_BY_ID = "SELECT * FROM psicologia WHERE psicologia_id = ?"
        private const val UPDATE_PSICOLOGIA = "UPDATE psicologia SET user_id = ? WHERE psicologia_id = ?"
        private const val DELETE_PSICOLOGIA = "DELETE FROM psicologia WHERE psicologia_id = ?"
    }

    /**
     * Crea un nuevo registro de usuario psicólogo en la base de datos.
     *
     * @param psicologia Datos del psicólogo a crear
     * @return ID generado para el nuevo registro de psicología
     * @throws Exception Si no se puede recuperar el ID del registro insertado
     */
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

    /**
     * Obtiene un registro de psicología por su ID.
     *
     * @param psicologiaId ID del registro de psicología a buscar
     * @return Objeto Psicologia con los datos del registro encontrado
     * @throws Exception Si el registro no existe
     */
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

    /**
     * Actualiza un registro de psicología existente.
     *
     * @param psicologiaId ID del registro de psicología a actualizar
     * @param psicologia Nuevos datos del registro
     */
    suspend fun update(psicologiaId: Int, psicologia: Psicologia) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_PSICOLOGIA)
        statement.setInt(1, psicologia.userId)
        statement.setInt(2, psicologiaId)
        statement.executeUpdate()
    }

    /**
     * Elimina un registro de psicología por su ID.
     *
     * @param psicologiaId ID del registro de psicología a eliminar
     */
    suspend fun delete(psicologiaId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_PSICOLOGIA)
        statement.setInt(1, psicologiaId)
        statement.executeUpdate()
    }
}