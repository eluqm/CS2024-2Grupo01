package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

@Serializable
data class Grupo(
    val grupoId: Int? = null,
    val jefeId: Int,
    val nombre: String,
    val horarioId: Int? = null,
    val descripcion: String?,
    val creadoEn: String? = null
)

@Serializable
data class GrupoMentoriaPlus(
    val grupoId: Int? = null,
    val jefeId: Int,
    val jefeName: String,
    val nombre: String,
    val descripcion: String?,
    val creadoEn: String? = null
)

@Serializable
data class SesionInfo(
    val temaSesion: String,
    val lugar: String,
    val fechaRegistrada: String,
    val participantes: String,
    val foto: ByteArray
)

@Serializable
data class UsuarioLista(
    val id: Int,
    val nombreCompletoUsuario: String,
    val email: String,
    val celularUsuario: String,
    val dniUsuario: String
)

class GruposService(private val connection: Connection) {
    companion object {
        private const val INSERT_GRUPO = "INSERT INTO grupos (jefe_id, nombre, descripcion) VALUES (?, ?, ?)"
        private const val SELECT_GRUPO_BY_ID = "SELECT * FROM grupos WHERE grupo_id = ?"
        private const val UPDATE_GRUPO = "UPDATE grupos SET jefe_id = ?, nombre = ?, horario_id = ?, descripcion = ? WHERE grupo_id = ?"
        private const val DELETE_GRUPO = "DELETE FROM grupos WHERE grupo_id = ?"

        // Nuevas consultas, en teoria solo devuelve uno
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
    }

    // Crear un nuevo grupo
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
            throw Exception("Unable to retrieve the id of the newly inserted grupo")
        }
    }

    // Leer un grupo por su ID
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
            throw Exception("Grupo not found")
        }
    }

    // Obtener usuarios mentoriados por jefeId
    suspend fun getMiembrosPorJefe(jefeId: Int): List<UsuarioLista> = withContext(Dispatchers.IO) {
        // Prepara la consulta
        val query = """
        SELECT u.user_id, CONCAT(u.nombre_usuario, ' ', u.apellido_usuario) AS nombre_completo, 
               u.email, u.celular_usuario, u.dni_usuario
        FROM usuarios u
        JOIN miembros_grupo mg ON u.user_id = mg.user_id
        JOIN grupos g ON mg.grupo_id = g.grupo_id
        WHERE g.jefe_id = ?
    """.trimIndent()

        val statement = connection.prepareStatement(query)
        statement.setInt(1, jefeId)

        // Ejecuta la consulta
        val resultSet = statement.executeQuery()

        // Construir la lista de usuarios
        val usuarios = mutableListOf<UsuarioLista>()
        while (resultSet.next()) {
            usuarios.add(
                UsuarioLista(
                    id = resultSet.getInt("user_id"),
                    nombreCompletoUsuario = resultSet.getString("nombre_completo"),
                    email = resultSet.getString("email"),
                    celularUsuario = resultSet.getString("celular_usuario"),
                    dniUsuario = resultSet.getString("dni_usuario")
                )
            )
        }

        // Devuelve la lista
        return@withContext usuarios
    }


    // Actualizar un grupo
    suspend fun update(grupoId: Int, grupo: Grupo) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_GRUPO)
        statement.setInt(1, grupo.jefeId)
        statement.setString(2, grupo.nombre)
        statement.setString(3, grupo.descripcion)
        statement.setInt(4, grupoId)
        statement.executeUpdate()
    }

    // Eliminar un grupo
    suspend fun delete(grupoId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_GRUPO)
        statement.setInt(1, grupoId)
        statement.executeUpdate()
    }

    // Obtener los usuarios mentoriados de un grupo específico
    suspend fun getUsuariosMentoriadosPorMentor(mentorId: Int): List<UsuarioLista> = withContext(Dispatchers.IO) {

        // Primera consulta: Obtener grupo asociado al mentor
        val statement = connection.prepareStatement(SELECT_GRUPOS_BY_MENTOR)
        statement.setInt(1, mentorId)
        val resultSet = statement.executeQuery()

        // Comprobar si hay resultados y obtener el grupo_id
        if (!resultSet.next()) {
            throw IllegalArgumentException("No se encontró un grupo para el mentor con ID $mentorId")
        }
        val grupoId = resultSet.getInt("grupo_id")

        // Segunda consulta: Obtener usuarios mentoriados del grupo
        val statement2 = connection.prepareStatement(SELECT_MENTORIADOS_BY_GRUPO)
        statement2.setInt(1, grupoId)
        val resultSet2 = statement2.executeQuery()

        // Construcción de la lista de usuarios
        val usuarios = mutableListOf<UsuarioLista>()
        while (resultSet2.next()) {
            usuarios.add(
                UsuarioLista(
                    id = resultSet2.getInt("user_id"), // corregido: debe coincidir con el alias de la consulta
                    nombreCompletoUsuario = resultSet2.getString("nombre_completo"),
                    email = resultSet2.getString("email"),
                    celularUsuario = resultSet2.getString("celular_usuario"),
                    dniUsuario = resultSet2.getString("dni_usuario")
                )
            )
        }
        return@withContext usuarios
    }


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
                throw Exception("User not found in any group")
            }

            return resultSet.getInt("grupo_id")
        }
    }

}
