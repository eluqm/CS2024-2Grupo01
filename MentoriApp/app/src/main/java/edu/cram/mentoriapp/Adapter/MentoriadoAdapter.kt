package edu.cram.mentoriapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.Mentoriado
import edu.cram.mentoriapp.R

class MentoriadoAdapter(private val items: MutableList<Mentoriado>, val onItemSelected: (Mentoriado) -> Unit): RecyclerView.Adapter<MentoriadoViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentoriadoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_mentoriado,parent, false)
        return MentoriadoViewHolder(itemView)

    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MentoriadoViewHolder, position: Int) {
        val item = items[position]
        holder.render(item, onItemSelected)
    }

    fun addUser(mentoriado: Mentoriado) {
        items.add(0, mentoriado)
        notifyItemInserted(0)
    }

    private fun deleteUser(index: Int) {
        items.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, items.size)
    }

    fun editUser(index: Int, mentoriado: Mentoriado) {
        items.removeAt(index)
        items[index] = mentoriado
        notifyItemChanged(index)
    }
    

}