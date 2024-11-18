package edu.cram.mentoriapp.Adapter

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.Chat
import edu.cram.mentoriapp.Model.Evento
import edu.cram.mentoriapp.R

class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val viewNombre = itemView.findViewById<TextView>(R.id.txt_emisor_chat)
    private val viewMensaje = itemView.findViewById<TextView>(R.id.txt_mensaje_chat)
    private val viewFecha = itemView.findViewById<TextView>(R.id.txt_fecha_chat)
    private val viewHora = itemView.findViewById<TextView>(R.id.txt_hora_chat)

    @SuppressLint("SetTextI18n")
    fun render(item: Chat, onClickListener:(Chat) -> Unit) {
        viewNombre.text = item.emisor
        viewMensaje.text = item.mensaje
        viewFecha.text = item.fecha
        viewHora.text = item.hora.substring(0, 5)

        itemView.setOnClickListener() {
            onClickListener(item)
        }
    }
}