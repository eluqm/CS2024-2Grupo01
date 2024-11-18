package edu.cram.mentoriapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.Chat
import edu.cram.mentoriapp.R

class ChatAdapter(val items: MutableList<Chat>, val onItemSelected: (Chat) -> Unit): RecyclerView.Adapter<ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_chat,parent, false)
        return ChatViewHolder(itemView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = items[position]
        holder.render(item, onItemSelected)
    }
}