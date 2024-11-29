package edu.cram.mentoriapp.Model

import java.io.Serializable

data class HorarioUpdate(
    val horarioId: Int,
    val lugar: String,
    val estado: Boolean = true
): Serializable
