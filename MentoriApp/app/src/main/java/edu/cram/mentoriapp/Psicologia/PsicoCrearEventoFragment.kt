package edu.cram.mentoriapp.Psicologia

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import edu.cram.mentoriapp.DAO.CommonDAO
import edu.cram.mentoriapp.Model.Evento
import edu.cram.mentoriapp.R

class PsicoCrearEventoFragment : Fragment(R.layout.fragment_psico_crear_evento) {

    private lateinit var commonDAO: CommonDAO

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa CommonDAO
        commonDAO = CommonDAO(requireContext())

        // Encuentra el botón para crear evento
        view.findViewById<Button>(R.id.boton_crear_evento).setOnClickListener {
            // Mostrar el diálogo
            CrearEventoDialog(requireContext(), commonDAO).show(childFragmentManager, "CrearEventoDialog")
        }
    }

}