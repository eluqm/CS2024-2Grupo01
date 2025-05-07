package com.example.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification

class FCMService {

    // Enviar mensaje a un dispositivo específico
    fun sendMessageToDevice(token: String, title: String, body: String): String {
        val message = Message.builder()
            .setToken(token)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .build()

        return FirebaseMessaging.getInstance().send(message)
    }

    // Enviar mensaje a un tema/topic
    fun sendMessageToTopic(topic: String, title: String, body: String): String {
        val message = Message.builder()
            .setTopic(topic)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .build()

        return FirebaseMessaging.getInstance().send(message)
    }
}