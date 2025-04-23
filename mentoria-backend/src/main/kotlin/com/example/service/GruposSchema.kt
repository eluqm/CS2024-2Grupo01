package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

/**
 * Clase de datos que representa un grupo de mentoría.
 *
 * @property grupoId ID del grupo, null si aún no se ha insertado en la base de datos
 * @property jefeId ID del líder/mentor del grupo
 * @property nombre Nombre del grupo
 * @property horarioId ID del horario del grupo, null si no está asignado
 * @property descripcion Descripción del grupo, puede ser null
 * @property creadoEn Fecha cuando se creó el grupo, null si no está establecida
 */
@Serializable
data class Grupo(
    val grupoId: Int? = null,
    val jefeId: Int,
    val nombre: String,
    val horarioId: Int? = null,
    val descripcion: String?,
    val creadoEn: String? = null
)

/**
 * Clase de datos extendida para información del grupo incluyendo el nombre del mentor.
 *
 * @property grupoId ID del grupo, null si aún no se ha insertado en la base de datos
 * @property jefeId ID del líder/mentor del grupo
 * @property jefeName Nombre completo del líder/mentor del grupo
 * @property nombre Nombre del grupo
 * @property descripcion Descripción del grupo, puede ser null
 * @property creadoEn Fecha cuando se creó el grupo, null si no está establecida
 */
@Serializable
data class GrupoMentoriaPlus(
    val grupoId: Int? = null,
    val jefeId: Int,
    val jefeName: String,
    val nombre: String,
    val descripcion: String?,
    val creadoEn: String? = null
)

/**
 * Clase de datos para información de sesión de mentoría.
 *
 * @property temaSesion Tema de la sesión
 * @property lugar Lugar donde se lleva a cabo la sesión
 * @property fechaRegistrada Fecha cuando se registró la sesión
 * @property participantes Representación en cadena de texto de la proporción de participantes
 * @property foto Fotografía de la sesión como array de bytes
 */
@Serializable
data class SesionInfo(
    val temaSesion: String,
    val lugar: String,
    val fechaRegistrada: String,
    val participantes: String,
    val foto: ByteArray
)

/**
 * Clase de datos para información de listado de usuarios.
 *
 * @property id ID del usuario
 * @property nombreCompletoUsuario Nombre completo del usuario
 * @property email Correo electrónico del usuario
 * @property celularUsuario Número de teléfono del usuario
 * @property dniUsuario DNI (número de identificación) del usuario
 */
@Serializable
data class UsuarioLista(
    val id: Int,
    val nombreCompletoUsuario: String,
    val email: String,
    val celularUsuario: String,
    val dniUsuario: String
)

/**
 * Clase de servicio para gestionar grupos de mentoría en la base de datos.
 * Proporciona operaciones CRUD y consultas especializadas para grupos de mentoría.
 *
 * @property connection Conexión activa a la base de datos
 */
class GruposService(private val connection: Connection) {
    companion object {
        // Consultas SQL
        private const val INSERT_GRUPO = "INSERT INTO grupos (jefe_id, nombre, descripcion) VALUES (?, ?, ?)"
        private const val SELECT_GRUPO_BY_ID = "SELECT * FROM grupos WHERE grupo_id = ?"
        private const val UPDATE_GRUPO = "UPDATE grupos SET jefe_id = ?, nombre = ?, horario_id = ?, descripcion = ? WHERE grupo_id = ?"
        private const val DELETE_GRUPO = "DELETE FROM grupos WHERE grupo_id = ?"
        private const val SELECT_GRUPOS_BY_MENTOR = "SELECT * FROM grupos WHERE jefe_id = ?"
        private const val SELECT_MENTORIADOS_BY_GRUPO = """
            SELECT u.user_id, concat(u.nombre_usuario, ' ',u.apellido_usuario) as nombre_completo, u.email, u.celular_usuario, u.dni_usuario
            FROM usuarios u
            JOIN miembros_grupo mg ON u.user_id = mg.user_id
            WHERE mg.grupo_id = ? AND u.tipo_usuario = 'mentoriado'
            ORDER BY nombre_completo
        """
        private const val SELECT_SESIONES_POR_JEFE = """
            SELECT 
                s.tema_sesion,
                h.lugar,
                DATE(s.fecha_hora) AS fecha_registrada,
                CONCAT(COUNT(a.mentoriado_id), '/', (SELECT COUNT(*) FROM miembros_grupo mg WHERE mg.grupo_id = g.grupo_id)) AS participantes,
                s.fotografia AS foto
            FROM 
                sesiones_mentoria s
            JOIN 
                grupos g ON s.grupo_id = g.grupo_id
            JOIN 
                horarios h ON g.horario_id = h.horario_id
            JOIN 
                asistencias_sesiones a ON s.sesion_id = a.sesion_id
            WHERE 
                g.jefe_id = ? 
                AND a.asistio = true
            GROUP BY 
                s.tema_sesion, h.lugar, fecha_registrada, foto, g.grupo_id
            ORDER BY fecha_registrada DESC
        """
        private const val SELECT_ALL_GRUPOS = """
            SELECT 
                g.grupo_id,
                g.jefe_id,
                CONCAT(u.nombre_usuario, ' ', u.apellido_usuario) AS jefe_name,
                g.nombre,
                g.descripcion,
                g.creado_en
            FROM grupos g
            JOIN usuarios u ON g.jefe_id = u.user_id
            ORDER BY g.creado_en DESC
        """
    }

