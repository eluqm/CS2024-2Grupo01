package edu.cram.mentoriapp.Adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.GrupoMentoriaPlus
import edu.cram.mentoriapp.R


class GruposViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val viewNombre = itemView.findViewById<TextView>(R.id.txt_nombre2)
    private val viewFecha = itemView.findViewById<TextView>(R.id.txt_fecha_y_hora2)
    @SuppressLint("SetTextI18n")
    fun render(item: GrupoMentoriaPlus, onClickListener:(GrupoMentoriaPlus) -> Unit) {
        viewNombre.text = item.nombre
        viewFecha.text = item.jefeName

        itemView.setOnClickListener() {
            onClickListener(item)
        }
    }
}