package edu.cram.mentoriapp.Psicologia

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
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
import edu.cram.mentoriapp.Adapter.ChatAdapter
import edu.cram.mentoriapp.Adapter.EventosAdapter
import edu.cram.mentoriapp.DAO.CommonDAO
import edu.cram.mentoriapp.DAO.PsicologiaDAO
import edu.cram.mentoriapp.Model.Chat
import edu.cram.mentoriapp.Model.Cities
import edu.cram.mentoriapp.Model.Evento
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch

class PiscoHomeFragment : Fragment(R.layout.fragment_pisco_home) {

    private var chats: MutableList<Chat> = mutableListOf()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var apiRest: ApiRest

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textView = view.findViewById<TextView>(R.id.tv_title)
        val cerrar_sesion = view.findViewById<ImageButton>(R.id.cerrar_sesion)
        val psicoDao = CommonDAO(requireContext())

        inicializarRecycle(view)

        cerrar_sesion.setOnClickListener(){
            cerrarSesion()
            view.findNavController().navigate(R.id.loginFragment, null)
        }

        // Llama a las funciones para interactuar con el API
        lifecycleScope.launch {
            // Ejemplo: Obtener una ciudad con ID 1 y mostrar los datos
            //psicoDao.fetchCity(1, textView)


        }

    }

    private fun inicializarRecycle(view: View) {
        //loadSesionChats()  // Carga los mentoriados directamente con mentorId
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
                val userId = sharedPreferences.getInt("userId", -1)

                if (userId != -1) {
                    val response = apiRest.getMensajesPorUsuario(userId)  // Llamada a la nueva API

                    if (response.isSuccessful) {
                        val chatsLlegada = response.body()
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

    private fun cerrarSesion() {
        val sharedPreferences = requireContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // Limpiar todos los datos guardados
        editor.apply() // Aplicar los cambios

        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }



}