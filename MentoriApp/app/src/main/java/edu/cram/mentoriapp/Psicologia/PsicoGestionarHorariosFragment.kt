package edu.cram.mentoriapp.Psicologia

import HorarioAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Adapter.OpcionesAdapter
import edu.cram.mentoriapp.Common.FCMToken
import edu.cram.mentoriapp.Common.NotificationRequest
import edu.cram.mentoriapp.Model.HorarioCell
import edu.cram.mentoriapp.Model.HorarioDetalles
import edu.cram.mentoriapp.Model.HorarioUpdate
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PsicoGestionarHorariosFragment : Fragment(R.layout.fragment_psico_gestionar_horarios) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var apiRest: ApiRest
    private val TAG = "NotificationSystem"

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

        // Evitar que el scroll del recyclerView sea interceptado por el parent
        recyclerView.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        // Cargar los horarios
        fetchHorarios()
    }

    private fun fetchHorarios() {
        viewLifecycleOwner.lifecycleScope.launch {
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

    @SuppressLint("MissingInflatedId")
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
            editTextLugar.setText(horarioDetalles.lugar ?: "")
            textViewMentor.text = "Mentor: ${horarioDetalles.nombreCompletoJefe}"
            textViewDia.text = "Día: ${horarioDetalles.dia}"
            textViewHoraInicio.text = "Hora de inicio: ${horarioDetalles.horaInicio}"
            textViewHoraFin.text = "Hora de fin: ${horarioDetalles.horaFin.substring(0,5)}"
        }

        if (horarioDetalles is HorarioCell) {
            if (horarioDetalles.estado) {
                // Si el horario ya está confirmado, solo mostrar información
                editTextLugar.isEnabled = false
                AlertDialog.Builder(requireContext())
                    .setTitle("Detalles del Horario")
                    .setView(dialogView)
                    .setNegativeButton("Okey") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            } else {
                // Si el horario está pendiente, permitir confirmar
                AlertDialog.Builder(requireContext())
                    .setTitle("Detalles del Horario")
                    .setView(dialogView)
                    .setPositiveButton("Confirmar") { dialog, _ ->
                        val lugarActualizado = editTextLugar.text.toString()
                        if (horarioDetalles.horarioId != null) {
                            confirmarYNotificarHorario(
                                horarioId = horarioDetalles.horarioId,
                                lugar = lugarActualizado
                            )
                        } else {
                            Toast.makeText(requireContext(), "Horario ID no disponible", Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }
        } else if (horarioDetalles is HorarioDetalles) {
            if (horarioDetalles.estado) {
                // Modo visualización (horario ya aceptado)
                editTextLugar.isEnabled = false
                AlertDialog.Builder(requireContext())
                    .setTitle("Detalles del Horario")
                    .setView(dialogView)
                    .setNegativeButton("Ok") { dialog, _ -> dialog.dismiss() }
                    .show()
            } else {
                // Modo edición (pendiente de aceptación)
                AlertDialog.Builder(requireContext())
                    .setTitle("Detalles del Horario")
                    .setView(dialogView)
                    .setPositiveButton("Confirmar") { dialog, _ ->
                        val lugarActualizado = editTextLugar.text.toString().takeIf { it.isNotBlank() }
                            ?: run {
                                Toast.makeText(requireContext(), "Ingrese un lugar válido", Toast.LENGTH_SHORT).show()
                                return@setPositiveButton
                            }

                        if (horarioDetalles.horarioId == null) {
                            Toast.makeText(requireContext(), "ID de horario no disponible", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        confirmarYNotificarHorario(
                            horarioId = horarioDetalles.horarioId!!,
                            lugar = lugarActualizado
                        )
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }
    }

    // Función principal para confirmar horario y enviar notificaciones
    private fun confirmarYNotificarHorario(horarioId: Int, lugar: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val loadingDialog = showLoadingDialog("Confirmando horario...")
            Log.d(TAG, "Iniciando proceso de confirmación de horario #$horarioId con lugar: $lugar")

            try {
                // 1. PRIMERO: Actualizar horario en la base de datos
                val horarioUpdate = HorarioUpdate(
                    horarioId = horarioId,
                    lugar = lugar,
                    estado = true
                )

                val updateResult = withContext(Dispatchers.IO) {
                    apiRest.updateHorario(horarioId, horarioUpdate)
                }

                if (!updateResult.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Error al actualizar horario: Código ${updateResult.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadingDialog.dismiss()
                    }
                    return@launch
                }

                Log.d(TAG, "✅ Horario #$horarioId actualizado exitosamente")

                // 2. SEGUNDO: Obtener tokens de forma paralela
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Obteniendo destinatarios para notificaciones...", Toast.LENGTH_SHORT).show()
                }

                val mentoresDeferred = async(Dispatchers.IO) {
                    apiRest.getTokensByHorario(horarioId)
                }

                val mentoriadosDeferred = async(Dispatchers.IO) {
                    apiRest.getTokensByGrupoHorario(horarioId)
                }

                val mentoresResponse = mentoresDeferred.await()
                val mentoriadosResponse = mentoriadosDeferred.await()

                val allTokens = mutableSetOf<FCMToken>()

                // Procesar tokens de mentores
                if (mentoresResponse.isSuccessful) {
                    mentoresResponse.body()?.let { tokens ->
                        allTokens.addAll(tokens)
                        Log.d(TAG, "✅ Obtenidos ${tokens.size} tokens de mentores")
                    }
                } else {
                    Log.e(TAG, "❌ Error al obtener tokens de mentores: ${mentoresResponse.code()}")
                }

                // Procesar tokens de mentoriados
                if (mentoriadosResponse.isSuccessful) {
                    mentoriadosResponse.body()?.let { tokens ->
                        allTokens.addAll(tokens)
                        Log.d(TAG, "✅ Obtenidos ${tokens.size} tokens de mentoriados")
                    }
                } else {
                    Log.e(TAG, "❌ Error al obtener tokens de mentoriados: ${mentoriadosResponse.code()}")
                }

                // 3. TERCERO: Enviar notificaciones
                if (allTokens.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(),
                            "Enviando ${allTokens.size} notificaciones...",
                            Toast.LENGTH_SHORT).show()
                    }

                    val notificationResults = sendNotificationsToAllTokens(allTokens, lugar)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(),
                            "Notificaciones: ${notificationResults.success} exitosas, ${notificationResults.failed} fallidas",
                            Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(),
                            "No se encontraron dispositivos para notificar",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                // 4. CUARTO: Actualizar UI
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(),
                        "✅ Horario confirmado exitosamente",
                        Toast.LENGTH_SHORT).show()
                    fetchHorarios() // Actualizar la vista con los cambios
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en el proceso de confirmación: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "❌ Error: ${e.message ?: "Error desconocido"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                loadingDialog.dismiss()
            }
        }
    }

    // Clase para almacenar resultados de notificaciones
    private data class NotificationResults(
        val success: Int = 0,
        val failed: Int = 0
    )

    // Función para mostrar diálogo de carga
    private fun showLoadingDialog(message: String = "Procesando..."): AlertDialog {
        return AlertDialog.Builder(requireContext()).apply {
            setView(ProgressBar(requireContext()).apply {
                isIndeterminate = true
            })
            setMessage(message)
            setCancelable(false)
        }.show()
    }

    // Función optimizada para enviar notificaciones con seguimiento detallado
    private suspend fun sendNotificationsToAllTokens(tokens: Set<FCMToken>, lugar: String): NotificationResults = withContext(Dispatchers.IO) {
        var exitosas = 0
        var fallidas = 0

        // Preparar la notificación
        val notificationTitle = "✅ Horario Confirmado"
        val notificationBody = "El horario en $lugar ha sido confirmado"

        tokens.forEachIndexed { index, token ->
            try {
                val notificationRequest = NotificationRequest(
                    token = token.fcmToken,
                    title = notificationTitle,
                    body = notificationBody
                )

                val response = apiRest.sendNotification(notificationRequest)

                if (response.isSuccessful) {
                    exitosas++
                    Log.d(TAG, "✅ Notificación ${index + 1}/${tokens.size} enviada exitosamente")
                } else {
                    fallidas++
                    Log.e(TAG, "❌ Error al enviar notificación ${index + 1}/${tokens.size}: Código ${response.code()}")
                }

                // Mostrar progreso periódicamente
                if ((index + 1) % 5 == 0 || index == tokens.size - 1) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Enviando notificaciones: ${index + 1}/${tokens.size}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                fallidas++
                Log.e(TAG, "❌ Excepción al enviar notificación: ${e.message}")
            }
        }

        // Log resumen final
        Log.d(TAG, "📊 Resumen de notificaciones - Total: ${tokens.size}, ✅ Exitosas: $exitosas, ❌ Fallidas: $fallidas")

        return@withContext NotificationResults(exitosas, fallidas)
    }

    private fun updateHorario(horarioUpdate: HorarioUpdate) {
        viewLifecycleOwner.lifecycleScope.launch {
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