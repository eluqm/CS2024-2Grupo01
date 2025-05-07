package com.example.plugins

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.FileInputStream

object FirebaseAdmin {
    fun initialize() {
        try {
            // Ruta a tu archivo de credenciales (lo descargas desde la consola de Firebase)
            val serviceAccount = FileInputStream("C:\\Users\\CharlsMikhail\\VSC\\CS2024-2Grupo01\\mentoria-backend\\src\\main\\kotlin\\com\\example\\plugins\\mentoriapp-1cb9a-firebase-adminsdk-fbsvc-131a92f35f.json")

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