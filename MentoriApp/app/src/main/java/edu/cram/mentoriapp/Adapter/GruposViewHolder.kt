package edu.cram.mentoriapp.Adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.GrupoMentoria
import edu.cram.mentoriapp.R


class GruposViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val viewNombre = itemView.findViewById<TextView>(R.id.txt_nombre2)
    private val viewFecha = itemView.findViewById<TextView>(R.id.txt_fecha_y_hora2)
    private val viewMonto = itemView.findViewById<TextView>(R.id.txt_monto2)
    @SuppressLint("SetTextI18n")
    fun render(item: GrupoMentoria, onClickListener:(GrupoMentoria) -> Unit) {
        viewNombre.text = item.nombre
        viewFecha.text = item.descripcion
        viewMonto.text = item.jefeId.toString()

        itemView.setOnClickListener() {
            onClickListener(item)
        }
    }
}