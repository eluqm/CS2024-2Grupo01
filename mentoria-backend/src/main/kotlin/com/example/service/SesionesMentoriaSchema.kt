package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

/**
 * Clase de datos que representa una sesión de mentoría.
 *
 * @property sesionId ID de la sesión, null si aún no se ha insertado en la base de datos
 * @property grupoId ID del grupo al que pertenece esta sesión
 * @property estado Estado de la sesión
 * @property temaSesion Tema de la sesión
 * @property notas Notas sobre la sesión, puede ser null
 * @property fotografia Fotografía de la sesión como array de bytes
 */
@Serializable
data class SesionMentoria(
    val sesionId: Int? = null,
    val grupoId: Int,
    val estado: String,
    val temaSesion: String,
    val notas: String?,
    val fotografia: ByteArray
)

/**
 * Clase de servicio para gestionar sesiones de mentoría en la base de datos.
 * Proporciona operaciones CRUD y consultas especializadas para sesiones de mentoría.
 *
 * @property connection Conexión activa a la base de datos
 */
class SesionesMentoriaService(private val connection: Connection) {
    companion object {
        // Consultas SQL
        private const val INSERT_SESION = "INSERT INTO sesiones_mentoria (grupo_id, estado, tema_sesion, notas, fotografia) VALUES (?, ?, ?, ?, ?)"
        private const val SELECT_SESION_BY_ID = "SELECT * FROM sesiones_mentoria WHERE sesion_id = ?"
        private const val UPDATE_SESION = "UPDATE sesiones_mentoria SET grupo_id = ?, hora_programada = ?, estado = ?, tema_sesion = ? , notas = ?, fotografia = ? WHERE grupo_id = ?"
        private const val DELETE_SESION = "DELETE FROM sesiones_mentoria WHERE sesion_id = ?"
        private const val SELECT_ALL_SESIONES = """
            SELECT 
                s.sesion_id,
                s.grupo_id,
                s.estado,
                s.tema_sesion,
                s.notas,
                s.fotografia,
                g.nombre as nombre_grupo
            FROM sesiones_mentoria s
            JOIN grupos g ON s.grupo_id = g.grupo_id
            ORDER BY s.sesion_id DESC
        """
    }

    /**
     * Crea una nueva sesión de mentoría en la base de datos.
     *
     * @param sesion Datos de la sesión a crear
     * @return ID de la sesión recién creada
     * @throws Exception si no se puede recuperar el ID generado
     */
    suspend fun create(sesion: SesionMentoria): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_SESION, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, sesion.grupoId)
        statement.setString(2, sesion.estado)
        statement.setString(3, sesion.temaSesion)
        statement.setString(4, sesion.notas)
        statement.setBytes(5, sesion.fotografia)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("No se puede recuperar el ID de la sesión recién insertada")
        }
    }

    /**
     * Recupera una sesión por su ID.
     *
     * @param sesionId ID de la sesión a recuperar
     * @return Objeto SesionMentoria con los datos de la sesión
     * @throws Exception si no se encuentra la sesión
     */
    suspend fun read(sesionId: Int): SesionMentoria = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_SESION_BY_ID)
        statement.setInt(1, sesionId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext SesionMentoria(
                grupoId = resultSet.getInt("grupo_id"),
                estado = resultSet.getString("estado"),
                temaSesion = resultSet.getString("tema_sesion"),
                notas = resultSet.getString("notas"),
                fotografia = resultSet.getBytes("fotografia")
            )
        } else {
            throw Exception("Sesión no encontrada")
        }
    }

    /**
     * Actualiza una sesión existente.
     *
     * @param sesionId ID de la sesión a actualizar
     * @param sesion Datos actualizados de la sesión
     */
    suspend fun update(sesionId: Int, sesion: SesionMentoria) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_SESION)
        statement.setInt(1, sesion.grupoId)
        statement.setString(2, sesion.estado)
        statement.setString(3, sesion.temaSesion)
        statement.setString(4, sesion.notas)
        statement.setBytes(5, sesion.fotografia)
        statement.setInt(6, sesionId)
        statement.executeUpdate()
    }

    /**
     * Elimina una sesión por su ID.
     *
     * @param sesionId ID de la sesión a eliminar
     */
    suspend fun delete(sesionId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_SESION)
        statement.setInt(1, sesionId)
        statement.executeUpdate()
    }

    /**
     * Comprueba si existe una sesión para un grupo en la fecha actual.
     *
     * @param grupoId ID del grupo a comprobar
     * @return true si existe una sesión hoy, false en caso contrario
     */
    suspend fun existeSesionHoy(grupoId: Int): Boolean = withContext(Dispatchers.IO) {
        val query = """
        SELECT EXISTS (
            SELECT 1 FROM sesiones_mentoria 
            WHERE grupo_id = ? 
            AND fecha_hora::DATE = CURRENT_DATE
        )
        """.trimIndent()

        val statement = connection.prepareStatement(query)
        statement.setInt(1, grupoId)

        val resultSet = statement.executeQuery()
        return@withContext if (resultSet.next()) resultSet.getBoolean(1) else false
    }

    /**
     * Obtiene todas las sesiones de mentoría con información del grupo.
     *
     * @return Lista de sesiones de mentoría
     */
    suspend fun readAll(): List<SesionMentoria> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ALL_SESIONES)
        val resultSet = statement.executeQuery()

        val sesiones = mutableListOf<SesionMentoria>()
        while (resultSet.next()) {
            sesiones.add(
                SesionMentoria(
                    sesionId = resultSet.getInt("sesion_id"),
                    grupoId = resultSet.getInt("grupo_id"),
                    estado = resultSet.getString("estado"),
                    temaSesion = resultSet.getString("tema_sesion"),
                    notas = resultSet.getString("notas"),
                    fotografia = resultSet.getBytes("fotografia")
                )
            )
        }
        return@withContext sesiones
    }
}