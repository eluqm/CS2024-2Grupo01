package edu.cram.mentoriapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.GrupoMentoria
import edu.cram.mentoriapp.R

class GruposAdapter(val items: MutableList<GrupoMentoria>,
                    val onItemSelected: (GrupoMentoria) -> Unit
): RecyclerView.Adapter<GruposViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GruposViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_grupo,parent, false)
        return GruposViewHolder(itemView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: GruposViewHolder, position: Int) {
        val item = items[position]
        holder.render(item, onItemSelected)
    }

    fun addUser(item: GrupoMentoria) {
        items.add(0, item)
        notifyItemInserted(0)
    }

    private fun deleteUser(index: Int) {
        items.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, items.size)
    }

    fun editUser(index: Int, item: GrupoMentoria) {
        items.removeAt(index)
        items[index] = item
        notifyItemChanged(index)
    }

}