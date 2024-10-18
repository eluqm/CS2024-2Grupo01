package edu.cram.mentoriapp.Coordinacion

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import edu.cram.mentoriapp.Model.GrupoMentoria
import edu.cram.mentoriapp.Model.MiembroGrupo
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch

class CoorGruposFragment : Fragment(R.layout.fragment_coor_grupos) {

    private lateinit var mentores: List<Usuario> // Lista de escuelas a mostrar
    private lateinit var apiRest: ApiRest

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiRest = RetrofitClient.makeRetrofitClient()


        view.findViewById<Button>(R.id.boton_crear_grupo).setOnClickListener {
            loadMentores()
        }

    }

    private fun loadMentores() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Llamar a la API para obtener mentores por tipo
                val response = apiRest.getUsuariosByType("mentor")

                if (response.isSuccessful) {
                    mentores = response.body() ?: emptyList()

                    if (mentores.isNotEmpty()) {
                        // Mostrar el diálogo para seleccionar un mentor
                        showMentorSelectionDialog(mentores)
                    } else {
                        // Mensaje si no hay mentores disponibles
                        Toast.makeText(requireContext(), "No hay mentores disponibles", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Manejo de errores
                    val errorBody = response.errorBody()?.string() ?: "Cuerpo de error vacío"
                    Toast.makeText(requireContext(), "Error al cargar mentores: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                // Manejo de excepciones
                Toast.makeText(requireContext(), "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showMentorSelectionDialog(mentores: List<Usuario>) {
        val mentorNombres = mentores.map { it.nombreUsuario } // Asegúrate de que Usuario tenga esta propiedad

        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona un Mentor")
            .setItems(mentorNombres.toTypedArray()) { dialogInterface, which ->
                val mentorSeleccionado = mentores[which]
                // Lógica adicional después de seleccionar el mentor
                println("Mentor seleccionado: ${mentorSeleccionado.nombreUsuario}, Tipo: ${mentorSeleccionado.tipoUsuario}")
                showCreateGrupoMentoriaDialog(mentorSeleccionado)
                // Aquí puedes continuar con la lógica que necesites después de seleccionar un mentor
                // Por ejemplo, almacenar el ID del mentor seleccionado o hacer otra operación
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun showCreateGrupoMentoriaDialog(mentorSeleccionado: Usuario) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_create_grupo_mentoria, null)

        // Referencias a los campos del diálogo
        val editTextNombre = dialogView.findViewById<EditText>(R.id.editTextNombre)
        val editTextDescripcion = dialogView.findViewById<EditText>(R.id.editTextDescripcion)

        builder.setView(dialogView)
            .setTitle("Crear Grupo de Mentoría")
            .setPositiveButton("Crear") { dialog, which ->
                // Obtener el nombre y la descripción ingresados
                val nombre = editTextNombre.text.toString()
                val descripcion = editTextDescripcion.text.toString().takeIf { it.isNotEmpty() } // Descripción opcional

                // Crear el objeto GrupoMentoria
                val grupoMentoria = mentorSeleccionado.userId?.let {
                    GrupoMentoria(
                        jefeId = it, // Asegúrate de que Usuario tenga userId
                        nombre = nombre,
                        horarioId = 5, // ID del horario manualmente asignado
                        descripcion = descripcion
                    )
                }

                Log.d("XDDD",grupoMentoria.toString())

                // Pasar al siguiente diálogo (aún no implementado)
                if (grupoMentoria != null) {
                    loadMentoriados(mentorSeleccionado.escuelaId, grupoMentoria)
                }
            }
            .setNegativeButton("Cancelar") { dialog, which -> dialog.cancel() }
            .show()
    }

    private fun loadMentoriados(escuelaId: Int, grupoMentoria: GrupoMentoria) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiRest.findUsuariosByTypeAndSchool("mentoriado", escuelaId)
                if (response.isSuccessful) {
                    val mentoriados = response.body() ?: emptyList()
                    showMentoriadosSelectionDialog(mentoriados, grupoMentoria)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error vacío"
                    Log.e("Mentoriados", "Error: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                Log.e("Mentoriados", "Error de red: ${e.message}")
            }
        }
    }



    private fun showMentoriadosSelectionDialog(mentoriados: List<Usuario>, grupoMentoria: GrupoMentoria) {
        val selectedMentoriados = mentoriados.take(20).map { it.userId }.toMutableSet() // Auto-seleccionar los primeros 20
        val items = mentoriados.map { "${it.nombreUsuario} ${it.apellidoUsuario}" }.toTypedArray()

        // Crear un AlertDialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Seleccionar Mentoriados")
        builder.setMultiChoiceItems(items, selectedMentoriados.map { it in mentoriados.map { it.userId } }.toBooleanArray()) { dialog, which, isChecked ->
            val userId = mentoriados[which].userId
            if (isChecked) {
                selectedMentoriados.add(userId) // Agregar el ID a la selección
            } else {
                selectedMentoriados.remove(userId) // Eliminar el ID de la selección
            }
        }

        builder.setPositiveButton("Crear Grupo") { dialog, _ ->
            createGrupo(selectedMentoriados.toList() ,grupoMentoria) // Crear el grupo y luego las relaciones
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun createGrupo(selectedMentoriados: List<Int?>, grupoMentoria: GrupoMentoria) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Crear el grupo
                val grupoResponse = apiRest.createGrupo(grupoMentoria) // Asegúrate de que esta función esté bien definida
                if (grupoResponse.isSuccessful) {
                    val grupoId = grupoResponse.body() ?: throw Exception("Grupo ID es nulo") // Obtener ID del grupo creado

                    // Crear las relaciones para cada mentoriado seleccionado
                    for (user in selectedMentoriados) {
                        user?.let {
                            val miembroGrupo = MiembroGrupo(grupoId = grupoId, userId = it)
                            val miembroResponse = apiRest.createMiembroGrupo(miembroGrupo)

                            if (miembroResponse.isSuccessful) {
                                // Manejar éxito de la relación creada
                                Log.d("Grupo", "Miembro agregado con ID: ${miembroResponse.body()}")
                            } else {
                                // Manejar error al crear la relación
                                val errorBody = miembroResponse.errorBody()?.string() ?: "Error vacío"
                                Log.e("Grupo", "Error al agregar miembro: ${miembroResponse.code()} - $errorBody")
                            }
                        } ?: Log.e("Grupo", "Usuario es nulo")
                    }
                } else {
                    // Manejar error al crear el grupo
                    val errorBody = grupoResponse.errorBody()?.string() ?: "Error vacío"
                    Log.e("Grupo", "Error al crear grupo: ${grupoResponse.code()} - $errorBody")
                }
            } catch (e: Exception) {
                // Manejar cualquier excepción no controlada
                Log.e("Grupo", "Excepción al crear grupo o miembros: ${e.message}", e)
            }
        }
    }








}