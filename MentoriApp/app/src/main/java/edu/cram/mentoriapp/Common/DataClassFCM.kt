package edu.cram.mentoriapp.Common

import java.io.Serializable

data class TokenRequest(val token: String, val userId: String? = null): Serializable

data class NotificationRequest(
    val token: String,
    val title: String,
    val body: String
): Serializable
data class NotificationResponse(
    val success: Boolean,
    val messageId: String? = null
): Serializable
data class DeviceRegistration(
    val token: String,
    val userId: Int? = null
): Serializable

data class TokenResponse(
    val success: Boolean,
    val message: String? = null
): Serializable

data class TokensResponse(
    val success: Boolean,
    val tokens: List<String>? = null,
    val message: String? = null
): Serializable