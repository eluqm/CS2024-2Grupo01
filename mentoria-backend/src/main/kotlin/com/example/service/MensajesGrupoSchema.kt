package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement
import java.time.LocalDateTime

@Serializable
data class MensajeGrupo(
    val mensajeId: Int? = null,
    val grupoId: Int,
    val remitenteId: Int,
    val textoMensaje: String,
    val enviadoEn: String? = null
)

@Serializable
data class Chat(
    val emisor: String,
    val fecha: String,
    val mensaje: String,
    val hora: String
)

@Serializable
class MensajesGrupoService(private val connection: Connection) {
    companion object {
        private const val INSERT_MENSAJE = "INSERT INTO mensajes_grupo (grupo_id, remitente_id, texto_mensaje) VALUES (?, ?, ?)"
        private const val SELECT_MENSAJE_BY_ID = "SELECT * FROM mensajes_grupo WHERE mensaje_id = ?"
        private const val UPDATE_MENSAJE = "UPDATE mensajes_grupo SET grupo_id = ?, remitente_id = ?, texto_mensaje = ? WHERE mensaje_id = ?"
        private const val DELETE_MENSAJE = "DELETE FROM mensajes_grupo WHERE mensaje_id = ?"
        private const val SELECT_LIST_MENSAJES_BY_GRUPO_ID = "SELECT * FROM mensajes_grupo WHERE grupo_id = ? ORDER BY enviado_en ASC"
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

    suspend fun readChatsByUser(userId: Int): List<Chat> = withContext(Dispatchers.IO) {
        val chats = mutableListOf<Chat>()

        // Obtener el grupo_id usando el user_id
        val statementGrupo = connection.prepareStatement("SELECT grupo_id \n" +
                "FROM miembros_grupo \n" +
                "WHERE user_id = ? \n" +
                "UNION\n" +
                "SELECT grupo_id \n" +
                "FROM grupos \n" +
                "WHERE jefe_id = ?\n")

        statementGrupo.setInt(1, userId)
        statementGrupo.setInt(2, userId)
        val resultSetGrupo = statementGrupo.executeQuery()

        if (!resultSetGrupo.next()) {
            throw Exception("User not found in any group")
        }

        val grupoId = resultSetGrupo.getInt("grupo_id")

        // Ahora obtenemos los mensajes del grupo
        val statementMensajes = connection.prepareStatement(SELECT_LIST_MENSAJES_BY_GRUPO_ID)
        statementMensajes.setInt(1, grupoId)
        val resultSetMensajes = statementMensajes.executeQuery()

        while (resultSetMensajes.next()) {
            val remitenteId = resultSetMensajes.getInt("remitente_id")
            val remitenteNombre = getUsuarioNombreById(remitenteId) // Consulta para obtener el nombre del usuario
            val enviadoEn = resultSetMensajes.getTimestamp("enviado_en").toLocalDateTime()
            val fecha = enviadoEn.toLocalDate().toString()
            val hora = enviadoEn.toLocalTime().toString()

            chats.add(
                Chat(
                    emisor = remitenteNombre,
                    fecha = fecha,
                    hora = hora,
                    mensaje = resultSetMensajes.getString("texto_mensaje")
                )
            )
        }

        if (chats.isEmpty()) {
            throw Exception("No chats found for user ID $userId")
        }

        return@withContext chats
    }


    // Consulta auxiliar para obtener el nombre del usuario por ID
    private fun getUsuarioNombreById(usuarioId: Int): String {
        val statementUsuario = connection.prepareStatement("SELECT nombre_usuario FROM usuarios WHERE user_id = ?")
        statementUsuario.setInt(1, usuarioId)
        val resultSetUsuario = statementUsuario.executeQuery()
        return if (resultSetUsuario.next()) {
            resultSetUsuario.getString("nombre_usuario")
        } else {
            throw Exception("Usuario not found for ID $usuarioId")
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
