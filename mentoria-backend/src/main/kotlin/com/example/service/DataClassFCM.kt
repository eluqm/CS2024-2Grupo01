package com.example.service

import kotlinx.serialization.Serializable

@Serializable
data class DeviceRegistration(val token: String, val userId: Int? = null)

@Serializable
data class NotificationRequest(val token: String, val title: String, val body: String)

@Serializable
data class TopicNotificationRequest(val topic: String, val title: String, val body: String)

@Serializable
data class TokenResponse(val success: Boolean, val message: String? = null)

@Serializable
data class NotificationResponse(val success: Boolean, val messageId: String? = null)
