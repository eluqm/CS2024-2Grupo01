package edu.cram.mentoriapp.Mentor

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import edu.cram.mentoriapp.Model.AsistenciaSesion
import edu.cram.mentoriapp.Model.MiembroGrupo
import edu.cram.mentoriapp.Model.SesionMentoria
import edu.cram.mentoriapp.Model.UsuarioLista
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MentorLlamadoAsistenciaFragment : Fragment(R.layout.fragment_llamado_asistencia) {
    private lateinit var apiRest: ApiRest
    private var mentoriadosXgrupo: MutableList<UsuarioLista> = mutableListOf()
    private var photoByteArray: ByteArray? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiRest = RetrofitClient.makeRetrofitClient()

        val checkBoxContainer: LinearLayout = view.findViewById(R.id.container_checkboxes)
        val cerrarAsistencia = view.findViewById<Button>(R.id.btn_close_attendance)
        val tema = view.findViewById<EditText>(R.id.et_topic)
        val descriptionEditText = view.findViewById<EditText>(R.id.et_description)
        val foto = view.findViewById<ImageButton>(R.id.imageButton)

        val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
        val mentorId = sharedPreferences.getInt("userId", -1)
        val grupoId = sharedPreferences.getInt("grupoId", -1)
        val horaProgramadaStr = sharedPreferences.getString("horaProgramada", null)
        val diaProgramadoStr = sharedPreferences.getString("diaProgramado", null)
        if (mentorId != -1 && grupoId != -1 && horaProgramadaStr != null && diaProgramadoStr != null) {

            loadMentoriados(mentorId, checkBoxContainer)



            foto.setOnClickListener {
                openCamera()
            }

            cerrarAsistencia.setOnClickListener {

                val hoy = LocalDate.now()
                val diaActual = hoy.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale("es", "ES")).lowercase(Locale.getDefault())
                val horaActual = LocalTime.now()

                Log.d("hola21", "dia: $diaActual")
                Log.d("hola21", "hora: $horaActual")

                val temaa = tema.text.toString()
                val descripcion = descriptionEditText.text.toString()
                val seleccionados = getSelectedMentoriados(checkBoxContainer)

                if (temaa.isBlank() || descripcion.isBlank()) {
                    Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Compara el día
                if (diaProgramadoStr.lowercase(Locale.getDefault()) != diaActual) {
                    Toast.makeText(requireContext(), "La asistencia solo está disponible el día programado", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Parsear la hora programada
                val horaProgramada = LocalTime.parse(horaProgramadaStr, DateTimeFormatter.ofPattern("HH:mm:ss"))

                // Calcular el rango de 45 minutos
                val inicioRango = horaProgramada
                val finRango = horaProgramada.plusMinutes(45)

                // Validar que la hora actual esté dentro del rango
                if (horaActual.isBefore(inicioRango)) {
                    Toast.makeText(requireContext(), "Aún no es hora de la asistencia", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else if (horaActual.isAfter(finRango)) {
                    Toast.makeText(requireContext(), "Ya pasó la hora de la asistencia", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val estadoSesion = "realizada"

                photoByteArray?.let { it1 ->
                    enviarSesion(grupoId, temaa, descripcion, estadoSesion,
                        it1
                    ) { sesionId ->
                        enviarAsistencias(sesionId, seleccionados, checkBoxContainer)
                    }
                }

                view.findNavController().navigate(R.id.mentorHomeFragment)

            }
        } else {
            Toast.makeText(requireContext(), "Error al obtener la información del mentor o grupo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as? Bitmap
            if (photo != null) {
                photoByteArray = bitmapToByteArray(photo)
                Toast.makeText(requireContext(), "Foto capturada con éxito: \n ${photoByteArray.toString()}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return outputStream.toByteArray()
    }

    private fun loadMentoriados(mentorId: Int, checkBoxContainer: LinearLayout) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiRest.getMiembrosPorJefe(mentorId)
                if (response.isSuccessful) {
                    val mentoriados = response.body() ?: emptyList()
                    if (mentoriados.isNotEmpty()) {
                        mentoriadosXgrupo.clear()
                        mentoriadosXgrupo.addAll(mentoriados)
                        generateCheckBoxes(mentoriados, checkBoxContainer)
                    } else {
                        Toast.makeText(requireContext(), "No hay mentoriados disponibles", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error vacío"
                    Log.e("Mentoriados", "Error: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                Log.e("Mentoriados", "Error de red: ${e.message}")
            }
        }
    }

    private fun generateCheckBoxes(mentoriados: List<UsuarioLista>, checkBoxContainer: LinearLayout) {
        checkBoxContainer.removeAllViews()
        mentoriados.forEach { usuario ->
            val checkBox = CheckBox(requireContext()).apply {
                text = usuario.nombreCompletoUsuario
                tag = usuario.id
                isChecked = true
            }
            checkBoxContainer.addView(checkBox)
        }
    }

    private fun getSelectedMentoriados(checkBoxContainer: LinearLayout): List<Int> {
        val seleccionados = mutableListOf<Int>()
        for (i in 0 until checkBoxContainer.childCount) {
            val child = checkBoxContainer.getChildAt(i)
            if (child is CheckBox && child.isChecked) {
                seleccionados.add(child.tag as Int)
            }
        }
        return seleccionados
    }

    private fun enviarSesion(
        grupoId: Int,
        tema: String,
        descripcion: String,
        estado: String,
        foto: ByteArray,
        onSuccess: (Int) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sesionMentoria = SesionMentoria(
                    grupoId = grupoId,
                    temaSesion = tema,
                    notas = descripcion,
                    estado = estado,
                    fotografia = foto
                )
                val response = apiRest.crearSesion(sesionMentoria)
                if (response.isSuccessful) {
                    val sesionId = response.body()
                    if (sesionId != null) {
                        Toast.makeText(requireContext(), "Sesión creada con éxito", Toast.LENGTH_SHORT).show()
                        onSuccess(sesionId)
                    } else {
                        Toast.makeText(requireContext(), "Error al obtener el ID de la sesión", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al crear sesión", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("EnviarSesion", "Error: ${e.message}")
            }
        }
    }

    private fun enviarAsistencias(sesionId: Int, seleccionados: List<Int>, checkBoxContainer: LinearLayout) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val asistencias = mutableListOf<AsistenciaSesion>()
                for (i in 0 until checkBoxContainer.childCount) {
                    val child = checkBoxContainer.getChildAt(i)
                    if (child is CheckBox) {
                        val mentoriadoId = child.tag as Int
                        val asistio = seleccionados.contains(mentoriadoId)
                        asistencias.add(AsistenciaSesion(sesionId, mentoriadoId, asistio))
                    }
                }
                val response = apiRest.registrarAsistencias(asistencias)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Asistencias registradas con éxito", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error al registrar asistencias", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("EnviarAsistencias", "Error: ${e.message}")
            }
        }
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }
}