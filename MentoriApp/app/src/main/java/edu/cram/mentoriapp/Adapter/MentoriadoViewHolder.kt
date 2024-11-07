import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cram.mentoriapp.Model.UsuarioLista
import edu.cram.mentoriapp.R

class MentoriadoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val viewNombre = itemView.findViewById<TextView>(R.id.txt_name_user)
    private val viewCorreo = itemView.findViewById<TextView>(R.id.correo)
    private val viewNumero = itemView.findViewById<TextView>(R.id.numero)
    private val viewDni = itemView.findViewById<TextView>(R.id.dni)
    private val imgUsuario = itemView.findViewById<ImageButton>(R.id.img_usuario)
    @SuppressLint("SetTextI18n")
    fun render(
        item: UsuarioLista,
        onClickListener: (UsuarioLista) -> Unit
    ) {
        viewCorreo.text = item.email
        viewNombre.text = item.nombreCompletoUsuario
        viewNumero.text = item.celularUsuario
        viewDni.text = item.dniUsuario

        itemView.setOnClickListener() { onClickListener(item) }
    }
}
