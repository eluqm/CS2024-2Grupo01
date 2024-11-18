package edu.cram.mentoriapp.Mentor

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import edu.cram.mentoriapp.Model.Horario
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.ApiRest
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class MentorGestionHorarioFragment : Fragment(R.layout.fragment_gestion_horario) {
    private lateinit var apiRest: ApiRest
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiRest = RetrofitClient.makeRetrofitClient()
        // Obtén las vistas del layout
        val radioGroupDays = view.findViewById<RadioGroup>(R.id.radio_group_days)
        val timePicker = view.findViewById<TimePicker>(R.id.time_picker)
        val proposeButton = view.findViewById<Button>(R.id.btn_proponer_horario)

        val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
        val mentorId = sharedPreferences.getInt("userId", -1)

        // Configura el TimePicker para formato de 12 horas (AM/PM)
        timePicker.setIs24HourView(false)

        // Botón para guardar los datos
        proposeButton.setOnClickListener {
            // Día seleccionado
            val selectedDay = when (radioGroupDays.checkedRadioButtonId) {
                R.id.rb_lu -> "Lunes"
                R.id.rb_ma -> "Martes"
                R.id.rb_mi -> "Miercoles"
                R.id.rb_ju -> "Jueves"
                R.id.rb_vi -> "Viernes"
                else -> null
            }

            if (selectedDay == null) {
                Toast.makeText(requireContext(), "Por favor selecciona un día", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Hora de inicio seleccionada
            val hour = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                timePicker.hour
            } else {
                timePicker.currentHour
            }

            val minute = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                timePicker.minute
            } else {
                timePicker.currentMinute
            }
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            // Convierte a LocalTime
            val horaInicio = LocalTime.of(hour, minute, 0)

            // Calcula la hora_fin sumando 45 minutos
            val horaFin = horaInicio.plusMinutes(45)
            val horaInicioString = horaInicio.format(formatter)
            val horaFinString = horaFin.format(formatter)
            // Asigna NULL a "aula" como se solicitó
            val aula = null

            // Estado predeterminado como false
            val estado = false
            // Guarda los datos en la base de datos (lógica simulada aquí)
            saveHorarioToDatabase(
                jefeId = mentorId,
                lugar = aula,
                dia = selectedDay,
                horaInicio = horaInicioString,
                horaFin = horaFinString,
                estado = estado
            )
        }
    }

    private fun saveHorarioToDatabase(
        jefeId: Int,
        lugar: String?,
        dia: String,
        horaInicio: String,
        horaFin: String,
        estado: Boolean
    ) {
        Log.d("dasdasd","Guardando en DB -> Lugar: $lugar, Día: $dia, Hora inicio: $horaInicio, Hora fin: $horaFin, Estado: $estado")
        val horario = Horario(
            lugar = "lugar",
            dia = dia,
            horaInicio = horaInicio,
            horaFin = horaFin,
            estado = estado
        )

        Log.d("dasdasd","$horario")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiRest.createHorario2(horario, jefeId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val horarioId = response.body()
                        Toast.makeText(
                            requireContext(),
                            "Horario creado con éxito. ID: $horarioId",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.d("dasdasd","Error al crear horario: ${response.errorBody()?.string()}")
                        Toast.makeText(
                            requireContext(),
                            "Error al crear horario: ${response.errorBody()?.string()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
