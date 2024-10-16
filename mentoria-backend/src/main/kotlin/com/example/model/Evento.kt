package com.example.model

data class Evento(
    val eventoId: Int,
    val nombre: String,
    val horarioId: Int,
    val descripcion: String?,
    val poster: ByteArray,
    val url: String?
)
