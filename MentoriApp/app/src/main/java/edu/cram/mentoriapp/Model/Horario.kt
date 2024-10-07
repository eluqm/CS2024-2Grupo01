package edu.cram.mentoriapp.Model

import java.time.LocalTime

data class Horario(
    val horarioId: Int,
    val lugar: String,
    val dia: String,
    val horaInicio: LocalTime,
    val horaFin: LocalTime,
    val estado: Boolean
)