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
    private lateinit var spinnerCargo: Spinner// Inicializar luego

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ahora el contexto está disponible, podemos inicializar CommonDAO aquí
        commonDAO = CommonDAO(requireContext())

        initSpinnerCargo(view)
        setupButton(view)
    }

    private fun initSpinnerCargo(view: View) {
        spinnerCargo = view.findViewById(R.id.spinner_cargo)
        val cargos = listOf("coordinador", "mentor", "mentoriado")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cargos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCargo.adapter = adapter
    }

    private fun setupButton(view: View) {
        val btnCargar: Button = view.findViewById(R.id.btn_cargar)
        btnCargar.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Solicitar el permiso
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PICK_EXCEL_REQUEST_CODE)
            } else {
                openFilePicker() // Llama al selector de archivos si ya tienes el permiso
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // Tipo MIME para archivos .xlsx
        }
        startActivityForResult(intent, PICK_EXCEL_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PICK_EXCEL_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFilePicker() // Abre el selector de archivos si el permiso fue concedido
            } else {
                // Maneja la negación del permiso
                if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Si el usuario ha elegido "No preguntar de nuevo", redirigir a la configuración
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_EXCEL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Procesar el archivo Excel usando el URI
                readExcelFile(uri)
            }
        }
    }

    // Diccionario para las escuelas
    private val escuelaMap = mapOf(
        "derecho" to 1,
        "software" to 2,
        "psicología" to 3,
        "administración" to 4,
        "comunicación" to 5,
        "comercial" to 6,
        "arquitectura" to 7,
        "industrial" to 8
    )

    // Función para convertir números a romanos
    private fun convertirARomano(numero: Int): String? {
        val romanos = listOf(
            Pair(12, "XII"),
            Pair(11, "XI"),
            Pair(10, "X"),
            Pair(9, "IX"),
            Pair(8, "VIII"),
            Pair(7, "VII"),
            Pair(6, "VI"),
            Pair(5, "V"),
            Pair(4, "IV"),
            Pair(3, "III"),
            Pair(2, "II"),
            Pair(1, "I")
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

    private fun readExcelFile(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri).use { inputStream ->
                val workbook = XSSFWorkbook(inputStream)
                val sheet = workbook.getSheetAt(0)

                var escuelaActual: Int? = null
                var semestreActual: String? = null

                // Palabras clave para detectar filas de cabeceras
                val palabrasClaveCabecera = listOf("DNI", "NOMBRES", "APELLIDOS", "CELULAR", "CORREO")

                for (row in sheet) {
                    // Extraer los valores de la fila actual
                    val filaDatos = mutableListOf<String>()
                    for (cell in row) {
                        val cellValue = when (cell.cellType) {
                            CellType.STRING -> cell.stringCellValue
                            CellType.NUMERIC -> cell.numericCellValue.toString()
                            CellType.BLANK -> "" // Si la celda está en blanco
                            else -> "Tipo desconocido"
                        }
                        filaDatos.add(cellValue)
                    }

                    // Comprobar si la fila contiene palabras clave de cabecera
                    if (filaDatos.any { it.uppercase() in palabrasClaveCabecera }) {
                        Log.d("ExcelData", "Fila de cabecera ignorada: ${filaDatos.joinToString(" | ")}")
                        continue // Saltar esta fila porque es una cabecera
                    }

                    val datosFila = filaDatos.joinToString(" | ")
                    Log.d("ExcelData", "Fila ${row.rowNum}: $datosFila")

                    // Identificar la carrera
                    if (escuelaMap.containsKey(filaDatos[0].lowercase())) {
                        escuelaActual = escuelaMap[filaDatos[0].lowercase()]
                        Log.d("ExcelData", "Escuela actual: ${filaDatos[0]}")
                        continue
                    }

                    // Identificar el semestre
                    if (filaDatos[0].startsWith("SEMESTRE", true)) {
                        val numeroSemestre = filaDatos[0].split(" ")[1].toIntOrNull()
                        semestreActual = numeroSemestre?.let { convertirARomano(it) }
                        Log.d("ExcelData", "Semestre actual: $semestreActual")
                        continue
                    }
                    // Identificar el tipo
                    val tipoUsuarioSeleccionado = spinnerCargo.selectedItem.toString()

                    // Si es una fila de datos de estudiante
                    if (filaDatos.size >= 6 && escuelaActual != null && semestreActual != null) {
                        val usuario = Usuario(
                            dniUsuario = filaDatos[1],
                            nombreUsuario = filaDatos[3],
                            apellidoUsuario = filaDatos[2],
                            celularUsuario = filaDatos[5],
                            passwordHash = "123456",
                            escuelaId = escuelaActual,
                            semestre = semestreActual,
                            email = filaDatos[4],
                            tipoUsuario = tipoUsuarioSeleccionado,
                            creadoEn = "null"
                        )
                        usuarios.add(usuario)
                        Log.d("ExcelData", "Usuario añadido: $usuario")
                    }
                }

                // Imprimir usuarios procesados
                usuarios.forEach { usuario -> Log.d("UsuarioProcesado", usuario.toString()) }

                // Insertar datos leídos en la base de datos
                lifecycleScope.launch {
                    usuarios.forEach { usuario ->
                        commonDAO.createUser(usuario) // Llamada a la función suspend
                    }
                }

                workbook.close()
            }
        } catch (e: Exception) {
            Log.e("ExcelError", "Error al leer el archivo: ${e.message}")
            Toast.makeText(requireContext(), "Error al leer el archivo. Intenta nuevamente.", Toast.LENGTH_SHORT).show()
        }
    }
}
