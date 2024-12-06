import android.graphics.Color
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
        val textView2: TextView = itemView.findViewById(R.id.textView2)

        fun render(item: HorarioCell, onClickListener:(HorarioCell) -> Unit) {
            if (item.horarioId == null) {
                itemView.background = null
            } else {
                when (item.estado) {
                    false -> {
                        itemView.setBackgroundColor(Color.parseColor("#FFC8C8")) // Rojo suave (pastel)
                        itemView.isClickable = true // Permitir clics
                    }
                    null -> {
                        itemView.setBackgroundColor(Color.parseColor("#C8DFFF")) // Azul suave (pastel)
                        itemView.isClickable = true // Permitir clics
                    }
                    true -> {
                        itemView.setBackgroundColor(Color.parseColor("#C8F6C8")) // Verde suave (pastel)
                        itemView.isEnabled = true // Desactivar clics
                    }
                }

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
            if (cell.horaInicio == "inicio") {
                holder.textView.text = cell.lugar

            } else {
                holder.textView.text =
                    cell.nombreEscuela ?: "" // Mostrar el lugar o vacío si no hay evento
                holder.textView2.text = cell.nombreGrupo ?: ""
                holder.render(cell, onItemSelected)
            }
    }

    override fun getItemCount(): Int = celdas.size
}
