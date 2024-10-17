package edu.cram.mentoriapp.Model

import java.time.LocalDateTime

data class Usuarios(
    val userId: Int? = null,
    val dniUsuario: String,
    val nombreUsuario: String,
    val apellidoUsuario: String,
    val celularUsuario: String,
    val passwordHash: String,
    val escuelaId: Int,
    val semestre: String?,
    val email: String,
    val tipoUsuario: String,
    val creadoEn: LocalDateTime
)

