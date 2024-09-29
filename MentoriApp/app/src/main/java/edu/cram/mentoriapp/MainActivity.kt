package edu.cram.mentoriapp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.cram.mentoriapp.Model.Cities
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var apiRest: ApiRest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textView)

        // Inicializa ApiService desde RetrofitServiceFactory
        apiRest = RetrofitClient.makeRetrofitClient()

        // Llama a las funciones para interactuar con el API
        lifecycleScope.launch {
            // Ejemplo: Obtener una ciudad con ID 1 y mostrar los datos
            fetchCity(1, textView)

            // Ejemplo: Crear una nueva ciudad
            val newCity = Cities(name = "Puno", population = 123)
            createCity(newCity)
        }
    }

    private suspend fun fetchCity(id: Int, textView: TextView) {
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
                Toast.makeText(this, "Error al obtener la ciudad", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun createCity(city: Cities) {
        try {
            val response = apiRest.createCity(city)
            if (response.isSuccessful) {
                val newCityId = response.body()
                Toast.makeText(this, "Ciudad creada con ID: $newCityId", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al crear la ciudad", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}