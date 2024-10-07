package edu.cram.mentoriapp.Psicologia

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.cram.mentoriapp.Model.Cities
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch

class PiscoHomeFragment : Fragment(R.layout.fragment_pisco_home) {

    private lateinit var apiRest: ApiRest

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textView = view.findViewById<TextView>(R.id.tv_title)


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

    /*Ejemplo*/
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
                Toast.makeText(requireContext(), "Error al obtener la ciudad", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun createCity(city: Cities) {
        try {
            val response = apiRest.createCity(city)
            if (response.isSuccessful) {
                val newCityId = response.body()
                Toast.makeText(requireContext(), "Ciudad creada con ID: $newCityId", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error al crear la ciudad", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


}