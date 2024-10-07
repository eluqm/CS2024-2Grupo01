package edu.cram.mentoriapp.Common

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import edu.cram.mentoriapp.R

class LoginFragment : Fragment(R.layout.fragment_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnLogin = view.findViewById<Button>(R.id.btn_login)


        btnLogin.setOnClickListener {
            view.findNavController().navigate(R.id.action_loginFragment_to_psicoActivity)
        }
    }
}