package com.example.DAO

import com.example.model.MiembroGrupo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class MiembrosGrupoDAO(private val connection: Connection) {
    companion object {
        private const val INSERT_MIEMBRO_GRUPO = "INSERT INTO miembros_grupo (grupo_id, user_id) VALUES (?, ?)"
        private const val SELECT_MIEMBRO_GRUPO_BY_ID = "SELECT * FROM miembros_grupo WHERE miembro_grupo_id = ?"
        private const val UPDATE_MIEMBRO_GRUPO = "UPDATE miembros_grupo SET grupo_id = ?, user_id = ? WHERE miembro_grupo_id = ?"
        private const val DELETE_MIEMBRO_GRUPO = "DELETE FROM miembros_grupo WHERE miembro_grupo_id = ?"
    }

    suspend fun create(miembroGrupo: MiembroGrupo): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_MIEMBRO_GRUPO, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, miembroGrupo.grupoId)
        statement.setInt(2, miembroGrupo.userId)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted miembro de grupo")
        }
    }

    suspend fun read(miembroGrupoId: Int): MiembroGrupo = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_MIEMBRO_GRUPO_BY_ID)
        statement.setInt(1, miembroGrupoId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext MiembroGrupo(
                miembroGrupoId = resultSet.getInt("miembro_grupo_id"),
                grupoId = resultSet.getInt("grupo_id"),
                userId = resultSet.getInt("user_id")
            )
        } else {
            throw Exception("Miembro del grupo not found")
        }
    }

    suspend fun update(miembroGrupoId: Int, miembroGrupo: MiembroGrupo) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_MIEMBRO_GRUPO)
        statement.setInt(1, miembroGrupo.grupoId)
        statement.setInt(2, miembroGrupo.userId)
        statement.setInt(3, miembroGrupoId)
        statement.executeUpdate()
    }

    suspend fun delete(miembroGrupoId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_MIEMBRO_GRUPO)
        statement.setInt(1, miembroGrupoId)
        statement.executeUpdate()
    }
}
