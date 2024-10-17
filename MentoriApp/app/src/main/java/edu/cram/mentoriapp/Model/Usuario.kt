package edu.cram.mentoriapp.Model

import java.io.Serializable

data class Usuario(
    val userId: Int? = null,
    val dniUsuario: String,
    val nombreUsuario: String,
    val apellidoUsuario: String,
    val celularUsuario: String,
    val passwordHash: String = "123456789",
    val escuelaId: Int,
    val semestre: String?,
    val email: String,
    val tipoUsuario: String = "mentoriado",
    val creadoEn: String? = null
): Serializable
