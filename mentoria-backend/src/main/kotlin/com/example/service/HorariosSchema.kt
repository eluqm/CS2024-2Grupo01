package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement
import java.sql.Time

/**
 * Representa un horario académico con sus atributos básicos.
 *
 * @property horarioId Identificador único del horario (opcional)
 * @property lugar Ubicación donde se realizará la actividad académica
 * @property dia Día de la semana en que se realizará la actividad
 * @property horaInicio Hora de inicio de la actividad en formato HH:MM:SS
 * @property horaFin Hora de finalización de la actividad en formato HH:MM:SS
 * @property estado Indica si el horario está activo (true) o inactivo (false)
 */
@Serializable
data class Horario(
    val horarioId: Int? = null,
    val lugar: String?,
    val dia: String,
    val horaInicio: String,
    val horaFin: String,
    val estado: Boolean
)

/**
 * Representa un horario con información detallada incluyendo datos del grupo asociado.
 *
 * @property horarioId Identificador único del horario (opcional)
 * @property lugar Ubicación donde se realizará la actividad académica
 * @property dia Día de la semana en que se realizará la actividad
 * @property horaInicio Hora de inicio de la actividad
 * @property horaFin Hora de finalización de la actividad
 * @property estado Indica si el horario está activo (true) o inactivo (false)
 * @property nombreGrupo Nombre del grupo académico asociado al horario
 * @property nombreCompletoJefe Nombre completo del jefe/responsable del grupo
 * @property nombreEscuela Nombre de la escuela a la que pertenece el grupo
 */
@Serializable
data class HorarioDetalles(
    val horarioId: Int? = null,
    val lugar: String?,
    val dia: String?,
    val horaInicio: String?,
    val horaFin: String?,
    val estado: Boolean?,
    val nombreGrupo: String?,
    val nombreCompletoJefe: String?,
    val nombreEscuela: String?
)

/**
 * Modelo para actualización parcial de un horario.
 *
 * @property horarioId Identificador único del horario a actualizar
 * @property lugar Nueva ubicación donde se realizará la actividad
 * @property estado Nuevo estado del horario (activo/inactivo)
 */
@Serializable
data class HorarioUpdate(
    val horarioId: Int,
    val lugar: String,
    val estado: Boolean = true
)

/**
 * Servicio para la gestión de horarios académicos en la base de datos.
 * Proporciona operaciones CRUD y otras funcionalidades específicas para horarios.
 *
 * @property connection Conexión a la base de datos
 */
class HorariosService(private val connection: Connection) {
    companion object {
        /** SQL para actualizar el horario de un grupo */
        private const val UPDATE_GRUPO_HORARIO = "UPDATE grupos SET horario_id = ? WHERE jefe_id = ? AND horario_id IS NULL"

        /** SQL para insertar un nuevo horario */
        private const val INSERT_HORARIO = "INSERT INTO horarios (lugar, dia, hora_inicio, hora_fin, estado) VALUES (?, ?, ?, ?, ?)"

        /** SQL para seleccionar un horario por ID con detalles de grupo, jefe y escuela */
        private const val SELECT_HORARIO_BY_ID = """
    SELECT 
        h.horario_id,
        h.lugar,
        h.dia,
        h.hora_inicio,
        h.hora_fin,
        h.estado,
        g.nombre AS nombre_grupo,
        u.nombre_usuario || ' ' || u.apellido_usuario AS nombre_completo_jefe,
        e.nombre AS nombre_escuela
    FROM horarios h
    LEFT JOIN grupos g ON h.horario_id = g.horario_id
    LEFT JOIN usuarios u ON g.jefe_id = u.user_id
    LEFT JOIN escuelas e ON u.escuela_id = e.escuela_id
    WHERE h.horario_id = ?
"""

        /** SQL para seleccionar todos los horarios con detalles */
        private const val SELECT_HORARIOS_DETALLES = """
    SELECT 
        h.horario_id,
        h.lugar,
        h.dia,
        h.hora_inicio,
        h.hora_fin,
        h.estado,
        g.nombre AS nombre_grupo,
        u.nombre_usuario || ' ' || u.apellido_usuario AS nombre_completo_jefe,
        e.acronimo AS acronimo
    FROM horarios h
    LEFT JOIN grupos g ON h.horario_id = g.horario_id
    LEFT JOIN usuarios u ON g.jefe_id = u.user_id
    LEFT JOIN escuelas e ON u.escuela_id = e.escuela_id
"""

        /** SQL para actualización parcial de un horario */
        private const val UPDATE_PARTIAL_HORARIO = "UPDATE horarios SET lugar = ?, estado = ? WHERE horario_id = ?"

        /** SQL para actualización completa de un horario */
        private const val UPDATE_HORARIO = "UPDATE horarios SET lugar = ?, dia = ?, hora_inicio = ?, hora_fin = ?, estado = ? WHERE horario_id = ?"

        /** SQL para eliminar un horario */
        private const val DELETE_HORARIO = "DELETE FROM horarios WHERE horario_id = ?"
    }

