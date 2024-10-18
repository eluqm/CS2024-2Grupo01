package edu.cram.mentoriapp.Adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.Mentoriado

class MentoriadoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun render(item: Mentoriado, onClickListener: (Mentoriado) -> Unit) {


        itemView.setOnClickListener { onClickListener(item) }
    }
}
