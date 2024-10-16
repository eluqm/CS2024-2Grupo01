package com.example.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.sql.Date
import java.time.Instant
import java.time.LocalDateTime

@Serializable
data class Usuarios(
    val user_id: Int,
    val dni_usuario: String,
    val nombre_usuario: String,
    val apellido_usuario: String,
    val celular_usuario: String,
    val password_hash: String,
    val escuelaId: Int,
    val semestre: String?,
    val email: String,
    val tipo_usuario: String,
    @Contextual val creado_en: LocalDateTime?
)
