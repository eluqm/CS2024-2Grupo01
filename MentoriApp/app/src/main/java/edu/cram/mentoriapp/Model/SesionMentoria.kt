package edu.cram.mentoriapp.Model

import java.time.LocalDateTime

data class SesionMentoria(
    val sesionId: Int? = null,
    val grupoId: Int,
    val estado: String,
    val temaSesion: String,
    val notas: String?,
    val fotografia: ByteArray
)
