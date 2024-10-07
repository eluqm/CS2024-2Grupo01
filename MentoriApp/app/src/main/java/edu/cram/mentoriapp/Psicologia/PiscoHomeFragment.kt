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
import edu.cram.mentoriapp.DAO.PsicologiaDAO
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
        val psicoDao = PsicologiaDAO(requireContext())


        // Llama a las funciones para interactuar con el API
        lifecycleScope.launch {
            // Ejemplo: Obtener una ciudad con ID 1 y mostrar los datos
            psicoDao.fetchCity(1, textView)

            // Ejemplo: Crear una nueva ciudad
            val newCity = Cities(name = "Puno", population = 123)
            psicoDao.createCity(newCity)
        }
    }




}