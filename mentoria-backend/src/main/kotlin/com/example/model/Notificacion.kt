package com.example.model

data class Notificacion(
    val notificacionId: Int,
    val userId: Int,
    val textoNotificacion: String,
    val tipoNotificacion: String,
    val creadoEn: String,
    val leido: Boolean,
    val eventoId: Int
)
