package com.example.service

import com.example.DAO.MensajesGrupoDAO
import com.example.model.MensajeGrupo
import java.sql.Connection

class MensajesGrupoService(connection: Connection) {
    private val mensajeDAO = MensajesGrupoDAO(connection)

    suspend fun create(mensaje: MensajeGrupo): Int {
        return mensajeDAO.create(mensaje)
    }

    suspend fun read(id: Int): MensajeGrupo? {
        return try {
            mensajeDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, mensaje: MensajeGrupo) {
        mensajeDAO.update(id, mensaje)
    }

    suspend fun delete(id: Int) {
        mensajeDAO.delete(id)
    }
}

