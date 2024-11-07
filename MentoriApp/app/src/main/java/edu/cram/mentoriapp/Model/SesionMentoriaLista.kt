package edu.cram.mentoriapp.Model

import java.io.Serializable

data class SesionMentoriaLista(
    val temaSesion: String,
    val lugar: String,
    val fechaRegistrada: String,
    val participantes: String
): Serializable
