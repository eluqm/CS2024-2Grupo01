import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.UsuarioLista
import edu.cram.mentoriapp.R

class MentoriadoAdapter(
    private val items: MutableList<UsuarioLista>,
    val onItemSelected: (UsuarioLista) -> Unit,
    val onDeleteUsuario: (UsuarioLista) -> Unit
) : RecyclerView.Adapter<MentoriadoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentoriadoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_mentoriado, parent, false)
        return MentoriadoViewHolder(itemView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MentoriadoViewHolder, position: Int) {
        val item = items[position]
        holder.render(item, onItemSelected, onDeleteUsuario)
    }

    fun updateUsuarios(newUsuarios: MutableList<UsuarioLista>) {
        items.clear()
        items.addAll(newUsuarios)
        notifyDataSetChanged()
    }

    fun removeUsuario(usuario: UsuarioLista) {
        items.remove(usuario)
        notifyDataSetChanged()
    }
}
