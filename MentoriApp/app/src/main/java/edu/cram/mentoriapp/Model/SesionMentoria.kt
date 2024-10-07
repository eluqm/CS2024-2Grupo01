package edu.cram.mentoriapp.Model

import java.time.LocalDateTime

data class SesionMentoria(
    val sesionId: Int,
    val grupoId: Int,
    val horaProgramada: LocalDateTime,
    val estado: String,
    val temaSesion: String,
    val notas: String?,
    val fotografia: ByteArray
)
