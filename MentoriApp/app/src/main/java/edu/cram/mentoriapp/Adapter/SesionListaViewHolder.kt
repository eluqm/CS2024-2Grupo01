package edu.cram.mentoriapp.Adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.SesionMentoriaLista
import edu.cram.mentoriapp.R

class SesionListaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val viewtemaSesion = itemView.findViewById<TextView>(R.id.temaSesion)
    private val viewlugar = itemView.findViewById<TextView>(R.id.lugar)
    private val viewfechaRegistrada = itemView.findViewById<TextView>(R.id.fechaRegistrada)
    private val viewnumeroParticipantes = itemView.findViewById<TextView>(R.id.numeroParticipantes)
    private val imgSesion = itemView.findViewById<ImageButton>(R.id.img_usuario)

    @SuppressLint("SetTextI18n")
    fun render(
        item: SesionMentoriaLista,
        onClickListener: (SesionMentoriaLista) -> Unit
    ) {
        viewtemaSesion.text = item.temaSesion
        viewlugar.text = item.lugar
        viewfechaRegistrada.text = "Fecha: " + item.fechaRegistrada
        viewnumeroParticipantes.text = item.participantes

        Glide.with(itemView.context)
            .asBitmap()
            .load(item.foto) // Cargar el ByteArray
            .placeholder(R.drawable.logoula) // Imagen de reemplazo
            .error(R.drawable.uls) // Imagen en caso de error
            .into(imgSesion)


        imgSesion.setOnClickListener {
            showImageDialog(itemView.context, item.foto)
        }
        itemView.setOnClickListener() { onClickListener(item) }
    }

    private fun showImageDialog(context: Context, imageData: ByteArray) {
        // Crear y configurar el Dialog para mostrar la imagen
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_image) // Usar un layout personalizado para la imagen ampliada
        val imageView = dialog.findViewById<ImageView>(R.id.dialogImageView)

        // Cargar la imagen en el ImageView
        Glide.with(context)
            .asBitmap()
            .load(imageData)
            .placeholder(R.drawable.logoula)
            .into(imageView)

        dialog.show()
    }
}