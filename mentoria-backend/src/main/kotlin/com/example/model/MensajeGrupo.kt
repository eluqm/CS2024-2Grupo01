package com.example.model

data class MensajeGrupo(
    val mensajeId: Int,
    val grupoId: Int,
    val remitenteId: Int,
    val textoMensaje: String,
    val enviadoEn: String
)
