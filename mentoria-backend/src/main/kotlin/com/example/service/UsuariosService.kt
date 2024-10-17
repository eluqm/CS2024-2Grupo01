package com.example.service

import com.example.DAO.UsuariosDAO
import com.example.model.Usuarios
import java.sql.Connection

class UsuariosService(connection: Connection) {
    private val usuarioDAO = UsuariosDAO(connection)

    suspend fun create(user: Usuarios): Int {
        return usuarioDAO.create(user)
    }

    suspend fun read(id: Int): Usuarios? {
        return try {
            usuarioDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, user: Usuarios) {
        usuarioDAO.update(id, user)
    }

    suspend fun delete(id: Int) {
        usuarioDAO.delete(id)
    }
}