    /**
     * Crea un nuevo grupo de mentoría en la base de datos.
     *
     * @param grupo Datos del grupo a crear
     * @return ID del grupo recién creado
     * @throws Exception si no se puede recuperar el ID generado
     */
    suspend fun create(grupo: Grupo): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_GRUPO, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, grupo.jefeId)
        statement.setString(2, grupo.nombre)
        statement.setString(3, grupo.descripcion)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("No se pudo recuperar el ID del grupo recién insertado")
        }
    }

    /**
     * Recupera un grupo por su ID.
     *
     * @param grupoId ID del grupo a recuperar
     * @return Objeto Grupo con los datos del grupo
     * @throws Exception si el grupo no se encuentra
     */
    suspend fun read(grupoId: Int): Grupo = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_GRUPO_BY_ID)
        statement.setInt(1, grupoId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Grupo(
                jefeId = resultSet.getInt("jefe_id"),
                nombre = resultSet.getString("nombre_grupo"),
                horarioId = resultSet.getInt("horario_id"),
                descripcion = resultSet.getString("descripcion"),
                creadoEn = resultSet.getString("fecha_creacion")
            )
        } else {
            throw Exception("Grupo no encontrado")
        }
    }

    /**
     * Obtiene usuarios mentoriados por ID del mentor.
     *
     * @param jefeId ID del mentor
     * @return Lista de usuarios mentoriados
     */
    suspend fun getMiembrosPorJefe(jefeId: Int): List<UsuarioLista> = withContext(Dispatchers.IO) {
        val query = """
        SELECT ment.mentoriado_id as ment_id, CONCAT(u.nombre_usuario, ' ', u.apellido_usuario) AS nombre_completo, 
               u.email, u.celular_usuario, u.dni_usuario
        FROM usuarios u
        JOIN mentoriados ment ON u.user_id = ment.user_id 
        JOIN miembros_grupo mg ON u.user_id = mg.user_id
        JOIN grupos g ON mg.grupo_id = g.grupo_id
        WHERE g.jefe_id = ?
        """.trimIndent()

        val statement = connection.prepareStatement(query)
        statement.setInt(1, jefeId)
        val resultSet = statement.executeQuery()

        val usuarios = mutableListOf<UsuarioLista>()
        while (resultSet.next()) {
            usuarios.add(
                UsuarioLista(
                    id = resultSet.getInt("ment_id"),
                    nombreCompletoUsuario = resultSet.getString("nombre_completo"),
                    email = resultSet.getString("email"),
                    celularUsuario = resultSet.getString("celular_usuario"),
                    dniUsuario = resultSet.getString("dni_usuario")
                )
            )
        }

        return@withContext usuarios
    }

    /**
     * Actualiza un grupo existente.
     *
     * @param grupoId ID del grupo a actualizar
     * @param grupo Datos actualizados del grupo
     */
    suspend fun update(grupoId: Int, grupo: Grupo) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_GRUPO)
        statement.setInt(1, grupo.jefeId)
        statement.setString(2, grupo.nombre)
        statement.setString(3, grupo.descripcion)
        statement.setInt(4, grupoId)
        statement.executeUpdate()
    }

    /**
     * Elimina un grupo por su ID.
     *
     * @param grupoId ID del grupo a eliminar
     */
    suspend fun delete(grupoId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_GRUPO)
        statement.setInt(1, grupoId)
        statement.executeUpdate()
    }

    /**
     * Obtiene usuarios mentoriados asociados con un mentor específico.
     *
     * @param mentorId ID del mentor
     * @return Lista de usuarios mentoriados
     * @throws IllegalArgumentException si no se encuentra un grupo para el mentor
     */
    suspend fun getUsuariosMentoriadosPorMentor(mentorId: Int): List<UsuarioLista> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_GRUPOS_BY_MENTOR)
        statement.setInt(1, mentorId)
        val resultSet = statement.executeQuery()

        if (!resultSet.next()) {
            throw IllegalArgumentException("No se encontró un grupo para el mentor con ID $mentorId")
        }
        val grupoId = resultSet.getInt("grupo_id")

        val statement2 = connection.prepareStatement(SELECT_MENTORIADOS_BY_GRUPO)
        statement2.setInt(1, grupoId)
        val resultSet2 = statement2.executeQuery()

        val usuarios = mutableListOf<UsuarioLista>()
        while (resultSet2.next()) {
            usuarios.add(
                UsuarioLista(
                    id = resultSet2.getInt("user_id"),
                    nombreCompletoUsuario = resultSet2.getString("nombre_completo"),
                    email = resultSet2.getString("email"),
                    celularUsuario = resultSet2.getString("celular_usuario"),
                    dniUsuario = resultSet2.getString("dni_usuario")
                )
            )
        }
        return@withContext usuarios
    }

    /**
     * Obtiene sesiones de mentoría por ID del mentor.
     *
     * @param jefeId ID del mentor
     * @return Lista de información de sesiones
     */
    suspend fun getSesionesPorJefe(jefeId: Int): List<SesionInfo> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_SESIONES_POR_JEFE)
        statement.setInt(1, jefeId)
        val resultSet = statement.executeQuery()

        val sesiones = mutableListOf<SesionInfo>()
        while (resultSet.next()) {
            sesiones.add(
                SesionInfo(
                    temaSesion = resultSet.getString("tema_sesion"),
                    lugar = resultSet.getString("lugar"),
                    fechaRegistrada = resultSet.getString("fecha_registrada"),
                    participantes = resultSet.getString("participantes"),
                    foto = resultSet.getBytes("foto")
                )
            )
        }

        return@withContext sesiones
    }

    /**
     * Obtiene todos los grupos por ID de escuela con información extendida.
     *
     * @param escuelaId ID de la escuela
     * @return Lista de grupos con información extendida
     * @throws Exception si no se encuentran grupos para la escuela
     */
    suspend fun readAllByEscuelaId(escuelaId: Int): List<GrupoMentoriaPlus> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(
            """
            SELECT 
                g.grupo_id,
                g.jefe_id,
                CONCAT(u.nombre_usuario, ' ', u.apellido_usuario) AS jefe_name,
                g.nombre,
                g.descripcion,
                g.creado_en
            FROM grupos g
            JOIN usuarios u ON g.jefe_id = u.user_id
            WHERE u.escuela_id = ? order by creado_en desc
            """
        )
        statement.setInt(1, escuelaId)
        val resultSet = statement.executeQuery()

        val grupos = mutableListOf<GrupoMentoriaPlus>()

        while (resultSet.next()) {
            grupos.add(
                GrupoMentoriaPlus(
                    grupoId = resultSet.getInt("grupo_id"),
                    jefeId = resultSet.getInt("jefe_id"),
                    jefeName = resultSet.getString("jefe_name"),
                    nombre = resultSet.getString("nombre"),
                    descripcion = resultSet.getString("descripcion"),
                    creadoEn = resultSet.getString("creado_en")
                )
            )
        }

        if (grupos.isEmpty()) {
            throw Exception("No se encontraron grupos para el escuela_id proporcionado.")
        }

        return@withContext grupos
    }

    /**
     * Encuentra el ID del grupo asociado con un usuario (ya sea como miembro o líder).
     *
     * @param userId ID del usuario
     * @return ID del grupo
     * @throws Exception si el usuario no se encuentra en ningún grupo
     */
    suspend fun hallarGrupoID(userId: Int): Int {
        val query = """
            SELECT grupo_id 
            FROM miembros_grupo 
            WHERE user_id = ? 
            UNION 
            SELECT grupo_id 
            FROM grupos 
            WHERE jefe_id = ?
        """.trimIndent()

        connection.prepareStatement(query).use { statement ->
            statement.setInt(1, userId)
            statement.setInt(2, userId)

            val resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                throw Exception("Usuario no encontrado en ningún grupo")
            }

            return resultSet.getInt("grupo_id")
        }
    }

    /**
     * Obtiene todos los grupos con información extendida.
     *
     * @return Lista de grupos con información extendida
     */
    suspend fun getAllGrupos(): List<GrupoMentoriaPlus> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ALL_GRUPOS)
        val resultSet = statement.executeQuery()

        val grupos = mutableListOf<GrupoMentoriaPlus>()

        while (resultSet.next()) {
            grupos.add(
                GrupoMentoriaPlus(
                    grupoId = resultSet.getInt("grupo_id"),
                    jefeId = resultSet.getInt("jefe_id"),
                    jefeName = resultSet.getString("jefe_name"),
                    nombre = resultSet.getString("nombre"),
                    descripcion = resultSet.getString("descripcion"),
                    creadoEn = resultSet.getString("creado_en")
                )
            )
        }

        return@withContext grupos
    }
}