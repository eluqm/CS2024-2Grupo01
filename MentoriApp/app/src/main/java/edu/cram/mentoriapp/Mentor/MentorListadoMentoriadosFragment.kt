import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.UsuarioLista
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch

class MentorListadoMentoriadosFragment : Fragment(R.layout.fragment_listado_mentoriados) {

    private lateinit var mentoriadoAdapter: MentoriadoAdapter
    private lateinit var mentorId: String // ID del mentor logueado

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el mentorId desde las SharedPreferences (sesión)
        val sharedPreferences = requireContext().getSharedPreferences("usuarioSesion", Context.MODE_PRIVATE)
        mentorId = sharedPreferences.getString("userId", "") ?: ""

        initRecyclerView(view)
        loadUsuariosMentoriados()  // Carga los mentoriados directamente con mentorId
    }

    private fun initRecyclerView(view: View) {
        val manager = LinearLayoutManager(context)
        mentoriadoAdapter = MentoriadoAdapter(
            mutableListOf(),
            onItemSelected = { usuario -> onItemSelected(usuario) },
            onDeleteUsuario = { usuario -> onDeleteUsuario(usuario) }
        )
        val decoration = DividerItemDecoration(context, manager.orientation)
        val mentoriadoRecyclerView = view.findViewById<RecyclerView>(R.id.mentoriadoRecyclerView)
        mentoriadoRecyclerView.layoutManager = manager
        mentoriadoRecyclerView.adapter = mentoriadoAdapter
        mentoriadoRecyclerView.addItemDecoration(decoration)
    }

    private fun loadUsuariosMentoriados() {
        // Llamada a la API en un hilo separado usando coroutines
        lifecycleScope.launch {
            try {
                // Obtener la instancia del servicio API
                val apiService = RetrofitClient.makeRetrofitClient()

                // Llamada a la API para obtener los usuarios mentoriados usando solo el mentorId
                val response: List<UsuarioLista> = apiService.getUsuariosMentoriadosPorMentor(mentorId)
                Toast.makeText(context, "Usuarios mentoriados cargados exitosamente ${mentorId}", Toast.LENGTH_SHORT).show()

                // Actualizar el adapter con la lista de usuarios
                mentoriadoAdapter.updateUsuarios(response.toMutableList())
            } catch (e: Exception) {
                // Mostrar mensaje de error si la API falla
                Toast.makeText(context, "Error al cargar usuarios: ${e.message}   ${mentorId}", Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun onItemSelected(usuario: UsuarioLista) {
        // Aquí puedes manejar el evento de selección, como navegar a los detalles del usuario
        Toast.makeText(context, "Seleccionaste: ${usuario.nombreCompletoUsuario}", Toast.LENGTH_SHORT).show()
    }


    private fun onDeleteUsuario(usuario: UsuarioLista) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirmación")
            .setMessage("¿Está seguro de que desea eliminar a este mentoriado?")
            .setPositiveButton("Sí") { _, _ -> deleteUsuarioFromDatabase(usuario) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteUsuarioFromDatabase(usuario: UsuarioLista) {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.makeRetrofitClient()
                val response = usuario.id.let { apiService.deleteMiembroGrupo(it) }

                if (response != null) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Mentoriado eliminado exitosamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error al eliminar mentoriado: ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al eliminar mentoriado: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
