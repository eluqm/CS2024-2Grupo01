package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

data class MensajeGrupo(
    val grupoId: Int,
    val remitenteId: Int,
    val textoMensaje: String,
    val enviadoEn: String
)

class MensajesGrupoService(private val connection: Connection) {
    companion object {
        private const val INSERT_MENSAJE = "INSERT INTO mensajes_grupo (grupo_id, remitente_id, texto_mensaje) VALUES (?, ?, ?)"
        private const val SELECT_MENSAJE_BY_ID = "SELECT * FROM mensajes_grupo WHERE mensaje_id = ?"
        private const val UPDATE_MENSAJE = "UPDATE mensajes_grupo SET grupo_id = ?, remitente_id = ?, texto_mensaje = ? WHERE mensaje_id = ?"
        private const val DELETE_MENSAJE = "DELETE FROM mensajes_grupo WHERE mensaje_id = ?"
    }

    suspend fun create(mensaje: MensajeGrupo): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_MENSAJE, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, mensaje.grupoId)
        statement.setInt(2, mensaje.remitenteId)
        statement.setString(3, mensaje.textoMensaje)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted mensaje")
        }
    }

    suspend fun read(mensajeId: Int): MensajeGrupo = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_MENSAJE_BY_ID)
        statement.setInt(1, mensajeId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext MensajeGrupo(
                grupoId = resultSet.getInt("grupo_id"),
                remitenteId = resultSet.getInt("remitente_id"),
                textoMensaje = resultSet.getString("texto_mensaje"),
                enviadoEn = resultSet.getTimestamp("enviado_en").toLocalDateTime().toString()
            )
        } else {
            throw Exception("Mensaje not found")
        }
    }

    suspend fun update(mensajeId: Int, mensaje: MensajeGrupo) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_MENSAJE)
        statement.setInt(1, mensaje.grupoId)
        statement.setInt(2, mensaje.remitenteId)
        statement.setString(3, mensaje.textoMensaje)
        statement.setInt(4, mensajeId)
        statement.executeUpdate()
    }

    suspend fun delete(mensajeId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_MENSAJE)
        statement.setInt(1, mensajeId)
        statement.executeUpdate()
    }
}
