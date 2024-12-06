package edu.cram.mentoriapp.Adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.Evento
import edu.cram.mentoriapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            // Formato de la fecha esperado
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaEvento = dateFormat.parse(item.fecha_evento)
            val fechaActual = Date()

            if (fechaEvento != null) {
                if (fechaEvento.before(fechaActual)) {
                    // Evento pasado
                    itemView.setBackgroundColor(Color.parseColor("#FFE0E0")) // Fondo rojo suave
                    viewNombre.setTextColor(Color.parseColor("#FF0000")) // Texto rojo
                    itemView.isEnabled = false // Deshabilitar clic
                } else {
                    // Evento vigente
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