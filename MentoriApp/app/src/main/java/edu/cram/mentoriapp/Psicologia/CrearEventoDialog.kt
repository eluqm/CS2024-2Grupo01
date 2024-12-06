package edu.cram.mentoriapp.Psicologia

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import edu.cram.mentoriapp.DAO.CommonDAO
import edu.cram.mentoriapp.Model.Evento
import edu.cram.mentoriapp.Model.Horario
import edu.cram.mentoriapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

class CrearEventoDialog(private val context: Context, private val commonDAO: CommonDAO) : DialogFragment() {

    private lateinit var editTextLugar: EditText
    private lateinit var editTextDia: EditText
    private lateinit var editTextHoraInicio: EditText
    private lateinit var editTextHoraFin: EditText
    private lateinit var editTextNombre: EditText
    private lateinit var editTextDescripcion: EditText
    private lateinit var editTextUrl: EditText
    private lateinit var buttonSeleccionarImagen: ImageButton
    private var selectedImageUri: Uri? = null
    private var selectedImageBytes: ByteArray? = null
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                selectedImageBytes = inputStream.readBytes()
            }
            Toast.makeText(context, "Imagen seleccionada exitosamente", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(context, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_crear_evento, null)

        editTextLugar = view.findViewById(R.id.editTextLugar)
        editTextDia = view.findViewById(R.id.editTextFecha)
        editTextHoraInicio = view.findViewById(R.id.editTextHoraInicio)
        editTextHoraFin = view.findViewById(R.id.editTextHoraFin)
        editTextNombre = view.findViewById(R.id.editTextNombre)
        editTextDescripcion = view.findViewById(R.id.editTextDescripcion)
        editTextUrl = view.findViewById(R.id.editTextUrl)
        buttonSeleccionarImagen = view.findViewById(R.id.buttonSeleccionarImagen)

        // Establecer el click listener para seleccionar una fecha
        editTextDia.setOnClickListener { mostrarDatePicker() }

        // Establecer el click listener para seleccionar la hora de inicio
        editTextHoraInicio.setOnClickListener { mostrarTimePicker { hora -> editTextHoraInicio.setText(hora) } }

        // Establecer el click listener para seleccionar la hora de fin
        editTextHoraFin.setOnClickListener { mostrarTimePicker { hora -> editTextHoraFin.setText(hora) } }

        // Establecer el click listener para seleccionar una imagen
        buttonSeleccionarImagen.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }



        // Botón para crear el evento y horario
        view.findViewById<Button>(R.id.buttonCrearEvento).setOnClickListener {
            val lugar = editTextLugar.text.toString().trim()
            val fecha = editTextDia.text.toString().trim()
            val horaInicio = editTextHoraInicio.text.toString().trim()
            val horaFin = editTextHoraFin.text.toString().trim()
            val nombre = editTextNombre.text.toString().trim()
            val descripcion = editTextDescripcion.text.toString().trim()
            val url = editTextUrl.text.toString().trim()

            // Validaciones
            when {
                lugar.isEmpty() -> editTextLugar.error = "Este campo es obligatorio"
                fecha.isEmpty() -> editTextDia.error = "Este campo es obligatorio"
                horaInicio.isEmpty() -> editTextHoraInicio.error = "Este campo es obligatorio"
                horaFin.isEmpty() -> editTextHoraFin.error = "Este campo es obligatorio"
                nombre.isEmpty() -> editTextNombre.error = "Este campo es obligatorio"
                descripcion.isEmpty() -> editTextDescripcion.error = "Este campo es obligatorio"
                selectedImageUri == null -> Toast.makeText(
                    context,
                    "Debes seleccionar una imagen",
                    Toast.LENGTH_SHORT
                ).show()

                else -> {
                    crearEventoYHorario()
                }
            }
        }


        view.findViewById<Button>(R.id.buttonCancelarEvento).setOnClickListener {
            dismiss()
        }

        builder.setView(view)

        return builder.create()
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
            // Formato de fecha en formato "dd/MM/yyyy"
            editTextDia.setText(String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear))

        }, year, month, day).show()
    }
    private fun obtenerDiaSemana(fecha: String): String {
        // Define el formato de la fecha
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
        val date = formatoFecha.parse(fecha) // Convierte la fecha de texto a Date

        // Configura el calendario con la fecha dada
        val calendar = Calendar.getInstance()
        calendar.time = date

        // Formato para obtener el día de la semana en español
        val diaSemana = SimpleDateFormat("EEEE", Locale("es", "ES")).format(calendar.time)

        // Convierte la primera letra a mayúscula y elimina tildes
        return quitarTildes(diaSemana.replaceFirstChar { it.uppercaseChar() })
    }

    private fun quitarTildes(cadena: String): String {
        // Normaliza la cadena y elimina las tildes
        return Normalizer.normalize(cadena, Normalizer.Form.NFD).replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
    }


    private fun mostrarTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(context, { _, selectedHour, selectedMinute ->
            // Formato de tiempo en formato "HH:mm"
            onTimeSelected(String.format("%02d:%02d", selectedHour, selectedMinute))
        }, hour, minute, true).show()
    }



    private fun crearEventoYHorario() {
        // Obtener los datos de los EditText
        val lugar = editTextLugar.text.toString()
        var dia = editTextDia.text.toString()
        val horaInicio = editTextHoraInicio.text.toString() + ":00"
        val horaFin = editTextHoraInicio.text.toString() + ":00"
        val nombre = editTextNombre.text.toString()
        val descripcion = editTextDescripcion.text.toString()
        val url = editTextUrl.text.toString()

        // Crear el horario primero
        val nuevoHorario = Horario(
            lugar = lugar,
            dia = obtenerDiaSemana(dia),
            horaInicio = horaInicio,
            horaFin = horaFin,
            estado = true // Estado por defecto en true
        )

        Log.d("AEA", nuevoHorario.toString())

        // Llamar a la función para crear el horario en un hilo de fondo
        CoroutineScope(Dispatchers.IO).launch {
            // Crear el horario
            // Aquí deberías manejar la respuesta de la creación del horario
            // Simulamos que se obtiene un ID del nuevo horario
            val horarioCreado = commonDAO.createHorario(nuevoHorario) // Debes crear este método en CommonDAO

            val posterData = selectedImageBytes ?: "Sin Imagen".toByteArray()
            Log.d("AEA", "llegue")
            // Ahora crea el evento con el horarioId del nuevo horario
            dia = dia.replace("/", "-")

            val inputFormat = SimpleDateFormat("dd-MM-yyyy")
            // Crear un formato de salida para "yyyy-MM-dd"
            val outputFormat = SimpleDateFormat("yyyy-MM-dd")

            try {
                // Parsear la fecha original
                val date: Date = inputFormat.parse(dia)
                // Formatear la fecha al nuevo formato
                dia = outputFormat.format(date)
            } catch (e: Exception) {
                Log.e("AEA", "Error al formatear la fecha", e)
                // Si hay un error, puedes optar por manejarlo, por ejemplo, asignar un valor por defecto.
                dia = "0000-00-00" // O cualquier valor predeterminado que desees
            }

            val nuevoEvento = horarioCreado?.let {
                Evento(
                    nombre = nombre,
                    horarioId = it, // Usar el ID del horario creado
                    descripcion = if (descripcion.isNotEmpty()) descripcion else null,
                    poster = posterData, // Coloca un ByteArray provisional
                    url = if (url.isNotEmpty()) url else null,
                    fecha_evento = dia

                )
            }

            Log.d("AEA", nuevoEvento.toString())

            // Crear el evento
            commonDAO.createEvent(nuevoEvento!!)
        }

        dismiss() // Cerrar el diálogo después de crear
    }
}
