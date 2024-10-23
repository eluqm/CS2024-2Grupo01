import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.GrupoMentoria
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch

class MentorListadoMentoriadosFragment : Fragment(R.layout.fragment_listado_mentoriados) {

    private lateinit var mentoriadoAdapter: MentoriadoAdapter
    private lateinit var mentorId: String // ID del mentor logueado
    private lateinit var grupoId: String  // ID del grupo asociado al mentor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el mentorId desde las SharedPreferences (sesión)
        val sharedPreferences = requireContext().getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE)
        mentorId = sharedPreferences.getString("mentorId", "") ?: ""

        //initRecyclerView(view)

        // Primero, obtén el grupoId y luego carga los usuarios mentoriados
        //loadGrupoId()
    }

    private fun initRecyclerView(view: View) {
        val manager = LinearLayoutManager(context)
        mentoriadoAdapter = MentoriadoAdapter(mutableListOf()) { usuario -> onItemSelected(usuario) }
        val decoration = DividerItemDecoration(context, manager.orientation)
        val mentoriadoRecyclerView = view.findViewById<RecyclerView>(R.id.mentoriadoRecyclerView)
        mentoriadoRecyclerView.layoutManager = manager
        mentoriadoRecyclerView.adapter = mentoriadoAdapter
        mentoriadoRecyclerView.addItemDecoration(decoration)
    }

    private fun loadGrupoId() {
        lifecycleScope.launch {
            try {
                // Obtener la instancia del servicio API
                val apiService = RetrofitClient.makeRetrofitClient()

                // Llamada a la API para obtener los grupos del mentor
                val grupos: List<GrupoMentoria> = apiService.getGruposPorMentor(mentorId)

                // Asumimos que quieres el primer grupo (ajusta según tu lógica)
                if (grupos.isNotEmpty()) {
                    grupoId = grupos[0].toString()  // O el campo correcto según tu data class
                    loadUsuariosMentoriados()  // Una vez que tienes el grupoId, carga los mentoriados
                } else {
                    Toast.makeText(context, "No se encontraron grupos para este mentor", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                // Manejar el error
                Toast.makeText(context, "Error al cargar grupos: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadUsuariosMentoriados() {
        // Hacer la llamada a la API en un hilo separado usando coroutines
        lifecycleScope.launch {
            try {
                // Obtener la instancia del servicio API
                val apiService = RetrofitClient.makeRetrofitClient()

                // Llamada a la API para obtener los usuarios de tipo mentoriado del grupo del mentor logueado
                val response: List<Usuario> = apiService.getUsuariosMentoriadosPorGrupo(mentorId, grupoId)

                // Actualizar el adapter con la lista de usuarios
                mentoriadoAdapter.updateUsuarios(response.toMutableList())
            } catch (e: Exception) {
                // Mostrar mensaje de error si la API falla
                Toast.makeText(context, "Error al cargar usuarios: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onItemSelected(usuario: Usuario) {
        // Aquí puedes manejar el evento de selección, como navegar a los detalles del usuario
        Toast.makeText(context, "Seleccionaste: ${usuario.nombreUsuario}", Toast.LENGTH_SHORT).show()
    }
}
