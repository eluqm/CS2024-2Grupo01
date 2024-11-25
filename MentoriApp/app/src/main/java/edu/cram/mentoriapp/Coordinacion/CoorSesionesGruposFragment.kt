package edu.cram.mentoriapp.Coordinacion

import SesionesAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.GrupoMentoria
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch

class CoorSesionesGruposFragment : Fragment(R.layout.fragment_coor_sesiones_grupos) {

    private lateinit var apiRest: ApiRest
    private lateinit var sesionesAdapter: SesionesAdapter
    private var gruposMentoria: MutableList<GrupoMentoria> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiRest = RetrofitClient.makeRetrofitClient()

        initRecycleView(view)
    }

    private fun initRecycleView(view: View) {
        loadGrupos()
        val manager = LinearLayoutManager(context)
        Toast.makeText(requireContext(), "Antes del recicler", Toast.LENGTH_SHORT).show()
        sesionesAdapter = SesionesAdapter(gruposMentoria) { user -> onItemSelected(user) }
        val decoration = DividerItemDecoration(context, manager.orientation)
        val usersRecyler = view.findViewById<RecyclerView>(R.id.recycler_grupos)
        usersRecyler.layoutManager = manager
        usersRecyler.adapter = sesionesAdapter
        usersRecyler.addItemDecoration(decoration)
        Toast.makeText(requireContext(), "Despues del recicler", Toast.LENGTH_SHORT).show()

    }

    private fun loadGrupos() {
        Log.d("loadGrupos", "loadGrupos() iniciado")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
                val escuelaId = sharedPreferences.getInt("escuelaId", -1)
                if (escuelaId != -1) {
                    val response = apiRest.getGrupoByEscuela(escuelaId)
                    if (response.isSuccessful) {
                        val grupos = response.body()
                        if (grupos != null && grupos.isNotEmpty()) {
                            gruposMentoria.clear()
                            gruposMentoria.addAll(grupos)
                            sesionesAdapter.notifyDataSetChanged()
                            Toast.makeText(requireContext(), "Grupos obtenidos: ${gruposMentoria.toString()}", Toast.LENGTH_LONG).show()
                            Log.d("loadGrupos", "Se obtuvieron grupos para la escuelaId $escuelaId: ${gruposMentoria.toString()}")

                        } else {
                            Toast.makeText(requireContext(), "No hay grupos disponibles para esta escuela", Toast.LENGTH_SHORT).show()
                            Log.d("loadGrupos", "No hay grupos disponibles para la escuelaId $escuelaId.")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Cuerpo de error vacío"
                        Toast.makeText(requireContext(), "Error al cargar grupos: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                        Log.d("loadGrupos", "Error en la respuesta de la API: ${response.code()} - $errorBody")
                    }
                } else {
                    Toast.makeText(requireContext(), "Escuela ID no encontrado en SharedPreferences", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("loadGrupos", "Error de red: ${e.message}")
            }
        }
    }

    private fun onItemSelected(user: GrupoMentoria) {
        TODO("Not yet implemented")
    }

}
