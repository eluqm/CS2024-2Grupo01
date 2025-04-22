package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

/**
 * Modelo de datos que representa un usuario en el sistema.
 *
 * @property userId Identificador único del usuario (auto-generado)
 * @property dniUsuario Documento de identidad del usuario
 * @property nombreUsuario Nombre del usuario
 * @property apellidoUsuario Apellido del usuario
 * @property celularUsuario Número de celular del usuario
 * @property passwordHash Hash de la contraseña del usuario
 * @property escuelaId Identificador de la escuela a la que pertenece
 * @property semestre Semestre académico actual del usuario (puede ser nulo)
 * @property email Dirección de correo electrónico del usuario
 * @property tipoUsuario Tipo de usuario (estudiante, profesor, etc.)
 * @property creadoEn Fecha y hora de creación del usuario (auto-generada)
 */
@Serializable
data class Usuarios(
    val userId: Int? = null,
    val dniUsuario: String,
    val nombreUsuario: String,
    val apellidoUsuario: String,
    val celularUsuario: String,
    val passwordHash: String,
    val escuelaId: Int,
    val semestre: String?,
    val email: String,
    val tipoUsuario: String,
    val creadoEn: String? = null
)

/**
 * Respuesta que indica si un usuario existe o no.
 *
 * @property exists Booleano que indica si el usuario existe
 */
@Serializable
data class UserExistResponse(val exists: Boolean)

/**
 * Vista simplificada de usuario para listados.
 *
 * @property id Identificador único del usuario
 * @property fullName Nombre completo del usuario (nombre + apellido)
 * @property semester Semestre académico del usuario
 */
@Serializable
data class UserView(
    val id: Int?,
    val fullName: String,
    val semester: String?
)

/**
 * Servicio que gestiona las operaciones CRUD y consultas relacionadas con usuarios.
 * Implementa operaciones asíncronas usando corrutinas de Kotlin.
 *
 * @property connection Conexión a la base de datos
 */
class UsuariosService(private val connection: Connection) {
    companion object {
        private const val INSERT_USUARIO = "INSERT INTO usuarios (dni_usuario, nombre_usuario, apellido_usuario, celular_usuario, password_hash, escuela_id, semestre, email, tipo_usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
        private const val SELECT_USUARIO_BY_ID = "SELECT * FROM usuarios WHERE user_id = ?"
        private const val UPDATE_USUARIO = "UPDATE usuarios SET dni_usuario = ?, nombre_usuario = ?, apellido_usuario = ?, celular_usuario = ?, password_hash = ?, escuela_id = ?, semestre = ?, email = ?, tipo_usuario = ? WHERE user_id = ?"
        private const val DELETE_USUARIO = "DELETE FROM usuarios WHERE user_id = ?"
        private const val SELECT_USUARIOS_BY_TIPO = "SELECT * FROM usuarios WHERE tipo_usuario = ?"
        private const val SELECT_USUARIOS_BY_TIPO_AND_SCHOOL = "SELECT u.user_id,\n" +
                "       CONCAT(u.nombre_usuario, ' ', u.apellido_usuario) AS nombre_completo,\n" +
                "       u.semestre\n" +
                "FROM usuarios u\n" +
                "WHERE u.tipo_usuario = ?\n" +
                "  AND u.escuela_id = ?\n" +
                "  AND NOT EXISTS (\n" +
                "      SELECT 1\n" +
                "      FROM grupos g\n" +
                "      WHERE g.jefe_id = u.user_id\n" +
                "  );"
        private const val SELECT_USUARIOS_BY_TIPO_AND_SCHOOL_AND_SEMESTER = "SELECT u.user_id,\n" +
                "       CONCAT(u.nombre_usuario, ' ', u.apellido_usuario) AS nombre_completo,\n" +
                "       u.semestre\n" +
                "FROM usuarios u\n" +
                "WHERE u.tipo_usuario = ?\n" +
                "  AND u.escuela_id = ?\n" +
                "  AND u.semestre = ?\n" +
                "  AND NOT EXISTS (\n" +
                "      SELECT 1\n" +
                "      FROM miembros_grupo m\n" +
                "      WHERE m.user_id = u.user_id\n" +
                "  );"
        private const val SELECT_ALL_USUARIOS = """
                                                    SELECT user_id, dni_usuario, nombre_usuario, apellido_usuario, celular_usuario, 
                                                           password_hash, escuela_id, semestre, email, tipo_usuario, creado_en 
                                                    FROM usuarios
                                                """

    }

