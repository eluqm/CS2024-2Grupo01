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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Adapter.GruposAdapter
import edu.cram.mentoriapp.Model.GrupoMentoria
import edu.cram.mentoriapp.Model.MiembroGrupo
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Response

class CoorGruposFragment : Fragment(R.layout.fragment_coor_grupos) {

    private lateinit var gruposAdapter: GruposAdapter
    private lateinit var mentores: List<Usuario> // Lista de escuelas a mostrar
    private lateinit var apiRest: ApiRest
    // Inicializa gruposMentoria como una lista vacía desde el principio
    private var gruposMentoria: MutableList<GrupoMentoria> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiRest = RetrofitClient.makeRetrofitClient()

        initRecycleView(view)
        view.findViewById<Button>(R.id.boton_crear_grupo).setOnClickListener {
            loadMentores()
        }

    }

    private fun initRecycleView(view: View) {
        //loadGruposManualmente()
        loadGrupos()
        val manager = LinearLayoutManager(context)
        Toast.makeText(requireContext(), "Antes del recicler", Toast.LENGTH_SHORT).show()
        gruposAdapter = GruposAdapter(gruposMentoria) { user -> onItemSelected(user) }
        val decoration = DividerItemDecoration(context, manager.orientation)
        val usersRecyler = view.findViewById<RecyclerView>(R.id.recycler_grupos)
        usersRecyler.layoutManager = manager
        usersRecyler.adapter = gruposAdapter
        usersRecyler.addItemDecoration(decoration)
        Toast.makeText(requireContext(), "Despues del recicler", Toast.LENGTH_SHORT).show()
    }

    private fun loadGrupos() {
        Log.d("loadGrupos", "loadGrupos() iniciado")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Obtener el valor de escuelaId desde SharedPreferences
                val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
                val escuelaId = sharedPreferences.getInt("escuelaId", -1) // -1 por defecto si no se encuentra

                // Verificar si el escuelaId es válido
                if (escuelaId != -1) {
                    // Llamar a la API para obtener los grupos por escuelaId
                    val response = apiRest.getGrupoByEscuela(escuelaId)

                    if (response.isSuccessful) {
                        // Asignar los grupos obtenidos
                        val grupos = response.body()

                        // Comprobar si el cuerpo no es null y no está vacío
                        if (grupos != null && grupos.isNotEmpty()) {
                            gruposMentoria.clear() // Limpiar la lista antes de agregar nuevos elementos
                            gruposMentoria.addAll(grupos) // Agregar los grupos obtenidos
                            gruposAdapter.notifyDataSetChanged() // Notificar al adaptador sobre los cambios
                            Toast.makeText(requireContext(), "Grupos obtenidos: ${gruposMentoria.toString()}", Toast.LENGTH_LONG).show()
                            Log.d("loadGrupos", "Se obtuvieron grupos para la escuelaId $escuelaId: ${gruposMentoria.toString()}")

                        } else {
                            // Mensaje si no hay grupos disponibles
                            Toast.makeText(requireContext(), "No hay grupos disponibles para esta escuela", Toast.LENGTH_SHORT).show()
                            Log.d("loadGrupos", "No hay grupos disponibles para la escuelaId $escuelaId.")
                        }
                    } else {
                        // Manejo de errores en caso de que la respuesta no sea exitosa
                        val errorBody = response.errorBody()?.string() ?: "Cuerpo de error vacío"
                        Toast.makeText(requireContext(), "Error al cargar grupos: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                        Log.d("loadGrupos", "Error en la respuesta de la API: ${response.code()} - $errorBody")
                    }
                } else {
                    // Si no se encuentra el escuelaId en SharedPreferences
                    Toast.makeText(requireContext(), "Escuela ID no encontrado en SharedPreferences", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Manejo de excepciones (errores de red, etc.)
                Toast.makeText(requireContext(), "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("loadGrupos", "Error de red: ${e.message}")
            }
        }
    }


    private fun loadGruposManualmente() {

        // Datos ficticios para llenar el objeto gruposMentoria
        val grupo1 = GrupoMentoria(
            grupoId = 1,
            jefeId = 101,
            nombre = "Grupo de Matemáticas",
            horarioId = 201,
            descripcion = "Este grupo se enfoca en el estudio avanzado de matemáticas.",
            creadoEn = "2024-01-15 08:30:00"
        )

        val grupo2 = GrupoMentoria(
            grupoId = 2,
            jefeId = 102,
            nombre = "Grupo de Física",
            horarioId = 202,
            descripcion = "Grupo dedicado a la física teórica.",
            creadoEn = "2024-02-10 10:00:00"
        )

        val grupo3 = GrupoMentoria(
            grupoId = 3,
            jefeId = 103,
            nombre = "Grupo de Programación",
            horarioId = 203,
            descripcion = "Aprenderemos sobre programación en diferentes lenguajes.",
            creadoEn = "2024-03-12 09:15:00"
        )

        // Agregar los grupos a la lista
        gruposMentoria.addAll(listOf(grupo1, grupo2, grupo3))

        // Verificar que se han agregado los grupos
        Log.i("loadGruposManualmente", "Se agregaron ${gruposMentoria.size} grupos.")
    }



    private fun onItemSelected(user:GrupoMentoria) {
        Toast.makeText(requireActivity(), user.nombre, Toast.LENGTH_SHORT).show()
    }

    private fun loadMentores() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Obtener el valor de escuelaId desde SharedPreferences
                val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
                val escuelaId = sharedPreferences.getInt("escuelaId", 1) // -1 es el valor por defecto si no existe

                val response = apiRest.findUsuariosByTypeAndSchool("mentor", escuelaId)

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