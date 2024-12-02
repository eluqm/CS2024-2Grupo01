import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.GrupoMentoria
import edu.cram.mentoriapp.Model.SesionMentoriaLista
import edu.cram.mentoriapp.Model.UsuarioLista
import edu.cram.mentoriapp.R

class MentoriadoAdapter(
    private val allItems: MutableList<UsuarioLista>,
    val onItemSelected: (UsuarioLista) -> Unit,
    val onDeleteSelected: (UsuarioLista) -> Unit
) : RecyclerView.Adapter<MentoriadoViewHolder>() {

    private val currentItems = mutableListOf<UsuarioLista>() // Lista visible

    init {
        currentItems.addAll(allItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentoriadoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_mentoriado, parent, false)
        return MentoriadoViewHolder(itemView)
    }

    override fun getItemCount(): Int = currentItems.size

    override fun onBindViewHolder(holder: MentoriadoViewHolder, position: Int) {
        val item = currentItems[position]
        holder.render(item, onItemSelected, onDeleteSelected)
    }
    fun updateList(newItems: List<UsuarioLista>) {
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
