package com.example.plugins

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.FileInputStream

object FirebaseAdmin {
    fun initialize() {
        try {
            // Ruta a tu archivo de credenciales (lo descargas desde la consola de Firebase)
            val serviceAccount = FileInputStream("src/main/resources/mentoriapp-1cb9a-firebase-adminsdk-fbsvc-248e6a04a6.json")
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            FirebaseApp.initializeApp(options)
            println("Firebase Admin SDK inicializado correctamente")
        } catch (e: Exception) {
            println("Error al inicializar Firebase Admin SDK: ${e.message}")
        }
    }
}