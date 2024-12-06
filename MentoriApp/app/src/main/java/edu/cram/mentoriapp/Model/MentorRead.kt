package edu.cram.mentoriapp.Model

import java.io.Serializable

data class MentorRead(
    val userId: Int? = null,
    val nombreCompleto: String,
    val celularUsuario: String,
    val correo: String
): Serializable