package edu.cram.mentoriapp.DAO

import android.content.Context
import android.widget.Toast
import edu.cram.mentoriapp.Model.Evento
import edu.cram.mentoriapp.Model.Horario
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

    suspend fun createEvent(event: Evento) {
        try {
            val response = apiRest.createEvento(event)
            if (response.isSuccessful) {
                val newEventId = response.body()
                Toast.makeText(context, "Evento creado con ID: $newEventId", Toast.LENGTH_SHORT).show()
            } else {
                // Mostrar el código de error y el mensaje de la API
                val errorBody = response.errorBody()?.string() ?: "Cuerpo de error vacío"
                Toast.makeText(context, "Error al crear el evento: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun createHorario(horario: Horario): Horario {
        try {
            val response = apiRest.createHorario(horario)
            if (response.isSuccessful) {
                val newhorarioId = response.body()
                Toast.makeText(context, "horario creado con ID: $newhorarioId", Toast.LENGTH_SHORT).show()
                return horario.copy(horarioId = newhorarioId)
            } else {
                // Mostrar el código de error y el mensaje de la API
                val errorBody = response.errorBody()?.string() ?: "Cuerpo de error vacío"
                Toast.makeText(context, "Error al crear el horario: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        return horario.copy(horarioId = 1)
    }

}