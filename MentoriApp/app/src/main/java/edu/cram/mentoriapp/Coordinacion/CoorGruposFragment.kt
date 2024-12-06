package edu.cram.mentoriapp.Coordinacion

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import edu.cram.mentoriapp.Adapter.GruposAdapter
import edu.cram.mentoriapp.Adapter.MentorAdapter
import edu.cram.mentoriapp.Model.GrupoMentoria
import edu.cram.mentoriapp.Model.GrupoMentoriaPlus
import edu.cram.mentoriapp.Model.MiembroGrupo
import edu.cram.mentoriapp.Model.UserView
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Response

class CoorGruposFragment : Fragment(R.layout.fragment_coor_grupos) {

    private lateinit var gruposAdapter: GruposAdapter
    private lateinit var mentores: List<UserView> // Lista de escuelas a mostrar
    private lateinit var apiRest: ApiRest
    // Inicializa gruposMentoria como una lista vacía desde el principio
    private var gruposMentoria: MutableList<GrupoMentoriaPlus> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiRest = RetrofitClient.makeRetrofitClient()

        initRecycleView(view)
        view.findViewById<FloatingActionButton>(R.id.boton_crear_grupo).setOnClickListener {
            loadMentores()
        }

    }

    private fun initRecycleView(view: View) {
        //loadGruposManualmente()
        loadGrupos()
        val manager = LinearLayoutManager(context)
        //Toast.makeText(requireContext(), "Antes del recicler", Toast.LENGTH_SHORT).show()
        gruposAdapter = GruposAdapter(gruposMentoria) { user -> onItemSelected(user) }
        val usersRecyler = view.findViewById<RecyclerView>(R.id.recycler_grupos)
        usersRecyler.layoutManager = manager
        usersRecyler.adapter = gruposAdapter
        //Toast.makeText(requireContext(), "Despues del recicler", Toast.LENGTH_SHORT).show()
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
                            //Toast.makeText(requireContext(), "Grupos obtenidos: ${gruposMentoria.toString()}", Toast.LENGTH_LONG).show()
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


    private fun onItemSelected(group: GrupoMentoriaPlus) {
        Toast.makeText(requireActivity(), "Creado el" + group.creadoEn, Toast.LENGTH_LONG).show()
        val delivery = Bundle().apply {
            putInt("JefeID", group.jefeId)
        }
        requireView().findNavController().navigate(R.id.action_coorGruposFragment_to_coorSesionesGruposFragment, delivery)
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
                        showMentorSelectionDialog(mentores, escuelaId)
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


    private fun showMentorSelectionDialog(mentores: List<UserView>, escuelaId: Int) {
        if (mentores.isEmpty()) {
            Toast.makeText(requireContext(), "No hay mentores disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear un ArrayAdapter personalizado
        val adapter = object : ArrayAdapter<UserView>(requireContext(), R.layout.mentor_item, mentores) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.mentor_item, parent, false)

                val mentor = getItem(position)

                // Referencias de las vistas del layout
                val mentorNameTextView = view.findViewById<TextView>(R.id.mentor_name)
                val mentorSemesterTextView = view.findViewById<TextView>(R.id.mentor_semester)

                mentor?.let {
                    // Rellenar las vistas con la información del mentor
                    mentorNameTextView.text = it.fullName
                    mentorSemesterTextView.text = it.semester
                }

                return view
            }
        }

        // Mostrar el AlertDialog con el ArrayAdapter personalizado
        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona un Mentor")
            .setAdapter(adapter) { dialogInterface, which ->
                val mentorSeleccionado = mentores[which]
                println("Mentor seleccionado: ${mentorSeleccionado.fullName}, Semestre: ${mentorSeleccionado.semester}")
                showSemesterSelectionDialog(mentorSeleccionado, escuelaId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }




    private fun showSemesterSelectionDialog(mentorSeleccionado: UserView, escuelaId: Int) {
        val semestres = arrayOf("I", "II")  // Los valores de los semestres como strings

        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona el Semestre")
            .setItems(semestres) { _, which ->
                val semestreSeleccionado = semestres[which]  // Asignar el valor "I" o "II"
                // Llamamos al método para cargar los mentoriados del semestre
                loadMentoriados(mentorSeleccionado, semestreSeleccionado, escuelaId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }



    private fun loadMentoriados(mentorSeleccionado: UserView, semestreSeleccionado: String, escuelaId: Int) {
        Log.d("Jaula", "loadMentoriados() iniciado")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiRest.findUsuariosByTypeAndSchoolAndSemester("mentoriado", escuelaId, semestreSeleccionado)
                if (response.isSuccessful) {
                    Log.d("Jaula", "xd1")
                    val mentoriados = response.body() ?: emptyList()

                    showMentoriadosSelectionDialog(mentoriados, mentorSeleccionado)
                    Toast.makeText(requireContext(), "Mentoriados disponibles", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(requireContext(), "No hay mentoriados disponibles", Toast.LENGTH_SHORT).show()
                    val errorBody = response.errorBody()?.string() ?: "Error vacío"
                    Log.e("Mentoriados", "Error: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                Log.e("Mentoriados", "Error de red: ${e.message}")
            }
        }
    }



    private fun showMentoriadosSelectionDialog(mentoriados: List<UserView>, mentorSeleccionado: UserView) {



        val selectedMentoriados = mentoriados.take(20).map { it.id }.toMutableSet() // Auto-seleccionar los primeros 20
        val items = mentoriados.map { it.fullName }.toTypedArray()



        // Crear un AlertDialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Seleccionar Mentoriados")
        builder.setMultiChoiceItems(items, selectedMentoriados.map { it in mentoriados.map { it.id } }.toBooleanArray()) { dialog, which, isChecked ->
            val userId = mentoriados[which].id
            if (isChecked) {
                selectedMentoriados.add(userId) // Agregar el ID a la selección
            } else {
                selectedMentoriados.remove(userId) // Eliminar el ID de la selección
            }
        }

        builder.setPositiveButton("Crear Grupo") { dialog, _ ->
            showCreateGrupoMentoriaDialog(selectedMentoriados.toList() ,mentorSeleccionado) // Crear el grupo y luego las relaciones
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }


    private fun showCreateGrupoMentoriaDialog(selectedMentoriados: List<Int?>, mentorSeleccionado: UserView) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_create_grupo_mentoria, null)

        // Referencias a los campos del diálogo
        val editTextNombre = dialogView.findViewById<TextInputEditText>(R.id.editTextNombre)
        val editTextDescripcion = dialogView.findViewById<TextInputEditText>(R.id.editTextDescripcion)
        val inputLayoutNombre = dialogView.findViewById<TextInputLayout>(R.id.inputLayoutNombre)

        builder.setView(dialogView)
            .setTitle("Crear Grupo de Mentoría")
            .setPositiveButton("Crear") { dialog, which ->
                // Obtener el nombre y la descripción ingresados
                val nombre = editTextNombre.text.toString().trim()
                val descripcion = editTextDescripcion.text.toString().takeIf { it.isNotEmpty() }

                // Validar que el nombre del grupo no esté vacío
                if (nombre.isEmpty()) {
                    // Si no hay nombre, mostrar un mensaje de error
                    inputLayoutNombre.error = "El nombre del grupo es obligatorio"
                    return@setPositiveButton
                } else {
                    inputLayoutNombre.error = null  // Limpiar el error si hay un nombre
                }

                // Crear el objeto GrupoMentoria
                val grupoMentoria = mentorSeleccionado.id?.let {
                    GrupoMentoria(
                        jefeId = it, // Asegúrate de que Usuario tenga userId
                        nombre = nombre,
                        horarioId = 5, // ID del horario manualmente asignado
                        descripcion = descripcion
                    )
                }

                Log.d("XDDD", grupoMentoria.toString())

                // Pasar al siguiente paso si todo es válido
                if (grupoMentoria != null) {
                    createGrupo(selectedMentoriados, grupoMentoria)
                }
            }
            .setNegativeButton("Cancelar") { dialog, which -> dialog.cancel() }
            .show()
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
                    loadGrupos()
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