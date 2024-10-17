package edu.cram.mentoriapp.Model

data class EvaluacionComentario(
    val evaluacionId: Int? = null,
    val evaluadoId: Int,
    val evaluadorId: Int,
    val puntuacionCategorica: String,
    val comentario: String?
)