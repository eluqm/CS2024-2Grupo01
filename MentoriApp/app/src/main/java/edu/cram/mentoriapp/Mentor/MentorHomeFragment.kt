package edu.cram.mentoriapp.Mentor

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Adapter.ChatAdapter
import edu.cram.mentoriapp.Model.Chat
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch

class MentorHomeFragment : Fragment(R.layout.fragment_mentor_home) {
    private var chats: MutableList<Chat> = mutableListOf()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var apiRest: ApiRest

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiRest = RetrofitClient.makeRetrofitClient()
        inicializarRecycle(view)
    }

    private fun inicializarRecycle(view: View) {
        loadSesionChats()  // Carga los mentoriados directamente con mentorId
        val manager = LinearLayoutManager(context)
        chatAdapter = ChatAdapter(chats) { chat -> onItemSelected(chat) }
        val decoration = DividerItemDecoration(context, manager.orientation)
        val sesionRecyclerView = view.findViewById<RecyclerView>(R.id.chat_grupal)
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