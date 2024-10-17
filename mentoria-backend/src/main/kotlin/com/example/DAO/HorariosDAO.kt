package com.example.DAO

import com.example.model.Horario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement
import java.sql.Time

class HorariosDAO(private val connection: Connection) {
    companion object {
        private const val INSERT_HORARIO = "INSERT INTO horarios (lugar, dia, hora_inicio, hora_fin, estado) VALUES (?, ?, ?, ?, ?)"
        private const val SELECT_HORARIO_BY_ID = "SELECT * FROM horarios WHERE horario_id = ?"
        private const val UPDATE_HORARIO = "UPDATE horarios SET lugar = ?, dia = ?, hora_inicio = ?, hora_fin = ?, estado = ? WHERE horario_id = ?"
        private const val DELETE_HORARIO = "DELETE FROM horarios WHERE horario_id = ?"
    }

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

    suspend fun read(horarioId: Int): Horario = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_HORARIO_BY_ID)
        statement.setInt(1, horarioId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Horario(
                horarioId = resultSet.getInt("horario_id"),
                lugar = resultSet.getString("lugar"),
                dia = resultSet.getString("dia"),
                horaInicio = resultSet.getTime("hora_inicio").toLocalTime().toString(),
                horaFin = resultSet.getTime("hora_fin").toLocalTime().toString(),
                estado = resultSet.getBoolean("estado")
            )
        } else {
            throw Exception("Horario not found")
        }
    }

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

    suspend fun delete(horarioId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_HORARIO)
        statement.setInt(1, horarioId)
        statement.executeUpdate()
    }
}
