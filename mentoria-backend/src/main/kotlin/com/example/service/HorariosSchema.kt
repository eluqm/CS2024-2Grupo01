package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement
import java.sql.Time
@Serializable
data class Horario(
    val horarioId: Int? = null,
    val lugar: String?,
    val dia: String,
    val horaInicio: String,
    val horaFin: String,
    val estado: Boolean
)

@Serializable
data class HorarioUpdate(
    val horarioId: Int,
    val lugar: String,
    val estado: Boolean = true
)

class HorariosService(private val connection: Connection) {
    companion object {
        private const val UPDATE_GRUPO_HORARIO = "UPDATE grupos SET horario_id = ? WHERE jefe_id = ? AND horario_id IS NULL"
        private const val INSERT_HORARIO = "INSERT INTO horarios (lugar, dia, hora_inicio, hora_fin, estado) VALUES (?, ?, ?, ?, ?)"
        private const val SELECT_HORARIO_BY_ID = "SELECT * FROM horarios WHERE horario_id = ?"
        private const val SELECT_HORARIO_BY_ID2 = "SELECT * FROM horarios"
        private const val UPDATE_PARTIAL_HORARIO = "UPDATE horarios SET lugar = ?, estado = ? WHERE horario_id = ?"
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
    suspend fun create2(horario: Horario, jefeId: Int) = withContext(Dispatchers.IO) {
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
    private suspend fun updateGrupoHorario(horarioId: Int, jefeId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_GRUPO_HORARIO)
        statement.setInt(1, horarioId) // Set the horario_id
        statement.setInt(2, jefeId)   // Find the group by jefe_id
        val rowsUpdated = statement.executeUpdate()
        if (rowsUpdated == 0) {
            throw Exception("No group found with jefe_id = $jefeId and null horario_id")
        }
    }
    suspend fun read(horarioId: Int): Horario = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_HORARIO_BY_ID)
        statement.setInt(1, horarioId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext Horario(
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

    suspend fun readAll(): List<Horario> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_HORARIO_BY_ID2)
        val resultSet = statement.executeQuery()

        val horarios = mutableListOf<Horario>()
        while (resultSet.next()) {
            horarios.add(
                Horario(
                    horarioId = resultSet.getInt("horario_id"),
                    lugar = resultSet.getString("lugar"),
                    dia = resultSet.getString("dia"),
                    horaInicio = resultSet.getTime("hora_inicio").toLocalTime().toString(),
                    horaFin = resultSet.getTime("hora_fin").toLocalTime().toString(),
                    estado = resultSet.getBoolean("estado")
                )
            )
        }
        return@withContext horarios
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

    suspend fun updatePartial(horarioUpdate: HorarioUpdate) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_PARTIAL_HORARIO)
        statement.setString(1, horarioUpdate.lugar)
        statement.setBoolean(2, horarioUpdate.estado)
        statement.setInt(3, horarioUpdate.horarioId)

        statement.executeUpdate()
    }

    suspend fun delete(horarioId: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_HORARIO)
        statement.setInt(1, horarioId)
        statement.executeUpdate()
    }
}
