package edu.cram.mentoriapp.Model

import java.time.LocalDateTime

data class MensajeGrupo(
    val mensajeId: Int,
    val grupoId: Int,
    val remitenteId: Int,
    val textoMensaje: String,
    val enviadoEn: LocalDateTime
)