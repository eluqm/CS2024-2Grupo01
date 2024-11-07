package edu.cram.mentoriapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.SesionMentoriaLista
import edu.cram.mentoriapp.R

class SesionListaAdapter(
    private val items: MutableList<SesionMentoriaLista>,
    val onItemSelected: (SesionMentoriaLista) -> Unit
) : RecyclerView.Adapter<SesionListaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SesionListaViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_sesionlista, parent, false)
        return SesionListaViewHolder(itemView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SesionListaViewHolder, position: Int) {
        val item = items[position]
        holder.render(item, onItemSelected)
    }

    fun addUser(item: SesionMentoriaLista) {
        items.add(0, item)
        notifyItemInserted(0)
    }

    private fun deleteUser(index: Int) {
        items.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, items.size)
    }

    fun editUser(index: Int, item: SesionMentoriaLista) {
        items.removeAt(index)
        items[index] = item
        notifyItemChanged(index)
    }
}