package edu.cram.mentoriapp.Psicologia

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.cram.mentoriapp.DAO.PsicologiaDAO
import edu.cram.mentoriapp.Model.Cities
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch

class PiscoHomeFragment : Fragment(R.layout.fragment_pisco_home) {

    private lateinit var apiRest: ApiRest

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textView = view.findViewById<TextView>(R.id.tv_title)
        val cerrar_sesion = view.findViewById<ImageButton>(R.id.cerrar_sesion)
        val psicoDao = PsicologiaDAO(requireContext())


        cerrar_sesion.setOnClickListener(){
            cerrarSesion()
            view.findNavController().navigate(R.id.loginFragment, null)
        }

        // Llama a las funciones para interactuar con el API
        lifecycleScope.launch {
            // Ejemplo: Obtener una ciudad con ID 1 y mostrar los datos
            //psicoDao.fetchCity(1, textView)

            // Ejemplo: Crear una nueva ciudad
            //val newCity = Cities(name = "Puno", population = 123)
            //psicoDao.createUser(newCity)

            val user = Usuario(
                userId = 123,
                dniUsuario = "12345678",
                nombreUsuario = "Juan",
                apellidoUsuario = "Pérez",
                celularUsuario = "987654321",
                passwordHash = "12345",  // Aquí puedes usar el hash real si lo tienes.
                escuelaId = 1,
                semestre = "III",
                email = "juan.perez@example.com",
                tipoUsuario = "mentor",
                creadoEn = "23123"// Puedes cambiar esto si el tipo de usuario es diferente.
                )
            psicoDao.createUser(user);
        }

    }

    private fun cerrarSesion() {
        val sharedPreferences = requireContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // Limpiar todos los datos guardados
        editor.apply() // Aplicar los cambios

        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }



}