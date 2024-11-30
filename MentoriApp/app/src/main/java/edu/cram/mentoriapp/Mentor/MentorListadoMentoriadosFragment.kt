import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.UsuarioLista
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch

class MentorListadoMentoriadosFragment : Fragment(R.layout.fragment_listado_mentoriados) {

    private lateinit var mentoriadoAdapter: MentoriadoAdapter
    private lateinit var apiRest: ApiRest
    private var mentoreadosxGrupo: MutableList<UsuarioLista> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val buscador = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
        apiRest = RetrofitClient.makeRetrofitClient()

        initRecyclerView(view)

        buscador.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // No necesitamos manejar la acción de enviar
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filtrar la lista del adaptador
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query: String?) {
        if (query.isNullOrBlank()) {
            // Si no hay texto, mostrar todos los elementos
            mentoriadoAdapter.updateList(mentoreadosxGrupo)
        } else {
            val filteredList = mentoreadosxGrupo.filter {
                it.nombreCompletoUsuario.contains(query, ignoreCase = true)
            }
            mentoriadoAdapter.updateList(filteredList)
        }
    }

    private fun initRecyclerView(view: View) {
        loadUsuariosMentoriados()  // Carga los mentoriados directamente con mentorId
        val manager = LinearLayoutManager(context)
        mentoriadoAdapter = MentoriadoAdapter(mentoreadosxGrupo) { usuario -> onItemSelected(usuario) }
        val mentoriadoRecyclerView = view.findViewById<RecyclerView>(R.id.mentoriadoRecyclerView)
        mentoriadoRecyclerView.layoutManager = manager
        mentoriadoRecyclerView.adapter = mentoriadoAdapter
    }

    private fun loadUsuariosMentoriados() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Obtener el mentorId desde las SharedPreferences (sesión)
                val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
                val mentorId = sharedPreferences.getInt("userId", -1)

                if (mentorId != -1) {
                    val response = apiRest.getUsuariosMentoriadosPorMentor(mentorId)

                    if (response.isSuccessful) {
                        val mentoriados = response.body()
                        if (mentoriados != null && mentoriados.isNotEmpty()) {
                            mentoreadosxGrupo.clear()
                            mentoreadosxGrupo.addAll(mentoriados)
                            mentoriadoAdapter.notifyDataSetChanged()
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
    private fun onItemSelected(usuario: UsuarioLista) {
        Toast.makeText(requireActivity(), usuario.nombreCompletoUsuario, Toast.LENGTH_SHORT).show()
    }
}
