package edu.cram.mentoriapp.Psicologia

import HorarioAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
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
import edu.cram.mentoriapp.Adapter.OpcionesAdapter
import edu.cram.mentoriapp.Model.HorarioCell
import edu.cram.mentoriapp.Model.HorarioDetalles
import edu.cram.mentoriapp.Model.HorarioUpdate
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch

class PsicoGestionarHorariosFragment : Fragment(R.layout.fragment_psico_gestionar_horarios) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var apiRest: ApiRest

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiRest = RetrofitClient.makeRetrofitClient()
        recyclerView = view.findViewById(R.id.recyclerViewHorario)

        // Configura el RecyclerView con su LayoutManager
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 6)

        // Añade las decoraciones
        val decoration1 = DividerItemDecoration(requireContext(), GridLayoutManager.HORIZONTAL)
        val decoration2 = DividerItemDecoration(requireContext(), GridLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(decoration1)
        recyclerView.addItemDecoration(decoration2)

        // AQUÍ ES DONDE DEBE IR EL CÓDIGO
        recyclerView.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        // Y luego continúas con el resto del código
        fetchHorarios()
    }

    private fun fetchHorarios() {
        lifecycleScope.launch {
            try {
                val response = apiRest.getHorarios()
                if (response.isSuccessful) {
                    val horarios = response.body() ?: emptyList()
                    Log.d("hola", horarios.toString())
                    setupRecyclerView(horarios)
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView(horarios: List<HorarioDetalles>) {
        val horas = listOf("07:15", "08:00", "08:45", "09:30", "10:15", "11:00", "11:45", "12:30", "13:15", "14:00", "14:45", "15:30", "16:15", "17:00", "17:45", "18:30", "19:15", "20:00")
        val dias = listOf("Lunes", "Martes", "Miercoles", "Jueves", "Viernes")

        val celdas = mutableListOf<HorarioCell>()
        for (hora in horas) {
            celdas.add(HorarioCell("inicio", "dia", "fin", lugar = hora,1))

            for (dia in dias) {
                val eventos = horarios.filter { it.dia == dia && it.horaInicio.substring(0, 5) == hora }
                if (eventos.size > 1) {
                    // Celda conflictiva con múltiples eventos
                    celdas.add(HorarioCell(hora, dia, null, null, null, false, esConflicto = true, eventos = eventos))
                } else {
                    val evento = eventos.firstOrNull()
                    celdas.add(HorarioCell(hora, dia, evento?.horaFin, evento?.lugar, evento?.horarioId, evento?.estado ?: false, evento?.nombreGrupo, evento?.nombreCompletoJefe, evento?.nombreEscuela))
                }
            }
        }
        recyclerView.adapter = HorarioAdapter(celdas) { horario -> onItemSelected(horario) }
    }

    private fun onItemSelected(horarioCell: HorarioCell) {
        if (horarioCell.esConflicto) {
            // Si es un conflicto, mostrar la lista de opciones
            mostrarOpcionesConflicto(horarioCell.eventos)
        } else {
            // Si no es conflicto, mostrar los detalles del horario
            initDialogo(horarioCell)
        }
    }


    private fun mostrarOpcionesConflicto(eventos: List<HorarioDetalles>?) {
        if (eventos.isNullOrEmpty()) return

        val opcionesAdapter = OpcionesAdapter(requireContext(), eventos)

        // Crear el cuadro de diálogo
        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar un horario")
            .setAdapter(opcionesAdapter) { _, which ->
                val horarioSeleccionado = eventos[which]
                // Muestra el diálogo correspondiente al evento seleccionado
                initDialogo(horarioSeleccionado)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }



    @SuppressLint("MissingInflatedId", "SuspiciousIndentation")
    private fun initDialogo(horarioDetalles: Any) {
        // Inflar el layout del diálogo
        val dialogView = layoutInflater.inflate(R.layout.dialog_horario, null)
        val editTextLugar = dialogView.findViewById<EditText>(R.id.editTextLugar)
        val textViewMentor = dialogView.findViewById<TextView>(R.id.textViewMentor)
        val textViewDia = dialogView.findViewById<TextView>(R.id.textViewDia)
        val textViewHoraInicio = dialogView.findViewById<TextView>(R.id.textViewHoraInicio)
        val textViewHoraFin = dialogView.findViewById<TextView>(R.id.textViewHoraFin)

        // Si el objeto pasado es de tipo `HorarioCell`, tratamos de usar su información
        if (horarioDetalles is HorarioCell) {
            editTextLugar.setText(horarioDetalles.lugar)
            textViewMentor.text = "Mentor: ${horarioDetalles.nombreCompletoJefe}"
            textViewDia.text = "Día: ${horarioDetalles.dia}"
            textViewHoraInicio.text = "Hora de inicio: ${horarioDetalles.horaInicio}"
            textViewHoraFin.text = "Hora de fin: ${horarioDetalles.horaFin?.substring(0,5)}"
        }

        // Si el objeto pasado es de tipo `HorarioDetalles`, usamos sus valores
        else if (horarioDetalles is HorarioDetalles) {
            editTextLugar.setText("Lugar ${horarioDetalles.lugar}")
            textViewMentor.text = "Mentor: ${horarioDetalles.nombreCompletoJefe}"
            textViewDia.text = "Día: ${horarioDetalles.dia}"
            textViewHoraInicio.text = "Hora de inicio: ${horarioDetalles.horaInicio}"
            textViewHoraFin.text = "Hora de fin: ${horarioDetalles.horaFin.substring(0,5)}"
        }

        if(horarioDetalles is HorarioCell){
            if(horarioDetalles.estado){
                editTextLugar.isEnabled = false

                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Detalles del Horario")
                    .setView(dialogView)
                    .setNegativeButton("Okey") { dialog, _ -> dialog.dismiss() }
                    .create()

                dialog.show()
            }else{
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Detalles del Horario")
                    .setView(dialogView)
                    .setPositiveButton("Confirmar") { dialog, _ ->
                        // Aquí puedes actualizar el lugar o cualquier otro dato
                        val lugarActualizado = editTextLugar.text.toString()

                        // Verificamos si el horarioId es nulo antes de crear el objeto de actualización
                            // Si es un `HorarioCell`, actualizar con su id y lugar
                            if (horarioDetalles.horarioId != null) {
                                val horarioUpdate = HorarioUpdate(
                                    horarioId = horarioDetalles.horarioId,  // Debe tener un id válido
                                    lugar = lugarActualizado,
                                    estado = true
                                )
                                updateHorario(horarioUpdate)
                            } else {
                                // Manejo de caso cuando horarioId es nulo
                                Toast.makeText(requireContext(), "Horario ID no disponible", Toast.LENGTH_SHORT).show()
                            }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                    .create()

                dialog.show()
            }


        } else if (horarioDetalles is HorarioDetalles) {
            if (horarioDetalles.estado) {
                editTextLugar.isEnabled = false
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Detalles del Horario")
                    .setView(dialogView)
                    .setNegativeButton("Okey") { dialog, _ -> dialog.dismiss() }
                    .create()

                dialog.show()
            }else{
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Detalles del Horario")
                    .setView(dialogView)
                    .setPositiveButton("Confirmar") { dialog, _ ->
                        // Aquí puedes actualizar el lugar o cualquier otro dato
                        val lugarActualizado = editTextLugar.text.toString()

                        // Verificamos si el horarioId es nulo antes de crear el objeto de actualización
                        // Si es un `HorarioCell`, actualizar con su id y lugar
                        if (horarioDetalles.horarioId != null) {
                            val horarioUpdate = HorarioUpdate(
                                horarioId = horarioDetalles.horarioId!!,  // Debe tener un id válido
                                lugar = lugarActualizado,
                                estado = true
                            )
                            updateHorario(horarioUpdate)
                        } else {
                            // Manejo de caso cuando horarioId es nulo
                            Toast.makeText(requireContext(), "Horario ID no disponible", Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                    .create()

                dialog.show()
            }
        }
    }



    private fun updateHorario(horarioUpdate: HorarioUpdate) {
        lifecycleScope.launch {
            try {
                val response = apiRest.updateHorario(horarioUpdate.horarioId, horarioUpdate)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Horario actualizado correctamente", Toast.LENGTH_SHORT).show()
                    fetchHorarios()
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
