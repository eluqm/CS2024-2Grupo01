import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.HorarioCell
import edu.cram.mentoriapp.R

class HorarioAdapter(private val celdas: List<HorarioCell>) : RecyclerView.Adapter<HorarioAdapter.HorarioViewHolder>() {

    class HorarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.cellTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tem_horario_cell, parent, false)
        return HorarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: HorarioViewHolder, position: Int) {
        val cell = celdas[position]
        holder.textView.text = cell.lugar ?: "" // Mostrar el lugar o vacío si no hay evento
    }

    override fun getItemCount(): Int = celdas.size
}
