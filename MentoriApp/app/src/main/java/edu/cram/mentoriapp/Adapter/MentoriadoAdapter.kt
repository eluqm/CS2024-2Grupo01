import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.GrupoMentoria
import edu.cram.mentoriapp.Model.SesionMentoriaLista
import edu.cram.mentoriapp.Model.UsuarioLista
import edu.cram.mentoriapp.R

class MentoriadoAdapter(
    private val items: MutableList<UsuarioLista>,
    val onItemSelected: (UsuarioLista) -> Unit
) : RecyclerView.Adapter<MentoriadoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentoriadoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_mentoriado, parent, false)
        return MentoriadoViewHolder(itemView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MentoriadoViewHolder, position: Int) {
        val item = items[position]
        holder.render(item, onItemSelected)
    }
    fun updateList(newItems: List<UsuarioLista>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
    fun addUser(item: UsuarioLista) {
        items.add(0, item)
        notifyItemInserted(0)
    }

    private fun deleteUser(index: Int) {
        items.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, items.size)
    }

    fun editUser(index: Int, item: UsuarioLista) {
        items.removeAt(index)
        items[index] = item
        notifyItemChanged(index)
    }
}
