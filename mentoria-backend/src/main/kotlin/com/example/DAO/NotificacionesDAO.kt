package com.example.DAO

import com.example.model.Notificacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class NotificacionesDAO(private val connection: Connection) {
    companion object {
        private const val INSERT_NOTIFICACION = "INSERT INTO notificaciones (user_id, texto_notificacion, tipo_notificacion, evento_id) VALUES (?, ?, ?, ?)"
        private const val SELECT_NOTIFICACION_BY_ID = "SELECT * FROM notificaciones WHERE notificacion_id = ?"
        private const val UPDATE_NOTIFICACION = "UPDATE notificaciones SET user_id = ?, texto_notificacion = ?, tipo_notificacion = ?, leido = ?, evento_id = ? WHERE notificacion_id = ?"
        private const val DELETE_NOTIFICACION = "DELETE FROM notificaciones WHERE notificacion_id = ?"
    }

    suspend fun create(notificacion: Notificacion): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_NOTIFICACION, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, notificacion.userId)
        statement.setString(2, notificacion.textoNotificacion)
        statement.setString(3, notificacion.tipoNotificacion)
        statement.setInt(4, notificacion.eventoId)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted notificacion")
        }
    }

    suspend fun read(notificacionId: Int): Notificacion = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_NOTIFICACION_BY_ID)
        statement.setInt(1, notificacionId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Notificacion(
                notificacionId = resultSet.getInt("notificacion_id"),
                userId = resultSet.getInt("user_id"),
                textoNotificacion = resultSet.getString("texto_notificacion"),
                tipoNotificacion = resultSet.getString("tipo_notificacion"),
                creadoEn = resultSet.getString("creado_en"),
                leido = resultSet.getBoolean("leido"),
                eventoId = resultSet.getInt("evento_id")
            )
        } else {
            throw Exception("Notificacion not found")
        }
    }

    suspend fun update(notificacionId: Int, notificacion: Notificacion) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_NOTIFICACION)
        statement.setInt(1, notificacion.userId)
        statement.setString(2, notificacion.textoNotificacion)
        statement.setString(3, notificacion.tipoNotificacion)
        statement.setBoolean(4,notificacion.leido)
        statement.setInt(6, notificacion.eventoId)
        statement.setInt(7, notificacionId)
        statement.executeUpdate()
    }

    suspend fun delete(notificacionId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_NOTIFICACION)
        statement.setInt(1, notificacionId)
        statement.executeUpdate()
    }
}
