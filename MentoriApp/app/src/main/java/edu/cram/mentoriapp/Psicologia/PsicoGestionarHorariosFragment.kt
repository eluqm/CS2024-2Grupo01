package edu.cram.mentoriapp.Psicologia

import HorarioAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.Horario
import edu.cram.mentoriapp.Model.HorarioCell
import edu.cram.mentoriapp.Model.HorarioUpdate
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


        apiRest = RetrofitClient.makeRetrofitClient()
        recyclerView = view.findViewById(R.id.recyclerViewHorario)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 6)
        val decoration1 = DividerItemDecoration(requireContext(), GridLayoutManager.HORIZONTAL)
        val decoration2 = DividerItemDecoration(requireContext(), GridLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(decoration1)
        recyclerView.addItemDecoration(decoration2)
        fetchHorarios()
    }

    private fun fetchHorarios() {

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
        val horas = listOf("07:15", "08:00", "08:45", "09:30", "10:15", "11:00", "11:45", "12:30", "13:15", "14:00", "14:45", "15:30", "16:15", "17:00")
        val dias = listOf("Lunes", "Martes", "Miercoles", "Jueves", "Viernes")

        val celdas = mutableListOf<HorarioCell>()
        for (hora in horas) {
            // Agregar primera columna con las horas
            celdas.add(HorarioCell("hora", "Horas", lugar = hora))

            // Agregar las celdas para cada día
            for (dia in dias) {
                val evento = horarios.find { it.dia == dia && it.horaInicio == hora }
                celdas.add(HorarioCell(hora, dia, evento?.lugar,evento?.horarioId))
            }
        }

        // Asignar el adaptador al RecyclerView
        recyclerView.adapter = HorarioAdapter(celdas) { horario -> onItemSelected(horario) }
    }

    private fun onItemSelected(horarioCell: HorarioCell) {
        // Verificar si el horarioId está disponible
        if (horarioCell.horarioId != null) {
            lifecycleScope.launch {
                try {
                    val response = apiRest.getHorario(horarioCell.horarioId)
                    if (response.isSuccessful) {
                        var horario = response.body()

                        if (horario != null) {
                            horario.horarioId = horarioCell.horarioId
                        }

                            horarioCell.horarioId
                        if (horario != null) {
                            initDialogo(horario)
                        } else {
                            Toast.makeText(requireContext(), "Horario no encontrado", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error al obtener horario: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "No hay información para esta celda", Toast.LENGTH_SHORT).show()
        }
    }



    @SuppressLint("MissingInflatedId")
    private fun initDialogo(horario: Horario) {
        // Crear el diálogo
        val dialogView = layoutInflater.inflate(R.layout.dialog_horario, null)
        val editTextLugar = dialogView.findViewById<EditText>(R.id.editTextLugar)
        val textViewDia = dialogView.findViewById<TextView>(R.id.textViewDia)
        val textViewHoraInicio = dialogView.findViewById<TextView>(R.id.textViewHoraInicio)
        val textViewHoraFin = dialogView.findViewById<TextView>(R.id.textViewHoraFin)

        // Configurar los valores iniciales
        editTextLugar.setText(horario.lugar)
        textViewDia.text = horario.dia
        textViewHoraInicio.text = horario.horaInicio
        textViewHoraFin.text = horario.horaFin

        // Crear el diálogo de alerta
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Detalles del Horario")
            .setView(dialogView)
            .setPositiveButton("Aceptar") { dialog, _ ->
                Log.d("aea", "aceptar")
                val lugarActualizado = editTextLugar.text.toString()
                Log.d("aea", "aceptar2 ${horario.horarioId} $lugarActualizado")
                // Crear el objeto de actualización
                val horarioUpdate = HorarioUpdate(
                    horarioId = horario.horarioId!!,
                    lugar = lugarActualizado,
                    estado = true // Cambiar el estado a true
                )
                Log.d("aea", "aceptar3")

                Log.d("aea", horarioUpdate.toString())

                // TODO: Aquí puedes llamar a tu función para actualizar en la base de datos
                updateHorario(horarioUpdate)

                // Cerrar el diálogo
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun updateHorario(horarioUpdate: HorarioUpdate) {
        Log.d("aea", "entre a el uopdate")
        lifecycleScope.launch {
            try {
                val response = apiRest.updateHorario(horarioUpdate.horarioId, horarioUpdate)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Horario actualizado correctamente", Toast.LENGTH_SHORT).show()
                    fetchHorarios() // Recargar la lista de horarios}
                    Log.d("aea2", "si")
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.d("aea2", "no")
                }
            } catch (e: Exception) {
                Log.d("aea2", "red")
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
