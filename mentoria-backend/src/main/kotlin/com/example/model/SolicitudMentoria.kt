package com.example.model

import java.time.LocalDateTime


data class SolicitudMentoria(
    val solicitudId: Int,
    val coordinadorId: Int,
    val mentorId: Int,
    val fechaSolicitud: LocalDateTime,
    val estado: String,
    val mensaje: String
)
