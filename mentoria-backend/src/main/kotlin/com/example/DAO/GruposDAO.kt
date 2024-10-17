package com.example.DAO

import com.example.model.Grupo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class GruposDAO(private val connection: Connection) {
    companion object {
        private const val INSERT_GRUPO = "INSERT INTO grupos (jefe_id, nombre, horario_id, descripcion, creado_en) VALUES (?, ?, ?, ?, ?)"
        private const val SELECT_GRUPO_BY_ID = "SELECT * FROM grupos WHERE grupo_id = ?"
        private const val UPDATE_GRUPO = "UPDATE grupos SET jefe_id = ?, nombre = ?, horario_id = ?, descripcion = ? WHERE grupo_id = ?"
        private const val DELETE_GRUPO = "DELETE FROM grupos WHERE grupo_id = ?"
    }

    suspend fun create(grupo: Grupo): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_GRUPO, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, grupo.jefeId)
        statement.setString(2, grupo.nombre)
        statement.setInt(3, grupo.horarioId)
        statement.setString(4, grupo.descripcion)
        statement.setString(5, grupo.creadoEn)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted grupo")
        }
    }

    suspend fun read(grupoId: Int): Grupo = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_GRUPO_BY_ID)
        statement.setInt(1, grupoId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Grupo(
                grupoId = resultSet.getInt("grupo_id"),
                jefeId = resultSet.getInt("jefe_id"),
                nombre = resultSet.getString("nombre_grupo"),
                horarioId = resultSet.getInt("horario_id"),
                descripcion = resultSet.getString("descripcion"),
                creadoEn = resultSet.getString("fecha_creacion")
            )
        } else {
            throw Exception("Grupo not found")
        }
    }

    suspend fun update(grupoId: Int, grupo: Grupo) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_GRUPO)
        statement.setInt(1, grupo.jefeId)
        statement.setString(2, grupo.nombre)
        statement.setInt(3, grupo.horarioId)
        statement.setString(4, grupo.descripcion)
        statement.setInt(5, grupoId)
        statement.executeUpdate()
    }

    suspend fun delete(grupoId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_GRUPO)
        statement.setInt(1, grupoId)
        statement.executeUpdate()
    }
}
