package edu.cram.mentoriapp.DAO

import android.content.Context
import android.util.Log
import android.widget.Toast
import edu.cram.mentoriapp.Model.Evento
import edu.cram.mentoriapp.Model.Horario
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommonDAO(val context: Context) {

    private var apiRest: ApiRest = RetrofitClient.makeRetrofitClient()


    suspend fun createUserIfNotExists(user: Usuario) {
        // Verificar si el usuario ya existe
        val exists = userExists(user.dniUsuario)

        if (exists) {
            Toast.makeText(context, "El usuario con el DNI: ${user.dniUsuario} ya existe.", Toast.LENGTH_SHORT).show()
        } else {
            // Si el usuario no existe, proceder a crearlo
            createUser(user)
        }
    }


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

    suspend fun userExists(dni: String): Boolean {
        return try {
            val response = apiRest.userExists(dni)
            if (response.isSuccessful) {
                // Acceder directamente al campo 'exists' en la respuesta
                response.body()?.exists ?: false
            } else {
                // Manejar el error de respuesta de la API
                val errorBody = response.errorBody()?.string() ?: "Cuerpo de error vacío"
                Toast.makeText(context, "Error al verificar usuario: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }


    suspend fun createEvent(event: Evento) {
        try {
            // Realizar la llamada a la API en el hilo de fondo
            val response = withContext(Dispatchers.IO) {
                apiRest.createEvento(event)
            }

            // Verificar si la respuesta fue exitosa
            if (response.isSuccessful) {
                val newEventId = response.body()

                // Ahora que la respuesta es exitosa, volvemos al hilo principal para mostrar el Toast
                withContext(Dispatchers.Main) {
                    Log.d("EventoCreado", "Evento creado con ID: $newEventId")
                    Toast.makeText(context, "Evento creado con ID: $newEventId", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Si hubo un error, volvemos al hilo principal para mostrar el mensaje de error
                withContext(Dispatchers.Main) {
                    val errorBody = response.errorBody()?.string() ?: "Cuerpo de error vacío"
                    Log.e("APIError", "Error al crear el evento: ${response.code()} - $errorBody")
                    Toast.makeText(context, "Error al crear el evento: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            // En caso de una excepción, volvemos al hilo principal para manejarla
            withContext(Dispatchers.Main) {
                Log.e("NetworkError", "Error de red: ${e.message}")
                Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    suspend fun createHorario(horario: Horario): Int? {
        return try {
            val response = apiRest.createHorario(horario)

            if (response.isSuccessful) {
                val newHorarioId = response.body()
                // Cambiar al hilo principal para mostrar el Toast
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Horario creado con ID: $newHorarioId", Toast.LENGTH_SHORT).show()
                }
                newHorarioId
            } else {
                val errorBody = response.errorBody()?.string() ?: "Cuerpo de error vacío"
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al crear el horario: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                }
                null
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            null
        }
    }


}