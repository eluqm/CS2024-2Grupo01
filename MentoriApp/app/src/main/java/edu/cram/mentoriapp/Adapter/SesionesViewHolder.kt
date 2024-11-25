package edu.cram.mentoriapp.Adapter

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.GrupoMentoria
import edu.cram.mentoriapp.R

class SesionesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


    //private val viewCorreo = itemView.findViewById<TextView>(R.id.correo)

    fun render(
        item: GrupoMentoria,
        onClickListener: (GrupoMentoria) -> Unit
    ) {
        //viewCorreo.text = item.email

        itemView.setOnClickListener() { onClickListener(item) }
    }
}