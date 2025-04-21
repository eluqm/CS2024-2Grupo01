package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement
import java.time.LocalDateTime

/**
 * Clase de datos que representa un registro de asistencia a sesión para creación.
 *
 * @property sesionId ID de la sesión
 * @property mentoriadoId ID del estudiante mentoriado
 * @property asistio Booleano que indica si el estudiante asistió
 */
@Serializable
data class AsistenciaSesion(
    val sesionId: Int,
    val mentoriadoId: Int,
    val asistio: Boolean,
)

/**
 * Clase de datos que representa un registro de asistencia a sesión para lectura.
 * Incluye información de fecha/hora.
 *
 * @property sesionId ID de la sesión
 * @property mentoriadoId ID del estudiante mentoriado
 * @property asistio Booleano que indica si el estudiante asistió
 * @property horaFechaRegistrada Fecha y hora cuando se registró la asistencia
 */
data class AsistenciaSesionLectura(
    val sesionId: Int,
    val mentoriadoId: Int,
    val asistio: Boolean,
    @Contextual
    val horaFechaRegistrada: LocalDateTime?
)

/**
 * Clase de servicio para gestionar registros de asistencia a sesiones en la base de datos.
 * Proporciona operaciones CRUD para asistencia a sesiones.
 *
 * @property connection Conexión activa a la base de datos
 */
class AsistenciasSesionesService(private val connection: Connection) {
    companion object {
        // Consultas SQL y creación de tablas
        private const val CREATE_TABLE_ASISTENCIAS_SESIONES =
            "CREATE TABLE ASISTENCIAS_SESIONES (ASISTENCIA_ID SERIAL PRIMARY KEY, SESION_ID INT, MENTORIADO_ID INT, ASISTIO BOOLEAN, HORA_FECHA_REGISTRADA TIMESTAMP);"
        private const val INSERT_ASISTENCIA = "INSERT INTO asistencias_sesiones (sesion_id, mentoriado_id, asistio) VALUES (?, ?, ?)"
        private const val SELECT_ASISTENCIA_BY_ID = "SELECT * FROM asistencias_sesiones WHERE asistencia_id = ?"
        private const val UPDATE_ASISTENCIA = "UPDATE asistencias_sesiones SET sesion_id = ?, mentoriado_id = ?, asistio = ? WHERE asistencia_id = ?"
        private const val DELETE_ASISTENCIA = "DELETE FROM asistencias_sesiones WHERE asistencia_id = ?"
    }

    /**
     * Inicializa el servicio. El SQL de creación de tabla está comentado
     * y debe ser descomentado si es necesario.
     */
    init {
        val statement = connection.createStatement()
        // Descomentar la siguiente línea para crear la tabla si es necesario
        // statement.executeUpdate(CREATE_TABLE_ASISTENCIAS_SESIONES)
    }

    /**
     * Crea un nuevo registro de asistencia en la base de datos.
     *
     * @param asistencia Datos de asistencia a crear
     * @return ID del registro de asistencia recién creado
     * @throws Exception si no se puede recuperar el ID generado
     */
    suspend fun create(asistencia: AsistenciaSesion): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_ASISTENCIA, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, asistencia.sesionId)
        statement.setInt(2, asistencia.mentoriadoId)
        statement.setBoolean(3, asistencia.asistio)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("No se puede recuperar el ID de la asistencia recién insertada")
        }
    }

    /**
     * Recupera un registro de asistencia por su ID.
     *
     * @param asistenciaId ID del registro de asistencia a recuperar
     * @return Objeto AsistenciaSesionLectura con los datos de asistencia
     * @throws Exception si no se encuentra el registro de asistencia
     */
    suspend fun read(asistenciaId: Int): AsistenciaSesionLectura = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ASISTENCIA_BY_ID)
        statement.setInt(1, asistenciaId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext AsistenciaSesionLectura(
                sesionId = resultSet.getInt("sesion_id"),
                mentoriadoId = resultSet.getInt("mentoriado_id"),
                asistio = resultSet.getBoolean("asistio"),
                horaFechaRegistrada = resultSet.getTimestamp("hora_fecha_registrada")?.toLocalDateTime()
            )
        } else {
            throw Exception("Asistencia no encontrada")
        }
    }

    /**
     * Actualiza un registro de asistencia existente.
     *
     * @param asistenciaId ID del registro de asistencia a actualizar
     * @param asistencia Datos de asistencia actualizados
     */
    suspend fun update(asistenciaId: Int, asistencia: AsistenciaSesion) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_ASISTENCIA)
        statement.setInt(1, asistencia.sesionId)
        statement.setInt(2, asistencia.mentoriadoId)
        statement.setBoolean(3, asistencia.asistio)
        statement.setInt(4, asistenciaId)
        statement.executeUpdate()
    }

    /**
     * Elimina un registro de asistencia por su ID.
     *
     * @param asistenciaId ID del registro de asistencia a eliminar
     */
    suspend fun delete(asistenciaId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_ASISTENCIA)
        statement.setInt(1, asistenciaId)
        statement.executeUpdate()
    }
}