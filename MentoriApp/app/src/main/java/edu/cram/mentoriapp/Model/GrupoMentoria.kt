package edu.cram.mentoriapp.Model

import java.time.LocalDateTime

data class GrupoMentoria(
    val grupoId: Int? = null,
    val jefeId: Int,
    val nombre: String,
    val horarioId: Int,
    val descripcion: String?,
    val creadoEn: String? = null
)


data class GrupoMentoriaPlus(
    val grupoId: Int? = null,
    val jefeId: Int,
    val jefeName: String,
    val nombre: String,
    val descripcion: String?,
    val creadoEn: String? = null
)