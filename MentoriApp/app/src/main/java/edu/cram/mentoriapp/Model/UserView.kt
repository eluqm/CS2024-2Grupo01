package edu.cram.mentoriapp.Model

import java.io.Serializable

data class UserView(
    val id: Int?,
    val fullName: String,
    val semester: String?
): Serializable

