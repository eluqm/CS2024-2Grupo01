package edu.cram.mentoriapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.SesionMentoriaLista
import edu.cram.mentoriapp.R

class SesionListaAdapter(
    private val allItems: MutableList<SesionMentoriaLista>, // Lista original
    val onItemSelected: (SesionMentoriaLista) -> Unit
) : RecyclerView.Adapter<SesionListaViewHolder>() {

    private val currentItems = mutableListOf<SesionMentoriaLista>() // Lista visible

    init {
        currentItems.addAll(allItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SesionListaViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_sesionlista, parent, false)
        return SesionListaViewHolder(itemView)
    }

    override fun getItemCount(): Int = currentItems.size

    override fun onBindViewHolder(holder: SesionListaViewHolder, position: Int) {
        val item = currentItems[position]
        holder.render(item, onItemSelected)
    }

    fun updateList(newItems: List<SesionMentoriaLista>) {
        currentItems.clear()
        currentItems.addAll(newItems)
        notifyDataSetChanged()
    }

    fun resetList() {
        currentItems.clear()
        currentItems.addAll(allItems)
        notifyDataSetChanged()
    }
}
