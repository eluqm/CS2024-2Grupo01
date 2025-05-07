package edu.cram.mentoriapp.Common
import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FCMManager(private val context: Context) {

    fun getAndRegisterToken(userId: Int, callback: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCMManager", "Error al obtener el token de FCM", task.exception)
                callback(null)
                return@OnCompleteListener
            }

            // Obtener el token
            val token = task.result

            // Enviar token + userId al servidor
            sendTokenToServer(token, userId)

            // Devolver el token mediante callback
            callback(token)
        })
    }

    private fun sendTokenToServer(token: String, userId: Int) {
        val apiRest = RetrofitClient.makeRetrofitClient()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deviceRegistration = DeviceRegistration(token, userId)
                val response = apiRest.registerFcmToken(deviceRegistration)

                if (response.isSuccessful) {
                    Log.d("FCMManager", "Token enviado correctamente al servidor")
                } else {
                    Log.e("FCMManager", "Error al enviar token: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("FCMManager", "Error de conexión al enviar token", e)
            }
        }
    }
}