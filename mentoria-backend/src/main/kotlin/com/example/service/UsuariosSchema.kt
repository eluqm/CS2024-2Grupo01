package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement
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

@Serializable
data class UserExistResponse(val exists: Boolean)

@Serializable
data class UserView(
    val id: Int?,
    val fullName: String,
    val semester: String?
)


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

    }

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

    suspend fun userExistsByDni(dniUsuario: String): Boolean = withContext(Dispatchers.IO) {
        val query = "SELECT COUNT(*) FROM usuarios WHERE dni_usuario = ?"
        val statement = connection.prepareStatement(query)
        statement.setString(1, dniUsuario)
        val resultSet = statement.executeQuery()
        resultSet.next()
        return@withContext resultSet.getInt(1) > 0
    }


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

    suspend fun delete(userId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_USUARIO)
        statement.setInt(1, userId)
        statement.executeUpdate()
    }

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

    suspend fun findUsuariosByTypeAndSchool(tipoUsuario: String, escuelaId: Int): List<UserView> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_USUARIOS_BY_TIPO_AND_SCHOOL) // Asegúrate de que la consulta está actualizada
        statement.setString(1, tipoUsuario) // Filtro por tipo de usuario
        statement.setInt(2, escuelaId) // Filtro por ID de escuela
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

    suspend fun findUsuariosByTypeAndSchoolAndSemester(tipoUsuario: String, escuelaId: Int, semestre: String): List<UserView> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_USUARIOS_BY_TIPO_AND_SCHOOL_AND_SEMESTER) // Asegúrate de que la consulta está actualizada
        statement.setString(1, tipoUsuario) // Filtro por tipo de usuario
        statement.setInt(2, escuelaId) // Filtro por ID de escuela
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
