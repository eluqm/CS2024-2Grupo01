package com.example.model

data class SesionMentoria(
    val sesionId: Int,
    val grupoId: Int,
    val horaProgramada: String,
    val estado: String,
    val temaSesion: String,
    val notas: String?,
    val fotografia: ByteArray
)
