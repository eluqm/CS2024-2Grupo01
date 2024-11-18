package edu.cram.mentoriapp.Model

import java.io.Serializable

data class Chat(
    val emisor: String,
    val fecha: String,
    val mensaje: String,
    val hora: String
): Serializable
