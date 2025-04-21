package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement
import java.time.LocalDateTime

/**
 * Modelo de datos que representa una solicitud de mentoría.
 *
 * @property coordinadorId Identificador del coordinador que realiza la solicitud.
 * @property mentorId Identificador del mentor al que se dirige la solicitud.
 * @property fechaSolicitud Fecha y hora en que se registró la solicitud. Generada automáticamente por la base de datos.
 * @property estado Estado actual de la solicitud (ej: "pendiente", "aprobada", "rechazada").
 * @property mensaje Texto descriptivo de la solicitud.
 */
@Serializable
data class SolicitudMentoria(
    val coordinadorId: Int,
    val mentorId: Int,
    @Contextual
    val fechaSolicitud: LocalDateTime?,
    val estado: String,
    val mensaje: String
)

/**
 * Servicio para la gestión de solicitudes de mentoría en la base de datos.
 * Proporciona operaciones CRUD para las solicitudes de mentoría.
 *
 * @property connection Conexión a la base de datos.
 */
class SolicitudesMentoriaService(private val connection: Connection) {
    companion object {
        private const val INSERT_SOLICITUD = "INSERT INTO solicitudes_mentoria (coordinador_id, mentor_id, estado, mensaje) VALUES (?, ?, ?, ?)"
        private const val SELECT_SOLICITUD_BY_ID = "SELECT * FROM solicitudes_mentoria WHERE solicitud_id = ?"
        private const val UPDATE_SOLICITUD = "UPDATE solicitudes_mentoria SET coordinador_id = ?, mentor_id = ?, estado = ?, mensaje = ? WHERE solicitud_id = ?"
        private const val DELETE_SOLICITUD = "DELETE FROM solicitudes_mentoria WHERE solicitud_id = ?"
    }

    /**
     * Crea una nueva solicitud de mentoría en la base de datos.
     *
     * @param solicitud Objeto SolicitudMentoria con los datos a insertar.
     * @return Identificador generado para la nueva solicitud.
     * @throws Exception Si no se puede recuperar el ID generado.
     */
    suspend fun create(solicitud: SolicitudMentoria): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_SOLICITUD, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, solicitud.coordinadorId)
        statement.setInt(2, solicitud.mentorId)
        statement.setString(3, solicitud.estado)
        statement.setString(4, solicitud.mensaje)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted solicitud de mentoria")
        }
    }

    /**
     * Recupera una solicitud de mentoría específica por su ID.
     *
     * @param solicitudId Identificador de la solicitud a buscar.
     * @return Objeto SolicitudMentoria con los datos recuperados.
     * @throws Exception Si no se encuentra la solicitud con el ID especificado.
     */
    suspend fun read(solicitudId: Int): SolicitudMentoria = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_SOLICITUD_BY_ID)
        statement.setInt(1, solicitudId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext SolicitudMentoria(
                coordinadorId = resultSet.getInt("coordinador_id"),
                mentorId = resultSet.getInt("mentor_id"),
                estado = resultSet.getString("estado"),
                mensaje = resultSet.getString("mensaje"),
                fechaSolicitud = resultSet.getTimestamp("fecha_solicitud").toLocalDateTime()
            )
        } else {
            throw Exception("Solicitud not found")
        }
    }

    /**
     * Actualiza los datos de una solicitud de mentoría existente.
     *
     * @param solicitudId Identificador de la solicitud a actualizar.
     * @param solicitud Objeto SolicitudMentoria con los nuevos datos.
     */
    suspend fun update(solicitudId: Int, solicitud: SolicitudMentoria) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_SOLICITUD)
        statement.setInt(1, solicitud.coordinadorId)
        statement.setInt(2, solicitud.mentorId)
        statement.setString(3, solicitud.estado)
        statement.setString(4, solicitud.mensaje)
        statement.setInt(5, solicitudId)
        statement.executeUpdate()
    }

    /**
     * Elimina una solicitud de mentoría de la base de datos.
     *
     * @param solicitudId Identificador de la solicitud a eliminar.
     */
    suspend fun delete(solicitudId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_SOLICITUD)
        statement.setInt(1, solicitudId)
        statement.executeUpdate()
    }
}