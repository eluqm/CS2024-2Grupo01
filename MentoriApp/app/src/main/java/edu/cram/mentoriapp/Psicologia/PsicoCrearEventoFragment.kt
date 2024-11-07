package edu.cram.mentoriapp.Psicologia

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Adapter.EventosAdapter
import edu.cram.mentoriapp.Adapter.SesionListaAdapter
import edu.cram.mentoriapp.DAO.CommonDAO
import edu.cram.mentoriapp.Model.Evento
import edu.cram.mentoriapp.Model.SesionMentoriaLista
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch

class PsicoCrearEventoFragment : Fragment(R.layout.fragment_psico_crear_evento) {

    private lateinit var commonDAO: CommonDAO
    private lateinit var eventosAdapter: EventosAdapter
    private lateinit var apiRest: ApiRest
    private var eventos: MutableList<Evento> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa CommonDAO
        commonDAO = CommonDAO(requireContext())

        // Encuentra el botón para crear evento
        view.findViewById<Button>(R.id.boton_crear_evento).setOnClickListener {
            // Mostrar el diálogo
            CrearEventoDialog(requireContext(), commonDAO).show(childFragmentManager, "CrearEventoDialog")
        }
        apiRest = RetrofitClient.makeRetrofitClient()
        initRecyclerView(view)
    }

    private fun initRecyclerView(view: View) {
        loadSesionMentoriados()  // Carga los mentoriados directamente con mentorId
        val manager = LinearLayoutManager(context)
        eventosAdapter = EventosAdapter(eventos) { evento -> onItemSelected(evento) }
        val decoration = DividerItemDecoration(context, manager.orientation)
        val sesionRecyclerView = view.findViewById<RecyclerView>(R.id.recycler_eventos)
        sesionRecyclerView.layoutManager = manager
        sesionRecyclerView.adapter = eventosAdapter
        sesionRecyclerView.addItemDecoration(decoration)
    }

    private fun loadSesionMentoriados() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Obtener el mentorId desde las SharedPreferences (sesión)
                val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
                val psicoId = sharedPreferences.getInt("userId", -1)

                if (psicoId != -1) {
                    val response = apiRest.getAllEventos()

                    if (response.isSuccessful) {
                        val sesiones = response.body()
                        if (sesiones != null && sesiones.isNotEmpty()) {
                            eventos.clear()
                            eventos.addAll(sesiones)
                            eventosAdapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(requireContext(), "No hay mentoriados disponibles", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Cuerpo de error vacío"
                        Toast.makeText(requireContext(), "Error al cargar mentoriados: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Mentor ID no encontrado en SharedPreferences", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Manejo de excepciones (errores de red, etc.)
                Toast.makeText(requireContext(), "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("loadMentoriados", "Error de red: ${e.message}")
            }
        }
    }
    private fun onItemSelected(evento: Evento) {
        evento.eventoId?.let { Toast.makeText(requireActivity(), it, Toast.LENGTH_SHORT).show() }
    }

}