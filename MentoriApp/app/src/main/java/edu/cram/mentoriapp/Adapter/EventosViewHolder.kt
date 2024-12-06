package edu.cram.mentoriapp.Adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.Evento
import edu.cram.mentoriapp.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class EventosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val viewNombre = itemView.findViewById<TextView>(R.id.tv_event_card_title)
    private val viewDescripcion = itemView.findViewById<TextView>(R.id.tv_event_card_description)
    private val viewFecha = itemView.findViewById<TextView>(R.id.tv_event_card_date)
    @SuppressLint("SetTextI18n")
    fun render(item: Evento, onClickListener: (Evento) -> Unit) {
        // Asignar datos
        viewNombre.text = item.nombre
        viewDescripcion.text = item.descripcion
        viewFecha.text = item.fecha_evento


        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
// Configurar la zona horaria a Lima
            dateFormat.timeZone = TimeZone.getTimeZone("America/Lima")

// Parsear la fecha del evento
            val fechaEvento = dateFormat.parse(item.fecha_evento)

// Obtener la fecha actual (normalizada a día, mes y año)
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Lima"))
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val fechaActual = calendar.time
            Log.d("fechaEvento", "fechaEvento: $fechaEvento")
            Log.d("fechaActual", "fechaActual: $fechaActual")
            if (fechaEvento != null) {
                if (fechaEvento.before(fechaActual)) {
                    // Evento pasado
                    itemView.setBackgroundColor(Color.parseColor("#FFE0E0")) // Fondo rojo suave
                    viewNombre.setTextColor(Color.parseColor("#FF0000")) // Texto rojo
                    itemView.isEnabled = false // Deshabilitar clic
                } else if (fechaEvento.equals(fechaActual)) {
                    // Evento de hoy (vigente)
                    itemView.setBackgroundColor(Color.parseColor("#E0FFE0")) // Fondo verde suave
                    viewNombre.setTextColor(Color.parseColor("#008000")) // Texto verde
                    itemView.isEnabled = true // Habilitar clic
                } else {
                    // Evento futuro (vigente)
                    itemView.setBackgroundColor(Color.parseColor("#E0FFE0")) // Fondo verde suave
                    viewNombre.setTextColor(Color.parseColor("#008000")) // Texto verde
                    itemView.isEnabled = true // Habilitar clic
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Configurar el clic del elemento
        itemView.setOnClickListener {
            if (itemView.isEnabled) {
                onClickListener(item)
            }
        }
    }
}