package edu.cram.mentoriapp.Model

import java.time.LocalDateTime

data class SolicitudMentoria(
    val solicitudId: Int? = null,
    val coordinadorId: Int,
    val mentorId: Int,
    val fechaSolicitud: LocalDateTime,
    val estado: String,
    val mensaje: String
)