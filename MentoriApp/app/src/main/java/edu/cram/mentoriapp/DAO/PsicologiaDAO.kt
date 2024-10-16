package edu.cram.mentoriapp.DAO

import android.content.Context
import android.widget.TextView
import android.widget.Toast
import edu.cram.mentoriapp.Model.Cities
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient

class PsicologiaDAO(val context: Context) {

    private var apiRest: ApiRest = RetrofitClient.makeRetrofitClient()

    /*Ejemplo*/
    suspend fun fetchCity(id: Int, textView: TextView) {
        try {
            val response = apiRest.getCity(id)
            if (response.isSuccessful) {
                val city = response.body()
                city?.let {
                    textView.text = "Ciudad: ${it.name}, Población: ${it.population}"
                } ?: run {
                    textView.text = "No se encontró la ciudad con ID: $id"
                }
            } else {
                Toast.makeText(context, "Error al obtener la ciudad", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun createCity(city: Cities) {
        try {
            val response = apiRest.createCity(city)
            if (response.isSuccessful) {
                val newCityId = response.body()
                Toast.makeText(context, "Ciudad creada con ID: $newCityId", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error al crear la ciudad", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun createUser(user: Usuario) {
        try {
            val response = apiRest.createUsuario(user)
            if (response.isSuccessful) {
                val newUserId = response.body()
                Toast.makeText(context, "Usuario creado con ID: $newUserId", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error al crear el usuario", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

}