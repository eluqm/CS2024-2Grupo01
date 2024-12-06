package edu.cram.mentoriapp.Model

data class HorarioCell(
    val horaInicio: String,
    val dia: String,
    val horaFin: String?,
    val lugar: String?,
    val horarioId: Int?,
    val estado: Boolean = false,
    val nombreGrupo: String? = null,
    val nombreCompletoJefe: String? = null,
    val nombreEscuela: String? = null,
    val esConflicto: Boolean = false, // Para indicar conflictos
    val eventos: List<HorarioDetalles>? = null // Lista de eventos conflictivos
)


