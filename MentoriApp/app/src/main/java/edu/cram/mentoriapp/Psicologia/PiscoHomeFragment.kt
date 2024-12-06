package edu.cram.mentoriapp.Psicologia

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.cram.mentoriapp.Adapter.ChatAdapter
import edu.cram.mentoriapp.Adapter.EventosAdapter
import edu.cram.mentoriapp.DAO.CommonDAO
import edu.cram.mentoriapp.DAO.PsicologiaDAO
import edu.cram.mentoriapp.MainActivity
import edu.cram.mentoriapp.Model.Chat
import edu.cram.mentoriapp.Model.Cities
import edu.cram.mentoriapp.Model.Evento
import edu.cram.mentoriapp.Model.MensajeGrupo
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch

class PiscoHomeFragment : Fragment(R.layout.fragment_pisco_home) {

    private var chats: MutableList<Chat> = mutableListOf()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var apiRest: ApiRest
    private lateinit var sesionRecyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiRest = RetrofitClient.makeRetrofitClient()

        pintarDatos(view)

        inicializarRecycle(view)

        iniciar_eventos(view)

    }

    private fun iniciar_eventos(view: View) {

        val btnEnviar = view.findViewById<ImageButton>(R.id.btn_send_message)
        val txtMensaje = view.findViewById<EditText>(R.id.et_chat_message)
        val recargarFloating = view.findViewById<FloatingActionButton>(R.id.btn_update_chat)


        val btnCerrarsesion = view.findViewById<ImageButton>(R.id.cerrar_sesion)

        val btnMostrarEventos = view.findViewById<ImageButton>(R.id.btn_notification)

        btnMostrarEventos.setOnClickListener {
            view.findNavController().navigate(R.id.action_piscoHomeFragment_to_mostrarEventosFragment2)
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
                        Toast.makeText(requireContext(), "Error al cargar mensajes: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
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

    private fun cerrarSesion() {
        val sharedPreferences = requireContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // Limpiar todos los datos guardados
        editor.apply() // Aplicar los cambios

        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }



}