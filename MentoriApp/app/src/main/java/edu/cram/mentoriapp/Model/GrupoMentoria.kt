package edu.cram.mentoriapp.Model

import java.io.Serializable
import java.time.LocalDateTime

data class GrupoMentoria(
    val grupoId: Int? = null,
    val jefeId: Int,
    val nombre: String,
    val horarioId: Int? = null,
    val descripcion: String?,
    val creadoEn: String? = null
): Serializable

data class GrupoMentoriaPlus(
    val grupoId: Int? = null,
    val jefeId: Int,
    val jefeName: String,
    val nombre: String,
    val descripcion: String?,
    val creadoEn: String? = null
)