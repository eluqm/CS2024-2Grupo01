package edu.cram.mentoriapp.Adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import edu.cram.mentoriapp.Model.HorarioDetalles

class OpcionesAdapter(
    private val context: Context,
    private val eventos: List<HorarioDetalles>
) : BaseAdapter() {

    override fun getCount(): Int = eventos.size

    override fun getItem(position: Int): Any = eventos[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)

        val evento = getItem(position) as HorarioDetalles
        val textView = itemView.findViewById<TextView>(android.R.id.text1)
        textView.text = "${evento.nombreEscuela} - ${evento.nombreGrupo}"

        // Aplicar el color de fondo según el estado
        val color = when (evento.estado) {
            true -> Color.parseColor("#C8F6C8") // Verde
            false -> Color.parseColor("#FFC8C8") // Rojo
            else -> Color.parseColor("#C8DFFF") // Azul
        }
        itemView.setBackgroundColor(color)

        return itemView
    }
}
