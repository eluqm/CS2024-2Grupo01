package com.example.model

data class Horario(
    val horarioId: Int,
    val lugar: String?,
    val dia: String,
    val horaInicio: String,
    val horaFin: String,
    val estado: Boolean
)
