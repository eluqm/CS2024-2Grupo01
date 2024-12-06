package edu.cram.mentoriapp.Model

data class HorarioCell(
    val horaInicio: String, // Hora de la celda (para encabezado de filas o celda normal)
    val dia: String,  // Día de la celda (para encabezado de columnas o celda normal)
    val horaFin: String?, // Hora de finalización del evento o actividad (si existe)
    val lugar: String? = null, // Lugar del evento o actividad (si existe)
    val horarioId: Int? = null,
    val estado: Boolean? = null,
    val nombreGrupo: String? = null,
    val nombreCompletoJefe: String? = null,
    val nombreEscuela: String? = null
)
