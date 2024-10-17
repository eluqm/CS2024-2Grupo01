package com.example.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.sql.Date
import java.time.Instant
import java.time.LocalDateTime

@Serializable
data class Usuarios(
    val userId: Int,
    val dniUsuario: String,
    val nombreUsuario: String,
    val apellidoUsuario: String,
    val celularUsuario: String,
    val passwordHash: String,
    val escuelaId: Int,
    val semestre: String?,
    val email: String,
    val tipoUsuario: String,
    val creadoEn: String
)

