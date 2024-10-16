package com.example.model
data class Grupo(
    val grupoId: Int,
    val jefeId: Int,
    val nombre: String,
    val horarioId: Int,
    val descripcion: String?,
    val creadoEn: String
)
