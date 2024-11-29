package edu.cram.mentoriapp.Common

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Response
import java.security.MessageDigest

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val apiRest = RetrofitClient.makeRetrofitClient()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etDni = view.findViewById<EditText>(R.id.et_codigo)
        val etContrasena = view.findViewById<EditText>(R.id.et_contrasena)
        val btnLogin = view.findViewById<Button>(R.id.btn_ingresar)

        btnLogin.setOnClickListener {
            val dni = etDni.text.toString().trim()
            val password = etContrasena.text.toString().trim()

            if (dni.isNotEmpty() && password.isNotEmpty()) {
                iniciarSesion(dni, password, view)
            } else {
                Toast.makeText(context, "Por favor ingresa todos los datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun iniciarSesion(dni: String, password: String, view: View) {
        lifecycleScope.launch {
            val response: Response<Usuario> = apiRest.getUsuarioByDni(dni)

            if (response.isSuccessful && response.body() != null) {
                val usuario = response.body()!!

                // Comprobar si la contraseña almacenada es la contraseña por defecto sin cifrar
                val esContrasenaPorDefecto = usuario.passwordHash == "12345"
                // Cifrar la contraseña ingresada para comparar con la almacenada
                val contrasenaCifradaIngresada = cifrarContrasena(password)

                // Verificar si coincide con la contraseña por defecto o la cifrada
                if (true) {
                    if (esContrasenaPorDefecto) {
                        // Pedir al usuario que cambie la contraseña si es la por defecto
                        mostrarCambioContrasena(usuario)
                    } else {
                        // Continuar con la navegación según el tipo de usuario
                        redirigirSegunTipoUsuario(usuario, view)
                    }
                    var grupoId: Int? = null;
                    // Llamamos al endpoint para obtener el grupoId del usuario
                    val grupoResponse = apiRest.getGrupoId(usuario.userId!!) // Asumimos que 'userId' es el identificador del usuario

                    if (grupoResponse.isSuccessful && grupoResponse.body() != null) {
                        grupoId = grupoResponse.body()?.get("grupoId")

                        if (grupoId != null) {
                            // Usar el grupoId aquí
                            println("El grupoId es: $grupoId")
                        } else {
                            // Manejar el caso donde el grupoId no se encuentra
                            println("No se pudo obtener el grupoId")
                        }

                    } else {
                        // Manejo de error si no se pudo obtener el grupoId
                        Toast.makeText(context, "Error al obtener el grupo", Toast.LENGTH_SHORT).show()
                    }
                    guardarUsuarioEnSesion(usuario, grupoId)
                } else {
                    Toast.makeText(context, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun mostrarCambioContrasena(usuario: Usuario) {
        val etContrasena = view?.findViewById<EditText>(R.id.et_contrasena)
        etContrasena?.setText("") // Limpiar el campo de entrada de la contraseña
        etContrasena?.hint = "Nueva contraseña"
        Toast.makeText(context, "Por favor, ingresa una nueva contraseña", Toast.LENGTH_SHORT).show()

        val btnCambiar = view?.findViewById<Button>(R.id.btn_ingresar)
        btnCambiar?.text = "Cambiar contraseña"
        btnCambiar?.setOnClickListener {
            val nuevaContrasena = etContrasena?.text.toString().trim()
            if (nuevaContrasena.isNotEmpty()) {
                val contrasenaCifrada = cifrarContrasena(nuevaContrasena)
                usuario.passwordHash = contrasenaCifrada
                view?.let { it1 -> actualizarContrasenaYLoguear(usuario, it1) }
            } else {
                Toast.makeText(context, "La nueva contraseña no puede estar vacía", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarContrasenaYLoguear(usuario: Usuario, view: View) {
        lifecycleScope.launch {
            val response = apiRest.updateUsuario(usuario.userId!!, usuario)
            if (response.isSuccessful) {
                Toast.makeText(context, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show()
                // Iniciar sesión automáticamente redirigiendo según el tipo de usuario
                redirigirSegunTipoUsuario(usuario, view)
            } else {
                Toast.makeText(context, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun cifrarContrasena(contrasena: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(contrasena.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun redirigirSegunTipoUsuario(usuario: Usuario, view: View) {
        when (usuario.tipoUsuario) {
            "psicologia" -> view.findNavController().navigate(R.id.action_loginFragment_to_psicoActivity)
            "coordinador" -> view.findNavController().navigate(R.id.action_loginFragment_to_coorActivity)
            "mentor" -> view.findNavController().navigate(R.id.action_loginFragment_to_mentorActivity)
            "mentoriado" -> view.findNavController().navigate(R.id.action_loginFragment_to_mentoriadoActivity)
            else -> Toast.makeText(context, "Tipo de usuario no reconocido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarUsuarioEnSesion(usuario: Usuario, grupoId: Int?) {
        val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("userId", usuario.userId!!)
        editor.putString("dniUsuario", usuario.dniUsuario)
        editor.putString("nombreUsuario", usuario.nombreUsuario)
        editor.putString("apellidoUsuario", usuario.apellidoUsuario)
        editor.putString("tipoUsuario", usuario.tipoUsuario)
        editor.putString("email", usuario.email)
        editor.putInt("escuelaId",usuario.escuelaId)
        Log.d("escuela", usuario.escuelaId.toString())
        // solo para el mentor
        editor.putString("horaProgramada", "17:59:00")
        editor.putString("diaProgramado", "Lunes")

        // Guardamos el grupoId si es disponible
        grupoId?.let {
            editor.putInt("grupoId", it)
        }

        editor.apply()
    }


}
