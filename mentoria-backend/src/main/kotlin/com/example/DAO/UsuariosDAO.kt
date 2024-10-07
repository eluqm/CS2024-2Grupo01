package com.example.DAO

import com.example.model.Usuarios
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement


class UsuariosDAO(private val connection: Connection) {
    companion object {
        private const val INSERT_USUARIO = "INSERT INTO usuarios (dni_usuario, nombre_usuario, apellido_usuario, celular_usuario, password_hash, semestre, email, tipo_usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        private const val SELECT_USUARIO_BY_ID = "SELECT * FROM usuarios WHERE user_id = ?"
        private const val UPDATE_USUARIO = "UPDATE usuarios SET nombre_usuario = ?, apellido_usuario = ?, celular_usuario = ?, password_hash = ?, semestre = ?, email = ?, tipo_usuario = ? WHERE user_id = ?"
        private const val DELETE_USUARIO = "DELETE FROM usuarios WHERE user_id = ?"
    }
    init {
        val statement = connection.createStatement()
    }
    suspend fun create(usuario: Usuarios): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_USUARIO, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, usuario.dni_usuario)
        statement.setString(2, usuario.nombre_usuario)
        statement.setString(3, usuario.apellido_usuario)
        statement.setString(4, usuario.celular_usuario)
        statement.setString(5, usuario.password_hash)
        statement.setString(6, usuario.semestre)
        statement.setString(7, usuario.email)
        statement.setString(8, usuario.tipo_usuario)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted user")
        }
    }

    suspend fun read(userId: Int): Usuarios = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_USUARIO_BY_ID)
        statement.setInt(1, userId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val user_id = resultSet.getInt("user_id")
            val dni_usuario = resultSet.getString("dni_usuario")
            val nombre_usuario = resultSet.getString("nombre_usuario")
            val apellido_usuario = resultSet.getString("apellido_usuario")
            val celular_usuario = resultSet.getString("celular_usuario")
            val password_hash = resultSet.getString("password_hash")
            val semestre = resultSet.getString("semestre")
            val email = resultSet.getString("email")
            val tipo_usuario = resultSet.getString("tipo_usuario")
            return@withContext Usuarios(user_id, dni_usuario, nombre_usuario, apellido_usuario, celular_usuario, password_hash, semestre, email, tipo_usuario, null)
        } else {
            throw Exception("User not found")
        }
    }

    suspend fun update(userId: Int, usuario: Usuarios) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_USUARIO)
        statement.setString(1, usuario.nombre_usuario)
        statement.setString(2, usuario.apellido_usuario)
        statement.setString(3, usuario.celular_usuario)
        statement.setString(4, usuario.password_hash)
        statement.setString(5, usuario.semestre)
        statement.setString(6, usuario.email)
        statement.setString(7, usuario.tipo_usuario)
        statement.setInt(8, userId)
        statement.executeUpdate()
    }

    suspend fun delete(userId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_USUARIO)
        statement.setInt(1, userId)
        statement.executeUpdate()
    }
}