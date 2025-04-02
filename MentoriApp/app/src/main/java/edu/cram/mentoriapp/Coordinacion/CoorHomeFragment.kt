package edu.cram.mentoriapp.Coordinacion

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import edu.cram.mentoriapp.Adapter.ChatAdapter
import edu.cram.mentoriapp.Model.Chat
import edu.cram.mentoriapp.Model.MensajeGrupo
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.cram.mentoriapp.MainActivity
import kotlinx.coroutines.launch

class CoorHomeFragment : Fragment(R.layout.fragment_coor_home) {
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

        val btnCerrarsesion = view.findViewById<ImageButton>(R.id.cerrar_sesion)

        val btnMostrarEventos = view.findViewById<ImageButton>(R.id.btn_notification)

        val actividad = requireActivity() as CoorActivity
        // Funciones que se llaman en cada caso
        fun onKeyboardShown() {
            actividad.bottomNav.visibility = View.GONE
        }

        fun onKeyboardHidden() {
            actividad.bottomNav.visibility = View.VISIBLE
            // Acción cuando el teclado se oculta
        }

        txtMensaje.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            txtMensaje.rootView.getWindowVisibleDisplayFrame(rect)

            val screenHeight = txtMensaje.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                // Teclado visible
                onKeyboardShown()
            } else {
                // Teclado oculto
                onKeyboardHidden()
            }
        }

        btnMostrarEventos.setOnClickListener {
            view.findNavController().navigate(R.id.action_coorHomeFragment_to_mostrarEventosFragment4)
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

        val recargarFloating = view.findViewById<FloatingActionButton>(R.id.btn_update_chat)
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
}