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
import edu.cram.mentoriapp.Adapter.SesionListaAdapter
import edu.cram.mentoriapp.Model.SesionMentoriaLista
import edu.cram.mentoriapp.Model.UsuarioLista
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch

class MentorListadoSesionesFragment : Fragment(R.layout.fragment_listado_sesiones) {

    private lateinit var sesionListaAdapter: SesionListaAdapter
    private lateinit var apiRest: ApiRest
    private var sesesionxGrupo: MutableList<SesionMentoriaLista> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiRest = RetrofitClient.makeRetrofitClient()

        initRecyclerView(view)
    }

    private fun initRecyclerView(view: View) {
        loadSesionMentoriados()  // Carga los mentoriados directamente con mentorId
        val manager = LinearLayoutManager(context)
        sesionListaAdapter = SesionListaAdapter(sesesionxGrupo) { sesion -> onItemSelected(sesion) }
        val decoration = DividerItemDecoration(context, manager.orientation)
        val sesionRecyclerView = view.findViewById<RecyclerView>(R.id.sesionesRecyclerView)
        sesionRecyclerView.layoutManager = manager
        sesionRecyclerView.adapter = sesionListaAdapter
        sesionRecyclerView.addItemDecoration(decoration)
    }

    private fun loadSesionMentoriados() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Obtener el mentorId desde las SharedPreferences (sesión)
                val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
                val mentorId = sharedPreferences.getInt("userId", -1)

                if (mentorId != -1) {
                    val response = apiRest.getSesionesPorJefe(mentorId)

                    if (response.isSuccessful) {
                        val sesiones = response.body()
                        if (sesiones != null && sesiones.isNotEmpty()) {
                            sesesionxGrupo.clear()
                            sesesionxGrupo.addAll(sesiones)
                            sesionListaAdapter.notifyDataSetChanged()
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
    private fun onItemSelected(sesion: SesionMentoriaLista) {
        Toast.makeText(requireActivity(), sesion.temaSesion, Toast.LENGTH_SHORT).show()
    }
}