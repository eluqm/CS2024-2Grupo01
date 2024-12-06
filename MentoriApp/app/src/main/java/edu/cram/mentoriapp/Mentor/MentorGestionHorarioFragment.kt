package edu.cram.mentoriapp.Mentor

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
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
        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker)
        val proposeButton = view.findViewById<Button>(R.id.btn_proponer_horario)

        val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
        val mentorId = sharedPreferences.getInt("userId", -1)

        // Lista de horarios
        val horas = listOf(
            "07:15:00", "08:00:00", "08:45:00", "09:30:00", "10:15:00",
            "11:00:00", "11:45:00", "12:30:00", "13:15:00", "14:00:00",
            "14:45:00", "15:30:00", "16:15:00", "17:00:00"
        )

        // Configura el NumberPicker
        numberPicker.minValue = 0
        numberPicker.maxValue = horas.size - 1
        numberPicker.displayedValues = horas.toTypedArray()

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
            val horaInicioString = horas[numberPicker.value]

            // Convierte la hora de inicio a LocalTime
            val horaInicio = LocalTime.parse(horaInicioString)
            val horaFin = horaInicio.plusMinutes(45)

            // Formatea las horas
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
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

            view.findNavController().apply {
                popBackStack(R.id.mentorHomeFragment, false) // Esto borra todo hasta llegar al fragmento especificado
                navigate(R.id.mentorHomeFragment) // Luego navegas al nuevo fragmento
            }

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
                        Log.d("ERROR","Error al crear horario: ${response.errorBody()?.string()}")

                    } else {
                        Log.d("ERROR","Error al crear horario: ${response.errorBody()?.string()}")
                        Log.d("ERROR","Hi: ${horario.toString()}")
                        Toast.makeText(
                            requireContext(),
                            "Error al crear horario: ${response.errorBody()?.string()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("ERROR2", "Error: ${e.message}")
                }
            }
        }
    }
}
