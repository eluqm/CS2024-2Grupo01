package edu.cram.mentoriapp.Model

data class EvaluacionComentario(
    val evaluacionId: Int,
    val evaluadoId: Int,
    val evaluadorId: Int,
    val puntuacionCategorica: String,
    val comentario: String?
)