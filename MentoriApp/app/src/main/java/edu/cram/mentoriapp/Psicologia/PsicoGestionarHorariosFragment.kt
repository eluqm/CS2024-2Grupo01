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


    // Función principal
    private fun confirmarYNotificarHorario(horarioId: Int, lugar: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val loadingDialog = showLoadingDialog("Confirmando horario...")
            Log.d(TAG, "Iniciando proceso de confirmación de horario #$horarioId con lugar: $lugar")

            try {
                // 1. ACTUALIZAR HORARIO (ESPERAR COMPLETACIÓN)
                val updateResponse = apiRest.updateHorario(
                    id = horarioId,
                    horario = HorarioUpdate(
                        horarioId = horarioId,
                        lugar = lugar,
                        estado = true
                    )
                )

                if (updateResponse.isSuccessful) {
                    Log.d(TAG, "✅ Horario #$horarioId actualizado exitosamente")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "✅ Horario actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "❌ Error al actualizar horario #$horarioId: Código ${updateResponse.code()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "❌ Error al actualizar horario: Código ${updateResponse.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadingDialog.dismiss()
                        return@withContext
                    }
                    return@launch
                }

                // 2. OBTENER TOKENS (PARALELO)
                Log.d(TAG, "Obteniendo tokens FCM para el horario #$horarioId...")
                val tokenMentoresResponse = async { apiRest.getTokensByHorario(horarioId) }
                val tokenMentoriadosResponse = async { apiRest.getTokensByGrupoHorario(horarioId) }

                val tokensMentoresResult = tokenMentoresResponse.await()
                val tokensMentoriadosResult = tokenMentoriadosResponse.await()

                // Verificar respuestas de tokens
                if (!tokensMentoresResult.isSuccessful) {
                    Log.e(TAG, "❌ Error al obtener tokens de mentores: Código ${tokensMentoresResult.code()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(),
                            "❌ Error al obtener tokens de mentores: ${tokensMentoresResult.code()}",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                if (!tokensMentoriadosResult.isSuccessful) {
                    Log.e(TAG, "❌ Error al obtener tokens de mentoriados: Código ${tokensMentoriadosResult.code()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(),
                            "❌ Error al obtener tokens de mentoriados: ${tokensMentoriadosResult.code()}",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                // 3. FILTRAR TOKENS VÁLIDOS
                val tokensMentores = tokensMentoresResult.body() ?: emptyList()
                val tokensMentoriados = tokensMentoriadosResult.body() ?: emptyList()

                Log.d(TAG, "📱 Tokens obtenidos - Mentores: ${tokensMentores.size}, Mentoriados: ${tokensMentoriados.size}")

                val allTokens = mutableSetOf<FCMToken>().apply {
                    addAll(tokensMentores)
                    addAll(tokensMentoriados)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "📱 Tokens encontrados: ${allTokens.size} (${tokensMentores.size} mentores, ${tokensMentoriados.size} mentoriados)",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // 4. ENVIAR NOTIFICACIONES
                if (allTokens.isNotEmpty()) {
                    Log.d(TAG, "🔔 Iniciando envío de ${allTokens.size} notificaciones...")
                    val notificationResults = sendNotificationsToAllTokens(allTokens, lugar)

                    withContext(Dispatchers.Main) {
                        val exitosas = notificationResults.success
                        val fallidas = notificationResults.failed

                        if (exitosas > 0) {
                            Toast.makeText(
                                requireContext(),
                                "✅ $exitosas notificaciones enviadas exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        if (fallidas > 0) {
                            Toast.makeText(
                                requireContext(),
                                "⚠️ $fallidas notificaciones fallaron. Revisa el log para más detalles",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Log.w(TAG, "⚠️ No se encontraron tokens registrados para enviar notificaciones")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "⚠️ No se encontraron dispositivos para notificar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // 5. MOSTRAR RESULTADO FINAL
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "✅ Proceso de confirmación de horario completado",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // 6. Actualizar UI
                fetchHorarios()

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

    // Función mejorada para enviar notificaciones con seguimiento detallado
    private suspend fun sendNotificationsToAllTokens(tokens: Set<FCMToken>, lugar: String): NotificationResults {
        var exitosas = 0
        var fallidas = 0

        tokens.forEachIndexed { index, token ->
            try {
                Log.d(TAG, "🔔 Enviando notificación ${index + 1}/${tokens.size} a token: ${token.fcmToken.take(10)}...")

                val response = apiRest.sendNotification(
                    NotificationRequest(
                        token = token.fcmToken,
                        title = "✅ Horario Confirmado",
                        body = "El horario en $lugar ha sido aceptado"
                    )
                )

                if (response.isSuccessful) {
                    exitosas++
                    Log.d(TAG, "✅ Notificación enviada exitosamente a token ${token.fcmToken.take(10)}...")
                } else {
                    fallidas++
                    Log.e(TAG, "❌ Error al enviar notificación a token ${token.fcmToken.take(10)}: Código ${response.code()}, Mensaje: ${response.message()}")

                    // Log del cuerpo de la respuesta si está disponible
                    response.errorBody()?.string()?.let { errorBody ->
                        Log.e(TAG, "Detalles del error: $errorBody")
                    }
                }
            } catch (e: Exception) {
                fallidas++
                Log.e(TAG, "❌ Excepción al enviar notificación a token ${token.fcmToken.take(10)}: ${e.message}", e)
            }

            // Actualizamos el progreso en la UI cada 5 notificaciones o al finalizar
            if ((index + 1) % 5 == 0 || index == tokens.size - 1) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "📤 Progreso notificaciones: ${index + 1}/${tokens.size} (✅$exitosas ❌$fallidas)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Log resumen final
        Log.d(TAG, "📊 Resumen de notificaciones - Total: ${tokens.size}, ✅ Exitosas: $exitosas, ❌ Fallidas: $fallidas")

        return NotificationResults(exitosas, fallidas)
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