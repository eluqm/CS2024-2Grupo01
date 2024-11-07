package edu.cram.mentoriapp.Model

import java.io.Serializable

data class UsuarioLista(
    val id: Int? = null,
    val nombreCompletoUsuario: String,
    val email: String,
    val celularUsuario: String,
    val dniUsuario: String
): Serializable