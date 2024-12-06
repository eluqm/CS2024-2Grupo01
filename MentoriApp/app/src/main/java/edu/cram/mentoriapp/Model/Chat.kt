package edu.cram.mentoriapp.Model

import org.apache.xmlbeans.impl.xb.ltgfmt.FileDesc.Role
import java.io.Serializable

data class Chat(
    val emisor: String,
    val fecha: String,
    val mensaje: String,
    val hora: String,
    val rol: String
): Serializable
