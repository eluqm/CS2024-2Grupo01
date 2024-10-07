package edu.cram.mentoriapp.Model

import java.time.LocalDateTime

data class Notificacion(
    val notificacionId: Int,
    val userId: Int,
    val textoNotificacion: String,
    val tipoNotificacion: String,
    val creadoEn: LocalDateTime,
    val leido: Boolean,
    val eventoId: Int
)
