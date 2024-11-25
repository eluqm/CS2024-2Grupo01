package edu.cram.mentoriapp.Model

data class HorarioUpdate(
    val horarioId: Int,
    val lugar: String,
    val estado: Boolean = true
)
