package edu.cram.mentoriapp.Model

import java.io.Serializable
import java.time.LocalTime

data class Horario(
    val horarioId: Int? = null,
    val lugar: String?,
    val dia: String,
    val horaInicio: String,
    val horaFin: String,
    val estado: Boolean
): Serializable