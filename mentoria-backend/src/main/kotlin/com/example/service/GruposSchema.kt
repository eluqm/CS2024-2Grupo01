package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

@Serializable
data class Grupo(
    val grupoId: Int? = null,
    val jefeId: Int,
    val nombre: String,
    val horarioId: Int,
    val descripcion: String?,
    val creadoEn: String? = null
)

@Serializable
data class Usuario(
    val id: Int,
    val nombreUsuario: String,
    val email: String,
    val celularUsuario: String,
    val dniUsuario: String
)

class GruposService(private val connection: Connection) {
    companion object {
        private const val INSERT_GRUPO = "INSERT INTO grupos (jefe_id, nombre, horario_id, descripcion) VALUES (?, ?, ?, ?)"
        private const val SELECT_GRUPO_BY_ID = "SELECT * FROM grupos WHERE grupo_id = ?"
        private const val UPDATE_GRUPO = "UPDATE grupos SET jefe_id = ?, nombre = ?, horario_id = ?, descripcion = ? WHERE grupo_id = ?"
        private const val DELETE_GRUPO = "DELETE FROM grupos WHERE grupo_id = ?"

        // Nuevas consultas
        private const val SELECT_GRUPOS_BY_MENTOR = "SELECT * FROM grupos WHERE jefe_id = ?"
        private const val SELECT_MENTORIADOS_BY_GRUPO = """
            SELECT u.usuario_id, u.nombre_usuario, u.email, u.celular_usuario, u.dni_usuario
            FROM usuarios u
            JOIN miembros_grupo mg ON u.usuario_id = mg.usuario_id
            WHERE mg.grupo_id = ? AND u.tipo_usuario = 'mentoriado'
        """
    }

    // Crear un nuevo grupo
    suspend fun create(grupo: Grupo): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_GRUPO, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, grupo.jefeId)
        statement.setString(2, grupo.nombre)
        statement.setInt(3, grupo.horarioId)
        statement.setString(4, grupo.descripcion)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted grupo")
        }
    }

    // Leer un grupo por su ID
    suspend fun read(grupoId: Int): Grupo = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_GRUPO_BY_ID)
        statement.setInt(1, grupoId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Grupo(
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

    // Actualizar un grupo
    suspend fun update(grupoId: Int, grupo: Grupo) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_GRUPO)
        statement.setInt(1, grupo.jefeId)
        statement.setString(2, grupo.nombre)
        statement.setInt(3, grupo.horarioId)
        statement.setString(4, grupo.descripcion)
        statement.setInt(5, grupoId)
        statement.executeUpdate()
    }

    // Eliminar un grupo
    suspend fun delete(grupoId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_GRUPO)
        statement.setInt(1, grupoId)
        statement.executeUpdate()
    }

    // Obtener los grupos de un mentor
    suspend fun getGruposPorMentor(mentorId: Int): List<Grupo> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_GRUPOS_BY_MENTOR)
        statement.setInt(1, mentorId)
        val resultSet = statement.executeQuery()

        val grupos = mutableListOf<Grupo>()
        while (resultSet.next()) {
            grupos.add(Grupo(
                jefeId = resultSet.getInt("jefe_id"),
                nombre = resultSet.getString("nombre"),
                horarioId = resultSet.getInt("horario_id"),
                descripcion = resultSet.getString("descripcion"),
                creadoEn = resultSet.getString("creado_en")
            ))
        }
        return@withContext grupos
    }

    // Obtener los usuarios mentoriados de un grupo específico
    suspend fun getUsuariosMentoriadosPorGrupo(grupoId: Int): List<Usuario> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_MENTORIADOS_BY_GRUPO)
        statement.setInt(1, grupoId)
        val resultSet = statement.executeQuery()

        val usuarios = mutableListOf<Usuario>()
        while (resultSet.next()) {
            usuarios.add(
                Usuario(
                    id = resultSet.getInt("usuario_id"),
                    nombreUsuario = resultSet.getString("nombre_usuario"),
                    email = resultSet.getString("email"),
                    celularUsuario = resultSet.getString("celular_usuario"),
                    dniUsuario = resultSet.getString("dni_usuario")
                )
            )
        }
        return@withContext usuarios
    }

    suspend fun readAllByEscuelaId(escuelaId: Int): List<Grupo> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(
            """
        SELECT *
        FROM grupos g
        JOIN usuarios u ON g.jefe_id = u.user_id
        WHERE u.escuela_id = ?
        """
        )
        statement.setInt(1, escuelaId)
        val resultSet = statement.executeQuery()

        val grupos = mutableListOf<Grupo>()

        while (resultSet.next()) {
            grupos.add(
                Grupo(
                    grupoId = resultSet.getInt("grupo_id"),
                    jefeId = resultSet.getInt("jefe_id"),
                    nombre = resultSet.getString("nombre"),
                    horarioId = resultSet.getInt("horario_id"),
                    descripcion = resultSet.getString("descripcion"),
                    creadoEn = resultSet.getString("creado_en")
                )
            )
        }

        if (grupos.isEmpty()) {
            throw Exception("No se encontraron grupos para el escuela_id proporcionado.")
        }

        return@withContext grupos
    }


}
