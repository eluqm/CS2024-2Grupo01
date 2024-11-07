package edu.cram.mentoriapp.Adapter

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.SesionMentoriaLista
import edu.cram.mentoriapp.R

class SesionListaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val viewtemaSesion = itemView.findViewById<TextView>(R.id.temaSesion)
    private val viewlugar = itemView.findViewById<TextView>(R.id.lugar)
    private val viewfechaRegistrada = itemView.findViewById<TextView>(R.id.fechaRegistrada)
    private val viewnumeroParticipantes = itemView.findViewById<TextView>(R.id.numeroParticipantes)
    private val imgUsuario = itemView.findViewById<ImageButton>(R.id.img_usuario)

    @SuppressLint("SetTextI18n")
    fun render(
        item: SesionMentoriaLista,
        onClickListener: (SesionMentoriaLista) -> Unit
    ) {
        viewtemaSesion.text = item.temaSesion
        viewlugar.text = item.lugar
        viewfechaRegistrada.text = "Fecha: " + item.fechaRegistrada
        viewnumeroParticipantes.text = item.participantes

        itemView.setOnClickListener() { onClickListener(item) }
    }
}