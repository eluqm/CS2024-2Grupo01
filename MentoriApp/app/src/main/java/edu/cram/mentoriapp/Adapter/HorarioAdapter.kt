import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.HorarioCell
import edu.cram.mentoriapp.R

class HorarioAdapter(private val celdas: List<HorarioCell>, val onItemSelected: (HorarioCell) -> Unit) : RecyclerView.Adapter<HorarioAdapter.HorarioViewHolder>() {

    class HorarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.cellTextView)
        val textView2: TextView = itemView.findViewById(R.id.textView2)

        fun render(item: HorarioCell, onClickListener: (HorarioCell) -> Unit) {
            // Reset clickability for all items
            itemView.isClickable = false
            itemView.isFocusable = false

            if (item.horarioId == null && !item.esConflicto) {
                itemView.background = null
            } else {
                when {
                    item.esConflicto -> {
                        itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.horario_conflicto))
                        itemView.isClickable = true
                        itemView.isFocusable = true
                    }
                    item.estado -> {
                        itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.horario_activo))
                        itemView.isClickable = true
                        itemView.isFocusable = true
                    }
                    item.estado == false -> {
                        itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.horario_inactivo))
                        itemView.isClickable = true
                        itemView.isFocusable = true
                    }
                    else -> {
                        itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.horario_default))
                        itemView.isClickable = true
                        itemView.isFocusable = true
                    }
                }
            }

            if (itemView.isClickable) {
                itemView.setOnClickListener { onClickListener(item) }
            } else {
                itemView.setOnClickListener(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tem_horario_cell, parent, false)
        return HorarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: HorarioViewHolder, position: Int) {
        val cell = celdas[position]

        // Reset all view properties first
        holder.textView.text = ""
        holder.textView2.text = ""
        holder.textView.textSize = 14F  // Reset to default text size
        holder.itemView.background = null
        holder.itemView.isClickable = false

        // Then apply specific properties
        if (cell.horaInicio == "inicio") {
            holder.textView.text = cell.lugar
        } else if (cell.esConflicto) {
            holder.textView.text = ". . ."
            holder.textView.textSize = 20F
            holder.render(cell, onItemSelected)
        } else {
            holder.textView.text = cell.nombreEscuela ?: ""
            holder.textView2.text = cell.nombreGrupo ?: ""
            holder.render(cell, onItemSelected)
        }
    }

    override fun getItemCount(): Int = celdas.size
}