    suspend fun getAll(): List<Usuarios> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ALL_USUARIOS)
        val resultSet = statement.executeQuery()

        val usuarios = mutableListOf<Usuarios>()

        while (resultSet.next()) {
            usuarios.add(
                Usuarios(
                    userId = resultSet.getInt("user_id"),
                    dniUsuario = resultSet.getString("dni_usuario"),
                    nombreUsuario = resultSet.getString("nombre_usuario"),
                    apellidoUsuario = resultSet.getString("apellido_usuario"),
                    celularUsuario = resultSet.getString("celular_usuario"),
                    passwordHash = resultSet.getString("password_hash"),
                    escuelaId = resultSet.getInt("escuela_id"),
                    semestre = resultSet.getString("semestre"),
                    email = resultSet.getString("email"),
                    tipoUsuario = resultSet.getString("tipo_usuario"),
                    creadoEn = resultSet.getString("creado_en")
                )
            )
        }

        return@withContext usuarios
    }


    /**
     * Crea un nuevo usuario en la base de datos.
     *
     * @param usuario Datos del usuario a crear
     * @return ID generado para el nuevo usuario
     * @throws Exception Si no se puede recuperar el ID del usuario insertado
     */
    suspend fun create(usuario: Usuarios): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_USUARIO, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, usuario.dniUsuario)
        statement.setString(2, usuario.nombreUsuario)
        statement.setString(3, usuario.apellidoUsuario)
        statement.setString(4, usuario.celularUsuario)
        statement.setString(5, usuario.passwordHash)
        statement.setInt(6, usuario.escuelaId)
        statement.setString(7, usuario.semestre)
        statement.setString(8, usuario.email)
        statement.setString(9, usuario.tipoUsuario)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted user")
        }
    }

    /**
     * Busca un usuario por su DNI.
     *
     * @param dniUsuario DNI del usuario a buscar
     * @return Objeto Usuarios con los datos del usuario encontrado
     * @throws Exception Si el usuario no existe
     */
    suspend fun readByDni(dniUsuario: String): Usuarios = withContext(Dispatchers.IO) {
        val query = "SELECT * FROM usuarios WHERE dni_usuario = ?"
        val statement = connection.prepareStatement(query)
        statement.setString(1, dniUsuario)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Usuarios(
                userId = resultSet.getInt("user_id"),
                dniUsuario = resultSet.getString("dni_usuario"),
                nombreUsuario = resultSet.getString("nombre_usuario"),
                apellidoUsuario = resultSet.getString("apellido_usuario"),
                celularUsuario = resultSet.getString("celular_usuario"),
                passwordHash = resultSet.getString("password_hash"),
                escuelaId = resultSet.getInt("escuela_id"),
                semestre = resultSet.getString("semestre"),
                email = resultSet.getString("email"),
                tipoUsuario = resultSet.getString("tipo_usuario"),
                creadoEn = resultSet.getString("creado_en")
            )
        } else {
            throw Exception("User not found")
        }
    }

    /**
     * Verifica si existe un usuario con el DNI especificado.
     *
     * @param dniUsuario DNI a verificar
     * @return true si existe un usuario con ese DNI, false en caso contrario
     */
    suspend fun userExistsByDni(dniUsuario: String): Boolean = withContext(Dispatchers.IO) {
        val query = "SELECT COUNT(*) FROM usuarios WHERE dni_usuario = ?"
        val statement = connection.prepareStatement(query)
        statement.setString(1, dniUsuario)
        val resultSet = statement.executeQuery()
        resultSet.next()
        return@withContext resultSet.getInt(1) > 0
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param userId ID del usuario a buscar
     * @return Objeto Usuarios con los datos del usuario encontrado
     * @throws Exception Si el usuario no existe
     */
    suspend fun read(userId: Int): Usuarios = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_USUARIO_BY_ID)
        statement.setInt(1, userId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Usuarios(
                userId = resultSet.getInt("user_id"),
                dniUsuario = resultSet.getString("dni_usuario"),
                nombreUsuario = resultSet.getString("nombre_usuario"),
                apellidoUsuario = resultSet.getString("apellido_usuario"),
                celularUsuario = resultSet.getString("celular_usuario"),
                passwordHash = resultSet.getString("password_hash"),
                escuelaId = resultSet.getInt("escuela_id"),
                semestre = resultSet.getString("semestre"),
                email = resultSet.getString("email"),
                tipoUsuario = resultSet.getString("tipo_usuario"),
                creadoEn = resultSet.getString("creado_en")
            )
        } else {
            throw Exception("User not found")
        }
    }

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param userId ID del usuario a actualizar
     * @param usuario Nuevos datos del usuario
     */
    suspend fun update(userId: Int, usuario:Usuarios) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_USUARIO)
        statement.setString(1, usuario.dniUsuario)
        statement.setString(2, usuario.nombreUsuario)
        statement.setString(3, usuario.apellidoUsuario)
        statement.setString(4, usuario.celularUsuario)
        statement.setString(5, usuario.passwordHash)
        statement.setInt(6, usuario.escuelaId)
        statement.setString(7, usuario.semestre)
        statement.setString(8, usuario.email)
        statement.setString(9, usuario.tipoUsuario)
        statement.setInt(10, userId)
        statement.executeUpdate()
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param userId ID del usuario a eliminar
     */
    suspend fun delete(userId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_USUARIO)
        statement.setInt(1, userId)
        statement.executeUpdate()
    }

    /**
     * Obtiene una lista de usuarios por tipo.
     *
     * @param tipoUsuario Tipo de usuario a buscar
     * @return Lista de usuarios del tipo especificado
     */
    suspend fun readByType(tipoUsuario: String): List<Usuarios> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_USUARIOS_BY_TIPO)
        statement.setString(1, tipoUsuario)
        val resultSet = statement.executeQuery()

        val usuariosList = mutableListOf<Usuarios>()

        while (resultSet.next()) {
            val usuario = Usuarios(
                userId = resultSet.getInt("user_id"),
                dniUsuario = resultSet.getString("dni_usuario"),
                nombreUsuario = resultSet.getString("nombre_usuario"),
                apellidoUsuario = resultSet.getString("apellido_usuario"),
                celularUsuario = resultSet.getString("celular_usuario"),
                passwordHash = resultSet.getString("password_hash"),
                escuelaId = resultSet.getInt("escuela_id"),
                semestre = resultSet.getString("semestre"),
                email = resultSet.getString("email"),
                tipoUsuario = resultSet.getString("tipo_usuario"),
                creadoEn = resultSet.getString("creado_en")
            )
            usuariosList.add(usuario)
        }

        return@withContext usuariosList
    }

    /**
     * Busca usuarios por tipo y escuela que no sean jefes de grupo.
     *
     * @param tipoUsuario Tipo de usuario a buscar
     * @param escuelaId ID de la escuela a la que pertenecen
     * @return Lista de vistas de usuario del tipo y escuela especificados que no son jefes de grupo
     */
    suspend fun findUsuariosByTypeAndSchool(tipoUsuario: String, escuelaId: Int): List<UserView> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_USUARIOS_BY_TIPO_AND_SCHOOL)
        statement.setString(1, tipoUsuario)
        statement.setInt(2, escuelaId)
        val resultSet = statement.executeQuery()

        val usuariosList = mutableListOf<UserView>()

        while (resultSet.next()) {
            val usuario = UserView(
                id = resultSet.getInt("user_id"),
                fullName = resultSet.getString("nombre_completo"),
                semester = resultSet.getString("semestre")
            )
            usuariosList.add(usuario)
        }

        return@withContext usuariosList
    }

    /**
     * Busca usuarios por tipo, escuela y semestre que no pertenecen a ningún grupo.
     *
     * @param tipoUsuario Tipo de usuario a buscar
     * @param escuelaId ID de la escuela a la que pertenecen
     * @param semestre Semestre académico de los usuarios
     * @return Lista de vistas de usuario que cumplen con los criterios especificados y no pertenecen a ningún grupo
     */
    suspend fun findUsuariosByTypeAndSchoolAndSemester(tipoUsuario: String, escuelaId: Int, semestre: String): List<UserView> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_USUARIOS_BY_TIPO_AND_SCHOOL_AND_SEMESTER)
        statement.setString(1, tipoUsuario)
        statement.setInt(2, escuelaId)
        statement.setString(3, semestre)
        val resultSet = statement.executeQuery()

        val usuariosList = mutableListOf<UserView>()

        while (resultSet.next()) {
            val usuario = UserView(
                id = resultSet.getInt("user_id"),
                fullName = resultSet.getString("nombre_completo"),
                semester = resultSet.getString("semestre")
            )
            usuariosList.add(usuario)
        }

        return@withContext usuariosList
    }
}