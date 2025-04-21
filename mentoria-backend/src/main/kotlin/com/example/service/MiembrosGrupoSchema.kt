package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

/**
 * Clase de datos que representa la membresía a un grupo.
 *
 * @property grupoId ID del grupo
 * @property userId ID del usuario que pertenece al grupo
 */
@Serializable
data class MiembroGrupo(
    val grupoId: Int,
    val userId: Int
)

/**
 * Clase de servicio para gestionar las membresías de grupos en la base de datos.
 * Proporciona operaciones CRUD para las membresías de grupos.
 *
 * @property connection Conexión activa a la base de datos
 */
class MiembrosGrupoService(private val connection: Connection) {
    companion object {
        // Consultas SQL
        private const val INSERT_MIEMBRO_GRUPO = "INSERT INTO miembros_grupo (grupo_id, user_id) VALUES (?, ?)"
        private const val SELECT_MIEMBRO_GRUPO_BY_ID = "SELECT * FROM miembros_grupo WHERE miembro_grupo_id = ?"
        private const val UPDATE_MIEMBRO_GRUPO = "UPDATE miembros_grupo SET grupo_id = ?, user_id = ? WHERE miembro_grupo_id = ?"
        private const val DELETE_MIEMBRO_GRUPO = "DELETE FROM miembros_grupo WHERE user_id = ?"
    }

    /**
     * Crea una nueva membresía de grupo en la base de datos.
     *
     * @param miembroGrupo Datos de la membresía de grupo a crear
     * @return ID de la membresía de grupo recién creada
     * @throws Exception si no se puede recuperar el ID generado
     */
    suspend fun create(miembroGrupo: MiembroGrupo): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_MIEMBRO_GRUPO, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, miembroGrupo.grupoId)
        statement.setInt(2, miembroGrupo.userId)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("No se puede recuperar el id del miembro de grupo recién insertado")
        }
    }

    /**
     * Recupera una membresía de grupo por su ID.
     *
     * @param miembroGrupoId ID de la membresía de grupo a recuperar
     * @return Objeto MiembroGrupo con los datos de la membresía
     * @throws Exception si no se encuentra la membresía
     */
    suspend fun read(miembroGrupoId: Int): MiembroGrupo = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_MIEMBRO_GRUPO_BY_ID)
        statement.setInt(1, miembroGrupoId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext MiembroGrupo(
                grupoId = resultSet.getInt("grupo_id"),
                userId = resultSet.getInt("user_id")
            )
        } else {
            throw Exception("Miembro del grupo no encontrado")
        }
    }

    /**
     * Actualiza una membresía de grupo existente.
     *
     * @param miembroGrupoId ID de la membresía a actualizar
     * @param miembroGrupo Datos actualizados de la membresía
     */
    suspend fun update(miembroGrupoId: Int, miembroGrupo: MiembroGrupo) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_MIEMBRO_GRUPO)
        statement.setInt(1, miembroGrupo.grupoId)
        statement.setInt(2, miembroGrupo.userId)
        statement.setInt(3, miembroGrupoId)
        statement.executeUpdate()
    }

    /**
     * Elimina una membresía de grupo por ID de usuario.
     *
     * @param miembroGrupoId ID del usuario a eliminar de las membresías
     */
    suspend fun delete(miembroGrupoId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_MIEMBRO_GRUPO)
        statement.setInt(1, miembroGrupoId)
        statement.executeUpdate()
    }
}