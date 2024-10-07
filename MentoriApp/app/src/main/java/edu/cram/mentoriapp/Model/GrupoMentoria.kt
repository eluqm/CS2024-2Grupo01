package edu.cram.mentoriapp.Model

import java.time.LocalDateTime

data class GrupoMentoria(
    val grupoId: Int,
    val mentorId: Int,
    val nombre: String,
    val horarioId: Int,
    val descripcion: String?,
    val creadoEn: LocalDateTime
)
