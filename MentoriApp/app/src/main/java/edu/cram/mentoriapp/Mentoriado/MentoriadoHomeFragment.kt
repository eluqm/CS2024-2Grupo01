package edu.cram.mentoriapp.Mentoriado

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.cram.mentoriapp.Adapter.ChatAdapter
import edu.cram.mentoriapp.Adapter.EventosAdapter
import edu.cram.mentoriapp.MainActivity
import edu.cram.mentoriapp.Model.Chat
import edu.cram.mentoriapp.Model.Evento
import edu.cram.mentoriapp.Model.Horario
import edu.cram.mentoriapp.Model.MensajeGrupo
import edu.cram.mentoriapp.Model.MentorRead
import edu.cram.mentoriapp.Psicologia.PsicoActivity
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch

class MentoriadoHomeFragment : Fragment(R.layout.fragment_mentoriado_home) {
    private var chats: MutableList<Chat> = mutableListOf()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var apiRest: ApiRest
    private lateinit var sesionRecyclerView: RecyclerView
    private lateinit var horarioGrupo: Horario
    private lateinit var mentorGrupo: MentorRead

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiRest = RetrofitClient.makeRetrofitClient()

        pintarDatos(view)

        inicializarRecycle(view)

        iniciar_eventos(view)
    }

    private fun obtenerDatosMentor(view: View) {
        val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
        val grupoId = sharedPreferences.getInt("grupoId", -1)

        val nombreMentor = view.findViewById<TextView>(R.id.nombre_mentor)
        val correoMentor = view.findViewById<TextView>(R.id.correo_mentor)
        val numeroMentor = view.findViewById<TextView>(R.id.numero_mentor)

        if (grupoId != -1) {
            // Realizamos la llamada a la API de forma asincrónica usando lifecycleScope
            lifecycleScope.launch {
                try {
                    val response = apiRest.getMentorByGroupId(grupoId)

                    if (response.isSuccessful) {
                        // Asignamos el horario recibido a la variable
                        mentorGrupo = response.body() ?: throw Exception("Horario no encontrado")

                        // Manejo de éxito
                        Toast.makeText(context, "Mentor obtenido con éxito", Toast.LENGTH_SHORT).show()

                        nombreMentor.text = mentorGrupo.nombreCompleto
                        correoMentor.text = mentorGrupo.correo
                        numeroMentor.text = mentorGrupo.celularUsuario


                    } else {
                        Toast.makeText(context, "Error al obtener el Mentor", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    // Manejo de excepciones
                    Toast.makeText(context, "Ocurrió un error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "No se pudo obtener el grupoId", Toast.LENGTH_SHORT).show()
        }


    }

    private fun obtenerHorarioGrupo(view: View) {
        val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
        val grupoId = sharedPreferences.getInt("grupoId", -1)

        val Horario = view.findViewById<LinearLayout>(R.id.linearLayoutHorario)
        val cardEstado = view.findViewById<androidx.cardview.widget.CardView>(R.id.estadoCard)

        val txtLugar = view.findViewById<TextView>(R.id.tv_lugar)
        val txtDiaHora = view.findViewById<TextView>(R.id.tv_dia_hora)

        if (grupoId != -1) {
            // Realizamos la llamada a la API de forma asincrónica usando lifecycleScope
            lifecycleScope.launch {
                try {
                    val response = apiRest.getHorarioByGrupo(grupoId)

                    if (response.isSuccessful) {
                        // Asignamos el horario recibido a la variable
                        horarioGrupo = response.body() ?: throw Exception("Horario no encontrado")

                        // Manejo de éxito
                        Toast.makeText(context, "Horario obtenido con éxito", Toast.LENGTH_SHORT).show()

                        val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()

                        editor.putString("horaProgramada", horarioGrupo.horaInicio + ":00")
                        editor.putString("diaProgramado", horarioGrupo.dia)

                        editor.apply()

                        // Actualizar la UI si es necesario
                        // Ejemplo: mostrar el lugar del horario en algún TextView
                        // txtLugar.text = horarioGrupo.lugar
                        if (horarioGrupo.estado == true) {
                            Horario.visibility = View.VISIBLE
                            txtLugar.text = "Lugar: " + horarioGrupo.lugar
                            txtDiaHora.text = "Dia y Hora: " + horarioGrupo.dia + ", " + horarioGrupo.horaInicio
                        } else {
                            Horario.visibility = View.GONE
                            cardEstado.visibility = View.VISIBLE
                        }


                    } else {
                        Horario.visibility = View.GONE
                        cardEstado.visibility = View.VISIBLE
                        cardEstado.findViewById<TextView>(R.id.estadoText).text = "Aún no se propuso un horario"
                        // Manejo de error si la respuesta no es exitosa
                        //Toast.makeText(context, "Error al obtener el horario", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    // Manejo de excepciones
                    Toast.makeText(context, "Ocurrió un error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {

            Toast.makeText(context, "No se pudo obtener el grupoId", Toast.LENGTH_SHORT).show()
        }
    }


    private fun iniciar_eventos(view: View) {
        val recargarFloating = view.findViewById<FloatingActionButton>(R.id.btn_update_chat)

        val btnEnviar = view.findViewById<ImageButton>(R.id.btn_send_message)
        val txtMensaje = view.findViewById<EditText>(R.id.et_chat_message)

        val btnCerrarsesion = view.findViewById<ImageButton>(R.id.cerrar_sesion)

        val btnMostrarEventos = view.findViewById<ImageButton>(R.id.btn_notification)

        btnMostrarEventos.setOnClickListener {
            view.findNavController().navigate(R.id.action_mentoriadoHomeFragment_to_mostrarEventosFragment)
        }

        btnCerrarsesion.setOnClickListener {
            // Crear el diálogo
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí") { _, _ ->
                    // Acción para cerrar sesión
                    val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()

                    // Navegar a la actividad principal
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish() // Limpia la pila manualmente
                }
                .setNegativeButton("Cancelar", null) // No hacer nada si cancela
                .create()
                .show()
        }

        recargarFloating.setOnClickListener {
            // Simula la recarga de datos (consulta al servidor)
            loadSesionChats()
        }

        btnEnviar.setOnClickListener {
            val mensaje = txtMensaje.text.toString()

            if (mensaje.isNotEmpty()) {
                // Recuperamos el grupoId desde SharedPreferences
                val grupoId = obtenerGrupoId()

                // Verificamos si el grupoId es válido
                if (grupoId != null) {
                    // Crear el objeto MensajeGrupo con los datos requeridos
                    val mensajeGrupo = obtenerUsuarioId()?.let { it1 ->
                        MensajeGrupo(
                            grupoId = grupoId,
                            remitenteId = it1,  // Asegúrate de obtener el remitenteId desde SharedPreferences o la sesión
                            textoMensaje = mensaje
                        )
                    }

                    // Enviar el mensaje al servidor
                    lifecycleScope.launch {
                        val response = mensajeGrupo?.let { it1 -> apiRest.createMensajeGrupo(it1) }

                        if (response != null) {
                            if (response.isSuccessful) {
                                // Manejo de éxito
                                Toast.makeText(context, "Mensaje enviado", Toast.LENGTH_SHORT).show()
                                loadSesionChats()
                                txtMensaje.setText("")
                            } else {
                                // Manejo de error
                                Toast.makeText(context, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    // Si no se pudo obtener el grupoId
                    Toast.makeText(context, "Grupo no encontrado", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "El mensaje no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }



    }


    private fun obtenerUsuarioId(): Int? {
        val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
        return sharedPreferences.getInt("userId", -1).takeIf { it != -1 }
    }


    private fun obtenerGrupoId(): Int? {
        val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
        return sharedPreferences.getInt("grupoId", -1).takeIf { it != -1 }
    }

    private fun pintarDatos(view: View){
        obtenerDatosMentor(view)
        obtenerHorarioGrupo(view)

        val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)

        val tvRolUsuario = view.findViewById<TextView>(R.id.tv_rol_usuario)
        val tvNombreUsuario = view.findViewById<TextView>(R.id.tv_nombre_usuario)

        tvRolUsuario.text = sharedPreferences.getString("tipoUsuario", "Sin Tipo")
        tvNombreUsuario.text = "${sharedPreferences.getString("nombreUsuario", "Sin Nombre")} ${sharedPreferences.getString("apellidoUsuario", "Sin Apellido")}"
    }


    private fun inicializarRecycle(view: View) {
        loadSesionChats()  // Carga los mentoriados directamente con mentorId
        val manager = LinearLayoutManager(context)
        chatAdapter = ChatAdapter(chats) { chat -> onItemSelected(chat) }
        val decoration = DividerItemDecoration(context, manager.orientation)
        sesionRecyclerView = view.findViewById<RecyclerView>(R.id.chat_grupal)
        sesionRecyclerView.layoutManager = manager
        sesionRecyclerView.adapter = chatAdapter
        sesionRecyclerView.addItemDecoration(decoration)
    }


    private fun loadSesionChats() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Obtener el userId desde las SharedPreferences (sesión)
                val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
                val userId = sharedPreferences.getInt("userId", 7)

                if (userId != -1) {
                    val response = apiRest.getMensajesPorUsuario(userId)  // Llamada a la nueva API

                    if (response.isSuccessful) {
                        val chatsLlegada = response.body()
                        Log.d("loadChats", "Chats recibidos: $chatsLlegada")
                        if (chatsLlegada != null && chatsLlegada.isNotEmpty()) {
                            chats.clear()  // Asegúrate de que el adapter sea el correcto
                            chats.addAll(chatsLlegada)
                            chatAdapter.notifyDataSetChanged()  // Asegúrate de que el adapter sea el correcto
                            sesionRecyclerView.scrollToPosition(chatsLlegada.size - 1);
                        } else {
                            Toast.makeText(requireContext(), "No hay mensajes disponibles", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Cuerpo de error vacío"
                        Toast.makeText(requireContext(), "No hay mensajes", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "User ID no encontrado en SharedPreferences", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Manejo de excepciones (errores de red, etc.)
                Toast.makeText(requireContext(), "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("loadChats", "Error de red: ${e.message}")
            }
        }
    }


    private fun onItemSelected(chat: Chat) {
        chat.emisor.let { Toast.makeText(requireActivity(), it, Toast.LENGTH_SHORT).show() }
    }
}