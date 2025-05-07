package edu.cram.mentoriapp.Common

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.R
import edu.cram.mentoriapp.Service.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.security.MessageDigest

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val apiRest = RetrofitClient.makeRetrofitClient()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Verificamos la conexión a la base de datos primero
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response: Response<Boolean> = apiRest.checkDatabaseConnection()
                if (!response.isSuccessful || response.body() != true) {
                    // Si no hay conexión o hay error, mostramos el diálogo y no continuamos
                    showConnectionErrorDialog()
                } else {
                    // Solo si hay conexión, procedemos con la lógica normal
                    proceedWithLoginLogic(view)
                }
            } catch (e: Exception) {
                // Si hay una excepción (ej. no hay red), mostramos el diálogo
                showConnectionErrorDialog()
            }
        }
    }

    private fun proceedWithLoginLogic(view: View) {
        if (existeSesionIniciada()) {
            val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
            val tipoUsuario = sharedPreferences.getString("tipoUsuario", "")
            redirigirSegunTipoUsuario(tipoUsuario!!, view)
        } else {
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
    }

    private fun showConnectionErrorDialog() {
        try {
            if (isAdded && activity != null) { // Verifica si el Fragmento está adjunto
                AlertDialog.Builder(requireContext())
                    .setTitle("Error de conexión")
                    .setMessage("No se pudo establecer la conexión. Por favor, verifica tu conexión e intenta de nuevo.")
                    .setPositiveButton("Salir") { _, _ ->
                        requireActivity().finish() // Cierra la Activity
                    }
                    .setCancelable(false)
                    .show()
            } else {
                Log.e("TuFragmento", "No se puede mostrar el diálogo: Fragmento no adjunto")
            }
        } catch (e: Exception) {
            Log.e("TuFragmento", "Error al mostrar el diálogo: ${e.message}")
        }
    }

    private fun existeSesionIniciada(): Boolean {
        val sharedPreferences = requireActivity().getSharedPreferences("usuarioSesion", android.content.Context.MODE_PRIVATE)
        val usuarioSesion = sharedPreferences.getInt("userId", -1) // Cambia "userId" por la clave correcta

        return usuarioSesion != -1 // Retorna true si userId tiene un valor válido, false en caso contrario
    }


    private fun iniciarSesion(dni: String, password: String, view: View) {
        lifecycleScope.launch {
            try {
                val response = apiRest.getUsuarioByDni(dni)

                if (response.isSuccessful && response.body() != null) {
                    val usuario = response.body()!!

                    val esContrasenaPorDefecto = usuario.passwordHash == "12345"
                    val contrasenaCifradaIngresada = cifrarContrasena(password)

                    // Verificar si la contraseña coincide
                    if (usuario.passwordHash == contrasenaCifradaIngresada || esContrasenaPorDefecto) {

                        // Obtener grupoId del usuario
                        val grupoResponse = apiRest.getGrupoId(usuario.userId!!)
                        if (grupoResponse.isSuccessful && grupoResponse.body() != null) {
                            val grupoId = grupoResponse.body()?.get("grupoId") as? Int
                            if (grupoId != null) {
                                println("El grupoId es: $grupoId")
                                guardarUsuarioEnSesion(usuario, grupoId)
                            } else {
                                Toast.makeText(context, "No se pudo obtener el grupoId", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Error al obtener el grupo", Toast.LENGTH_SHORT).show()
                        }


                        if (esContrasenaPorDefecto) {
                            mostrarCambioContrasena(usuario)
                        } else {
                            redirigirSegunTipoUsuario(usuario.tipoUsuario, view)
                        }


                    } else {
                        Toast.makeText(context, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(context, "Error de red. Verifica tu conexión.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error inesperado: ${e.message}", Toast.LENGTH_SHORT).show()
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
                redirigirSegunTipoUsuario(usuario.tipoUsuario, view)
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

    private fun redirigirSegunTipoUsuario(tipoUsuario: String, view: View) {


        when (tipoUsuario) {
            "psicologia" -> {
                view.findNavController().navigate(R.id.action_loginFragment_to_psicoActivity, null)
                requireActivity().finish()
            }
            "coordinador" -> {
                view.findNavController().navigate(R.id.action_loginFragment_to_coorActivity, null)
                requireActivity().finish()
            }
            "mentor" -> {
                view.findNavController().navigate(R.id.action_loginFragment_to_mentorActivity, null)
                requireActivity().finish()
            }
            "mentoriado" -> {
                view.findNavController().navigate(R.id.action_loginFragment_to_mentoriadoActivity, null)
                requireActivity().finish()
            }
            else -> Toast.makeText(context, "Tipo de usuario no reconocido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarUsuarioEnSesion(usuario: Usuario, grupoId: Int?) {

        val fcmManager = FCMManager(requireContext())
        fcmManager.getAndRegisterToken(usuario.userId!!) { token ->
            token?.let {
                Log.d("LoginFragment", "Token FCM registrado: $it")
                saveTokenLocally(it)
            }
        }

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
        //editor.putString("horaProgramada", "17:59:00")
        //editor.putString("diaProgramado", "Lunes")

        // Guardamos el grupoId si es disponible
        grupoId?.let {
            editor.putInt("grupoId", it)
        }

        editor.apply()
    }

    private fun saveTokenLocally(token: String) {
        val sharedPrefs = requireActivity().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("fcm_token", token).apply()
    }

}
