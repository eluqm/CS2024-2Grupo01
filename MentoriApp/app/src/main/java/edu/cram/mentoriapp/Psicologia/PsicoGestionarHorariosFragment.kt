package edu.cram.mentoriapp.Psicologia

import HorarioAdapter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.Horario
import edu.cram.mentoriapp.Model.HorarioCell
import edu.cram.mentoriapp.R

class PsicoGestionarHorariosFragment : Fragment(R.layout.fragment_psico_gestionar_horarios) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewHorario)

        // Configurar el RecyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 7) // 1 columna para horas + 6 días

        // Simulación de datos (puedes reemplazar esto con datos reales de tu base de datos)
        val horas = listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00")
        val dias = listOf("Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado")

        val horarios = listOf(
            Horario(1, "Aula 1", "Lunes", "08:00", "10:00", true),
            Horario(2, "Laboratorio", "Martes", "10:00", "12:00", true),
            Horario(3, "Biblioteca", "Jueves", "14:00", "16:00", true),
        )

        // Crear las celdas dinámicamente
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
        recyclerView.adapter = HorarioAdapter(celdas)
    }
}
