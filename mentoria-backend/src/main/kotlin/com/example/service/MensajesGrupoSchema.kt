package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement
import java.time.LocalDateTime

/**
 * Modelo de datos que representa un mensaje dentro de un grupo.
 *
 * @property mensajeId Identificador único del mensaje. Puede ser nulo cuando se crea un nuevo mensaje.
 * @property grupoId Identificador del grupo al que pertenece el mensaje.
 * @property remitenteId Identificador del usuario que envía el mensaje.
 * @property textoMensaje Contenido textual del mensaje.
 * @property enviadoEn Fecha y hora en que se envió el mensaje. Generada automáticamente por la base de datos.
 */
@Serializable
data class MensajeGrupo(
    val mensajeId: Int? = null,
    val grupoId: Int,
    val remitenteId: Int,
    val textoMensaje: String,
    val enviadoEn: String? = null
)

/**
 * Modelo de datos que representa un mensaje en formato de chat.
 *
 * @property emisor Nombre del usuario que envía el mensaje.
 * @property fecha Fecha en que se envió el mensaje en formato de texto.
 * @property mensaje Contenido textual del mensaje.
 * @property hora Hora en que se envió el mensaje en formato de texto.
 * @property rol Rol del usuario que envía el mensaje en el sistema.
 */
@Serializable
data class Chat(
    val emisor: String,
    val fecha: String,
    val mensaje: String,
    val hora: String,
    val rol: String
)

/**
 * Servicio para la gestión de mensajes de grupo en la base de datos.
 * Proporciona operaciones CRUD para los mensajes y funcionalidades para recuperarlos en formato de chat.
 *
 * @property connection Conexión a la base de datos.
 */
@Serializable
class MensajesGrupoService(private val connection: Connection) {
    companion object {
        private const val INSERT_MENSAJE = "INSERT INTO mensajes_grupo (grupo_id, remitente_id, texto_mensaje) VALUES (?, ?, ?)"
        private const val SELECT_MENSAJE_BY_ID = "SELECT * FROM mensajes_grupo WHERE mensaje_id = ?"
        private const val UPDATE_MENSAJE = "UPDATE mensajes_grupo SET grupo_id = ?, remitente_id = ?, texto_mensaje = ? WHERE mensaje_id = ?"
        private const val DELETE_MENSAJE = "DELETE FROM mensajes_grupo WHERE mensaje_id = ?"
        private const val SELECT_LIST_MENSAJES_BY_GRUPO_ID = "SELECT * FROM mensajes_grupo WHERE grupo_id = ? ORDER BY enviado_en ASC"
    }

    /**
     * Crea un nuevo mensaje en la base de datos.
     *
     * @param mensaje Objeto MensajeGrupo con los datos a insertar.
     * @return Identificador generado para el nuevo mensaje.
     * @throws Exception Si no se puede recuperar el ID generado.
     */
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

    /**
     * Recupera un mensaje específico por su ID.
     *
     * @param mensajeId Identificador del mensaje a buscar.
     * @return Objeto MensajeGrupo con los datos recuperados.
     * @throws Exception Si no se encuentra el mensaje con el ID especificado.
     */
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

    /**
     * Recupera todos los mensajes asociados a los grupos en los que participa un usuario,
     * formateados como objetos Chat.
     *
     * @param userId Identificador del usuario cuyos chats se desean recuperar.
     * @return Lista de objetos Chat con los mensajes formateados.
     * @throws Exception Si el usuario no pertenece a ningún grupo o no tiene mensajes.
     */
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
            val (remitenteNombre, remitenteRol) = getUsuarioAndRoleNombreById(remitenteId) // Obtiene nombre y rol
            val enviadoEn = resultSetMensajes.getTimestamp("enviado_en").toLocalDateTime()
            val fecha = enviadoEn.toLocalDate().toString()
            val hora = enviadoEn.toLocalTime().toString()

            chats.add(
                Chat(
                    emisor = remitenteNombre,
                    fecha = fecha,
                    hora = hora,
                    mensaje = resultSetMensajes.getString("texto_mensaje"),
                    rol = remitenteRol
                )
            )
        }

        if (chats.isEmpty()) {
            throw Exception("No chats found for user ID $userId")
        }

        return@withContext chats
    }

    /**
     * Método auxiliar para obtener el nombre y rol de un usuario por su ID.
     *
     * @param usuarioId Identificador del usuario.
     * @return Par con el nombre y el rol del usuario.
     * @throws Exception Si no se encuentra el usuario con el ID especificado.
     */
    private fun getUsuarioAndRoleNombreById(usuarioId: Int): Pair<String, String> {
        val statementUsuario = connection.prepareStatement(
            "SELECT nombre_usuario, tipo_usuario FROM usuarios WHERE user_id = ?"
        )
        statementUsuario.setInt(1, usuarioId)
        val resultSetUsuario = statementUsuario.executeQuery()
        return if (resultSetUsuario.next()) {
            Pair(
                resultSetUsuario.getString("nombre_usuario"),
                resultSetUsuario.getString("tipo_usuario")
            )
        } else {
            throw Exception("Usuario not found for ID $usuarioId")
        }
    }

    /**
     * Actualiza los datos de un mensaje existente.
     *
     * @param mensajeId Identificador del mensaje a actualizar.
     * @param mensaje Objeto MensajeGrupo con los nuevos datos.
     */
    suspend fun update(mensajeId: Int, mensaje: MensajeGrupo) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_MENSAJE)
        statement.setInt(1, mensaje.grupoId)
        statement.setInt(2, mensaje.remitenteId)
        statement.setString(3, mensaje.textoMensaje)
        statement.setInt(4, mensajeId)
        statement.executeUpdate()
    }

    /**
     * Elimina un mensaje de la base de datos.
     *
     * @param mensajeId Identificador del mensaje a eliminar.
     */
    suspend fun delete(mensajeId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_MENSAJE)
        statement.setInt(1, mensajeId)
        statement.executeUpdate()
    }
}