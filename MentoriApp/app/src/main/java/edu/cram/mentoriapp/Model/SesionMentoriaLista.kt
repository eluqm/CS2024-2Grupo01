package edu.cram.mentoriapp.Model

import java.io.Serializable

data class SesionMentoriaLista(
    val temaSesion: String,
    val lugar: String,
    val fechaRegistrada: String,
    val numeroParticipantes: String
): Serializable