    /**
     * Crea un nuevo horario en la base de datos.
     *
     * @param horario Datos del horario a crear
     * @return ID del horario creado
     * @throws Exception Si no se puede recuperar el ID del horario insertado
     */
    suspend fun create(horario: Horario): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_HORARIO, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, horario.lugar)
        statement.setString(2, horario.dia)
        statement.setTime(3, Time.valueOf(horario.horaInicio))
        statement.setTime(4, Time.valueOf(horario.horaFin))
        statement.setBoolean(5, horario.estado)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted horario")
        }
    }

    /**
     * Crea un nuevo horario y lo asocia a un grupo académico mediante su jefe.
     *
     * @param horario Datos del horario a crear
     * @param jefeId ID del jefe del grupo al que se asociará el horario
     * @return ID del horario creado
     * @throws Exception Si no se puede recuperar el ID del horario o no se encuentra el grupo
     */
    suspend fun create2(horario: Horario, jefeId: Int): Int = withContext(Dispatchers.IO) {
        // Insert the new horario
        val statement = connection.prepareStatement(INSERT_HORARIO, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, horario.lugar)
        statement.setString(2, horario.dia)
        statement.setString(3, horario.horaInicio)
        statement.setString(4, horario.horaFin)
        statement.setBoolean(5, horario.estado)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            val horarioId = generatedKeys.getInt(1)

            // Update the group to associate the new horario
            updateGrupoHorario(horarioId, jefeId)

            return@withContext horarioId
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted horario")
        }
    }

    /**
     * Actualiza el ID de horario para un grupo identificado por su jefe.
     *
     * @param horarioId ID del horario a asociar
     * @param jefeId ID del jefe del grupo
     * @throws Exception Si no se encuentra ningún grupo con el jefe especificado o ya tiene horario
     */
    private suspend fun updateGrupoHorario(horarioId: Int, jefeId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_GRUPO_HORARIO)
        statement.setInt(1, horarioId) // Set the horario_id
        statement.setInt(2, jefeId)   // Find the group by jefe_id
        val rowsUpdated = statement.executeUpdate()
        if (rowsUpdated == 0) {
            throw Exception("No group found with jefe_id = $jefeId and null horario_id")
        }
    }

    /**
     * Obtiene un horario por su ID con información detallada.
     *
     * @param horarioId ID del horario a consultar
     * @return Objeto HorarioDetalles con la información del horario y datos relacionados
     * @throws Exception Si no se encuentra el horario
     */
    suspend fun read(horarioId: Int): HorarioDetalles = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_HORARIO_BY_ID)
        statement.setInt(1, horarioId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext HorarioDetalles(
                horarioId = resultSet.getInt("horario_id"),
                lugar = resultSet.getString("lugar"),
                dia = resultSet.getString("dia"),
                horaInicio = resultSet.getTime("hora_inicio").toLocalTime().toString(),
                horaFin = resultSet.getTime("hora_fin").toLocalTime().toString(),
                estado = resultSet.getBoolean("estado"),
                nombreGrupo = resultSet.getString("nombre_grupo"),
                nombreCompletoJefe = resultSet.getString("nombre_completo_jefe"),
                nombreEscuela = resultSet.getString("nombre_escuela")
            )
        } else {
            throw Exception("Horario not found")
        }
    }

    /**
     * Obtiene el horario asociado a un grupo específico.
     *
     * @param grupoId ID del grupo cuyo horario se quiere consultar
     * @return Objeto Horario con la información del horario
     * @throws Exception Si no se encuentra horario para el grupo especificado
     */
    suspend fun readHorarioByGrupo(grupoId: Int): Horario = withContext(Dispatchers.IO) {
        val query = """
        select * from horarios
        inner join grupos g on horarios.horario_id = g.horario_id
        where grupo_id = ?
    """
        val statement = connection.prepareStatement(query)
        statement.setInt(1, grupoId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Horario(
                horarioId = resultSet.getInt("horario_id"),
                lugar = resultSet.getString("lugar"),
                dia = resultSet.getString("dia"),
                horaInicio = resultSet.getString("hora_inicio"),
                horaFin = resultSet.getString("hora_fin"),
                estado = resultSet.getBoolean("estado")
            )
        } else {
            throw Exception("Horario not found for grupoId: $grupoId")
        }
    }

    /**
     * Obtiene todos los horarios con información detallada.
     *
     * @return Lista de objetos HorarioDetalles con todos los horarios y su información relacionada
     */
    suspend fun readAll(): List<HorarioDetalles> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_HORARIOS_DETALLES)
        val resultSet = statement.executeQuery()

        val horarios = mutableListOf<HorarioDetalles>()
        while (resultSet.next()) {
            horarios.add(
                HorarioDetalles(
                    horarioId = resultSet.getInt("horario_id"),
                    lugar = resultSet.getString("lugar"),
                    dia = resultSet.getString("dia"),
                    horaInicio = resultSet.getString("hora_inicio"),
                    horaFin = resultSet.getString("hora_fin"),
                    estado = resultSet.getBoolean("estado"),
                    nombreGrupo = resultSet.getString("nombre_grupo"),
                    nombreCompletoJefe = resultSet.getString("nombre_completo_jefe"),
                    nombreEscuela = resultSet.getString("acronimo")
                )
            )
        }
        return@withContext horarios
    }

    /**
     * Actualiza completamente un horario existente.
     *
     * @param horarioId ID del horario a actualizar
     * @param horario Nuevos datos del horario
     */
    suspend fun update(horarioId: Int, horario: Horario) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_HORARIO)
        statement.setString(1, horario.lugar)
        statement.setString(2, horario.dia)
        statement.setTime(3, Time.valueOf(horario.horaInicio))
        statement.setTime(4, Time.valueOf(horario.horaFin))
        statement.setBoolean(5, horario.estado)
        statement.setInt(6, horarioId)
        statement.executeUpdate()
    }

    /**
     * Actualiza parcialmente un horario (solo lugar y estado).
     *
     * @param horarioUpdate Objeto con los datos parciales a actualizar
     */
    suspend fun updatePartial(horarioUpdate: HorarioUpdate) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_PARTIAL_HORARIO)
        statement.setString(1, horarioUpdate.lugar)
        statement.setBoolean(2, horarioUpdate.estado)
        statement.setInt(3, horarioUpdate.horarioId)

        statement.executeUpdate()
    }

    /**
     * Elimina un horario de la base de datos.
     *
     * @param horarioId ID del horario a eliminar
     */
    suspend fun delete(horarioId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_HORARIO)
        statement.setInt(1, horarioId)
        statement.executeUpdate()
    }
}