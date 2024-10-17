package edu.cram.mentoriapp.Model

data class Evento(
    val eventoId: Int? = null,
    val nombre: String,
    val horarioId: Int,
    val descripcion: String?,
    val poster: ByteArray,
    val url: String?
)