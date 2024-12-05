package edu.cram.mentoriapp.Model

data class HorarioDetalles(
    var horarioId: Int,
    val lugar: String,
    val dia: String,
    val horaInicio: String,
    val horaFin: String,
    val estado: Boolean,
    val nombreGrupo: String?,
    val nombreCompletoJefe: String?,
    val nombreEscuela: String?
)