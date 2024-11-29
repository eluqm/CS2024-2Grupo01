import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.Evento
import edu.cram.mentoriapp.Model.GrupoMentoria
import edu.cram.mentoriapp.Model.HorarioCell
import edu.cram.mentoriapp.R

class HorarioAdapter(private val celdas: List<HorarioCell>, val onItemSelected: (HorarioCell) -> Unit) : RecyclerView.Adapter<HorarioAdapter.HorarioViewHolder>() {

    class HorarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.cellTextView)

        fun render(item: HorarioCell, onClickListener:(HorarioCell) -> Unit) {
            if (item.horarioId == null) {
                itemView.background = null
            }
            itemView.setOnClickListener() {
                onClickListener(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tem_horario_cell, parent, false)
        return HorarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: HorarioViewHolder, position: Int) {
        val cell = celdas[position]
        holder.textView.text = cell.lugar ?: "" // Mostrar el lugar o vacío si no hay evento
        holder.render(cell, onItemSelected)
    }

    override fun getItemCount(): Int = celdas.size
}
