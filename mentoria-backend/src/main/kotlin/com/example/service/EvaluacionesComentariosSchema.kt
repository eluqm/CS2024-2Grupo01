package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

@Serializable
data class EvaluacionComentario(
    val evaluadoId: Int,
    val evaluadorId: Int,
    val puntuacionCategorica: String,
    val comentario: String
)

class EvaluacionesComentariosService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_EVALUACIONES_COMENTARIOS =
            "CREATE TABLE EVALUACIONES_COMENTARIOS (EVALUACION_ID SERIAL PRIMARY KEY, EVALUADO_ID INT, EVALUADOR_ID INT, PUNTUACION_CATEGORICA VARCHAR(255), COMENTARIO TEXT);"
        private const val INSERT_EVALUACION = "INSERT INTO evaluaciones_comentarios (evaluado_id, evaluador_id, puntuacion_categorica, comentario) VALUES (?, ?, ?, ?)"
        private const val SELECT_EVALUACION_BY_ID = "SELECT * FROM evaluaciones_comentarios WHERE evaluacion_id = ?"
        private const val UPDATE_EVALUACION = "UPDATE evaluaciones_comentarios SET evaluado_id = ?, evaluador_id = ?, puntuacion_categorica = ?, comentario = ? WHERE evaluacion_id = ?"
        private const val DELETE_EVALUACION = "DELETE FROM evaluaciones_comentarios WHERE evaluacion_id = ?"
    }

    init {
        val statement = connection.createStatement()
        // Descomentar la siguiente línea para crear la tabla si es necesario
        // statement.executeUpdate(CREATE_TABLE_EVALUACIONES_COMENTARIOS)
    }

    // Crear nueva evaluación
    suspend fun create(evaluacion: com.example.service.EvaluacionComentario): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_EVALUACION, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, evaluacion.evaluadoId)
        statement.setInt(2, evaluacion.evaluadorId)
        statement.setString(3, evaluacion.puntuacionCategorica)
        statement.setString(4, evaluacion.comentario)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted evaluacion")
        }
    }

    // Leer una evaluación
    suspend fun read(evaluacionId: Int): com.example.service.EvaluacionComentario = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_EVALUACION_BY_ID)
        statement.setInt(1, evaluacionId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext EvaluacionComentario(
                evaluadoId = resultSet.getInt("evaluado_id"),
                evaluadorId = resultSet.getInt("evaluador_id"),
                puntuacionCategorica = resultSet.getString("puntuacion_categorica"),
                comentario = resultSet.getString("comentario")
            )
        } else {
            throw Exception("Evaluacion not found")
        }
    }

    // Actualizar una evaluación
    suspend fun update(evaluacionId: Int, evaluacion: EvaluacionComentario) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_EVALUACION)
        statement.setInt(1, evaluacion.evaluadoId)
        statement.setInt(2, evaluacion.evaluadorId)
        statement.setString(3, evaluacion.puntuacionCategorica)
        statement.setString(4, evaluacion.comentario)
        statement.setInt(5, evaluacionId)
        statement.executeUpdate()
    }

    // Eliminar una evaluación
    suspend fun delete(evaluacionId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_EVALUACION)
        statement.setInt(1, evaluacionId)
        statement.executeUpdate()
    }
}
