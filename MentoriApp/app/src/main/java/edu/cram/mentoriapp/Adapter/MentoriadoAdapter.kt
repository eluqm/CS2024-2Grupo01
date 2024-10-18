import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.R

class MentoriadoAdapter(private val items: MutableList<Usuario>, val onItemSelected: (Usuario) -> Unit) :
    RecyclerView.Adapter<MentoriadoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentoriadoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_mentoriado, parent, false)
        return MentoriadoViewHolder(itemView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MentoriadoViewHolder, position: Int) {
        val item = items[position]
        holder.render(item, onItemSelected)
    }

    fun updateUsuarios(newUsuarios: MutableList<Usuario>) {
        items.clear()
        items.addAll(newUsuarios)
        notifyDataSetChanged()
    }
}
