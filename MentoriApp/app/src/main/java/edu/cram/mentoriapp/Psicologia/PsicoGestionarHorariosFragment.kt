package edu.cram.mentoriapp.Psicologia

import HorarioAdapter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.Horario
import edu.cram.mentoriapp.Model.HorarioCell
import edu.cram.mentoriapp.Model.SesionMentoriaLista
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PsicoGestionarHorariosFragment : Fragment(R.layout.fragment_psico_gestionar_horarios) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var apiRest: ApiRest
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewHorario)
        apiRest = RetrofitClient.makeRetrofitClient()
        // Configurar el RecyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 7) // 1 columna para horas + 6 días

        // Llamar al API para obtener horarios
        fetchHorarios()
    }

    private fun fetchHorarios() {

        // Llamar al API dentro de un coroutine
        lifecycleScope.launch {
            try {
                val response = apiRest.getHorarios()
                if (response.isSuccessful) {
                    val horarios = response.body() ?: emptyList()
                    setupRecyclerView(horarios)
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView(horarios: List<Horario>) {
        val horas = listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00")
        val dias = listOf("Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado")

        // Crear las celdas dinámicamente con los datos del API
        val celdas = mutableListOf<HorarioCell>()
        for (hora in horas) {
            // Agregar primera columna con las horas
            celdas.add(HorarioCell(hora, "Horas", lugar = null))

            // Agregar las celdas para cada día
            for (dia in dias) {
                val evento = horarios.find { it.dia == dia && it.horaInicio == hora }
                celdas.add(HorarioCell(hora, dia, evento?.lugar))
            }
        }

        // Asignar el adaptador al RecyclerView
        recyclerView.adapter = HorarioAdapter(celdas) { horario -> onItemSelected(horario) }
    }

    private fun onItemSelected(horario: HorarioCell) {
        Toast.makeText(requireActivity(), horario.lugar + "hola", Toast.LENGTH_SHORT).show()
    }

}
