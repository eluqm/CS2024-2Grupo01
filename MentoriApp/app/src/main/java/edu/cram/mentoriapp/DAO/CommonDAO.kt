package edu.cram.mentoriapp.DAO

import android.content.Context
import android.widget.Toast
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient

class CommonDAO(val context: Context) {

    private var apiRest: ApiRest = RetrofitClient.makeRetrofitClient()


    suspend fun createUser(user: Usuario) {
        try {
            val response = apiRest.createUsuario(user)
            if (response.isSuccessful) {
                val newUserId = response.body()
                Toast.makeText(context, "Usuario creado con ID: $newUserId", Toast.LENGTH_SHORT).show()
            } else {
                // Mostrar el código de error y el mensaje de la API
                val errorBody = response.errorBody()?.string() ?: "Cuerpo de error vacío"
                Toast.makeText(context, "Error al crear el usuario: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


}