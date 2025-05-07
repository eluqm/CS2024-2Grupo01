package edu.cram.mentoriapp.Common
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import edu.cram.mentoriapp.MainActivity
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: "Notificación"
        val body = remoteMessage.notification?.body ?: "Tienes un nuevo mensaje"

        showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Intenta obtener el userId desde SharedPreferences
        val sharedPrefs = applicationContext.getSharedPreferences("usuarioSesion", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getInt("userId", -1)

        if (userId != -1) {
            sendRegistrationToServer(token, userId)
        } else {
            Log.d("FCM", "No hay sesión iniciada. Token no registrado.")
        }

        Log.d("FCM", "Nuevo token FCM: $token")
    }

    private fun sendRegistrationToServer(token: String, userId: Int) {
        val apiRest = RetrofitClient.makeRetrofitClient()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deviceRegistration = DeviceRegistration(token, userId)
                val response = apiRest.registerFcmToken(deviceRegistration)

                if (response.isSuccessful) {
                    Log.d("FCM", "Token actualizado en el servidor correctamente")
                } else {
                    Log.e("FCM", "Error al actualizar token en servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error de conexión al actualizar token", e)
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "mi_canal_id",
                "Nombre del Canal",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, "mi_canal_id")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}