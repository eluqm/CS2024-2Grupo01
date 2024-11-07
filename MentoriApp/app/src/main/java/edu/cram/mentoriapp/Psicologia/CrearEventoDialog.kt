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
    private lateinit var buttonSeleccionarImagen: Button
    private var selectedImageUri: Uri? = null

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
            // Aquí debes implementar la lógica para seleccionar una imagen
            // Por simplicidad, no se implementará en este ejemplo
            Toast.makeText(context, "Seleccionar imagen (sin funcionalidad por ahora)", Toast.LENGTH_SHORT).show()
        }

        // Botón para crear el evento y horario
        view.findViewById<Button>(R.id.buttonCrearEvento).setOnClickListener {
            crearEventoYHorario()
        }

        builder.setView(view)
            .setTitle("Crear Nuevo Evento")
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }

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
        val dia = editTextDia.text.toString()
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
            Log.d("AEA", "llegue")
            // Ahora crea el evento con el horarioId del nuevo horario
            val nuevoEvento = Evento(
                nombre = nombre,
                horarioId = horarioCreado.horarioId ?: 0, // Usar el ID del horario creado
                descripcion = if (descripcion.isNotEmpty()) descripcion else null,
                poster = "Sin Imagen", // Coloca un ByteArray provisional
                url = if (url.isNotEmpty()) url else null,
                fecha_evento = dia
            )

            Log.d("AEA", nuevoEvento.toString())

            // Crear el evento
            commonDAO.createEvent(nuevoEvento)
        }

        dismiss() // Cerrar el diálogo después de crear
    }
}
