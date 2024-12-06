package edu.cram.mentoriapp.Psicologia

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import edu.cram.mentoriapp.DAO.CommonDAO
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.R
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class PsicoCargaDatosFragment : Fragment(R.layout.fragment_psico_carga_datos) {
    private val PICK_EXCEL_REQUEST_CODE = 1
    private val usuarios = mutableListOf<Usuario>()

    private lateinit var commonDAO: CommonDAO
    private lateinit var spinnerCargo: Spinner

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commonDAO = CommonDAO(requireContext())

        initSpinnerCargo(view)
        setupButton(view)
    }


    private fun initSpinnerCargo(view: View) {
        spinnerCargo = view.findViewById(R.id.spinner_cargo)
        val cargos = listOf("Coordinador", "Mentor", "Mentoriado")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, cargos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCargo.adapter = adapter
    }


    private fun setupButton(view: View) {
        val btnCargar: Button = view.findViewById(R.id.btn_cargar)
        btnCargar.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PICK_EXCEL_REQUEST_CODE)
            } else {
                openFilePicker()
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }
        startActivityForResult(intent, PICK_EXCEL_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PICK_EXCEL_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openFilePicker()
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(requireContext(), "Permiso denegado. Ve a la configuración para habilitarlo.", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Permiso denegado. No se puede acceder al almacenamiento.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_EXCEL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri -> readExcelFile(uri) }
        }
    }

    private fun readExcelFile(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri).use { inputStream ->
                val workbook = XSSFWorkbook(inputStream)
                val sheet = workbook.getSheetAt(0)

                var escuelaActual: Int? = null
                var semestreActual: String? = null
                val palabrasClaveCabecera = listOf("DNI", "NOMBRES", "APELLIDOS", "CELULAR", "CORREO")

                for (row in sheet) {
                    val filaDatos = mutableListOf<String>()
                    for (cell in row) {
                        val cellValue = when (cell.cellType) {
                            CellType.STRING -> cell.stringCellValue
                            CellType.NUMERIC -> cell.numericCellValue.toInt().toString() // Convertir números sin decimales
                            CellType.BLANK -> ""
                            else -> "Tipo desconocido"
                        }
                        filaDatos.add(cellValue)
                    }

                    if (filaDatos.any { it.uppercase() in palabrasClaveCabecera }) {
                        continue
                    }

                    if (escuelaMap.containsKey(filaDatos[0].lowercase())) {
                        escuelaActual = escuelaMap[filaDatos[0].lowercase()]
                        continue
                    }

                    if (filaDatos[0].startsWith("SEMESTRE", true)) {
                        val numeroSemestre = filaDatos[0].split(" ")[1].toIntOrNull()
                        semestreActual = numeroSemestre?.let { convertirARomano(it) }
                        continue
                    }

                    val tipoUsuarioSeleccionado = spinnerCargo.selectedItem.toString()
                    if (filaDatos.size >= 6 && escuelaActual != null && semestreActual != null) {
                        val usuario = Usuario(
                            dniUsuario = filaDatos[1],
                            nombreUsuario = filaDatos[3],
                            apellidoUsuario = filaDatos[2],
                            celularUsuario = filaDatos[5],
                            passwordHash = "12345",
                            escuelaId = escuelaActual,
                            semestre = semestreActual,
                            email = filaDatos[4],
                            tipoUsuario = tipoUsuarioSeleccionado,
                            creadoEn = "null"
                        )
                        usuarios.add(usuario)
                    }
                }

                lifecycleScope.launch {
                    for (usuario in usuarios) {
                        try {
                            if (commonDAO.userExists(usuario.dniUsuario)) {
                                showDialog("Error", "El DNI ${usuario.dniUsuario} ya existe en la base de datos.")
                                break
                            } else {
                                commonDAO.createUser(usuario)
                            }
                        } catch (e: Exception) {
                            showDialog("Error", "Error al insertar el usuario: ${e.message}")
                            break
                        }
                    }
                }
                workbook.close()
            }
        } catch (e: Exception) {
            Log.e("ExcelError", "Error al leer el archivo: ${e.message}")
            Toast.makeText(requireContext(), "Error al leer el archivo. Intenta nuevamente.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDialog(title: String, message: String) {
        // Aquí se puede implementar un diálogo para mostrar los errores al usuario
        Toast.makeText(requireContext(), "$title: $message", Toast.LENGTH_LONG).show()
    }

    // Convertir número a romano
    private fun convertirARomano(numero: Int): String? {
        val romanos = listOf(
            Pair(12, "XII"), Pair(11, "XI"), Pair(10, "X"),
            Pair(9, "IX"), Pair(8, "VIII"), Pair(7, "VII"),
            Pair(6, "VI"), Pair(5, "V"), Pair(4, "IV"),
            Pair(3, "III"), Pair(2, "II"), Pair(1, "I")
        )
        var num = numero
        val result = StringBuilder()
        for (par in romanos) {
            while (num >= par.first) {
                result.append(par.second)
                num -= par.first
            }
        }
        return if (result.isEmpty()) null else result.toString()
    }

    private val escuelaMap = mapOf(
        "derecho" to 1, "software" to 2, "psicología" to 3,
        "administración" to 4, "comunicación" to 5, "comercial" to 6,
        "arquitectura" to 7, "industrial" to 8
    )
}