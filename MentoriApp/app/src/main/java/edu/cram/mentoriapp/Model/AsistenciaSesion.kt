package edu.cram.mentoriapp.Model

import java.time.LocalDateTime

data class AsistenciaSesion(
    val asistenciaId: Int,
    val sesionId: Int,
    val mentoriadoId: Int,
    val asistio: Boolean,
    val horaFechaRegistrada: LocalDateTime
)
