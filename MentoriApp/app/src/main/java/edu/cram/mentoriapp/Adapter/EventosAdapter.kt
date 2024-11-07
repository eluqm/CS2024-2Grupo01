package edu.cram.mentoriapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.Evento
import edu.cram.mentoriapp.R

class EventosAdapter(val items: MutableList<Evento>,
                     val onItemSelected: (Evento) -> Unit
): RecyclerView.Adapter<EventosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventosViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_event_card,parent, false)
        return EventosViewHolder(itemView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: EventosViewHolder, position: Int) {
        val item = items[position]
        holder.render(item, onItemSelected)
    }

    fun addUser(item: Evento) {
        items.add(0, item)
        notifyItemInserted(0)
    }

    private fun deleteUser(index: Int) {
        items.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, items.size)
    }

    fun editUser(index: Int, item: Evento) {
        items.removeAt(index)
        items[index] = item
        notifyItemChanged(index)
    }

}