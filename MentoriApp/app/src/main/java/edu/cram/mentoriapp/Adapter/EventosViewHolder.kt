package edu.cram.mentoriapp.Adapter

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.Evento
import edu.cram.mentoriapp.R

class EventosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val viewNombre = itemView.findViewById<TextView>(R.id.tv_event_card_title)
    private val viewDescripcion = itemView.findViewById<TextView>(R.id.tv_event_card_description)
    private val viewFecha = itemView.findViewById<TextView>(R.id.tv_event_card_date)
    @SuppressLint("SetTextI18n")
    fun render(item: Evento, onClickListener:(Evento) -> Unit) {
        viewNombre.text = item.nombre
        viewDescripcion.text = item.descripcion
        viewFecha.text = item.fecha_evento

        itemView.setOnClickListener() {
            onClickListener(item)
        }
    }
}