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
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.R
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class PsicoCargaDatosFragment : Fragment(R.layout.fragment_psico_carga_datos) {
    private val PICK_EXCEL_REQUEST_CODE = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSpinnerCargo(view)
        setupButton(view)
    }

    private fun initSpinnerCargo(view: View) {
        val spinnerCargo: Spinner = view.findViewById(R.id.spinner_cargo)
        val cargos = listOf("Coordinadores", "Mentores", "Mentoriados")
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
                // Maneja la negación del permiso (puedes mostrar un mensaje al usuario)
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
                // Aquí puedes procesar el archivo Excel usando el URI
                readExcelFile(uri)
            }
        }
    }

    private fun readExcelFile(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri).use { inputStream ->
                val workbook = XSSFWorkbook(inputStream)
                val sheet = workbook.getSheetAt(0)

                for (row in sheet) {
                    // Saltar la primera fila si contiene encabezados
                    if (row.rowNum == 0) continue

                    // Crea un arreglo para almacenar los datos de la fila
                    val filaDatos = mutableListOf<String>()

                    // Itera a través de las celdas de la fila
                    for (cell in row) {
                        val cellValue = when (cell.cellType) {
                            CellType.STRING -> cell.stringCellValue
                            CellType.NUMERIC -> cell.numericCellValue.toString()
                            CellType.BLANK -> "" // Si la celda está en blanco
                            else -> "Tipo desconocido"
                        }
                        filaDatos.add(cellValue)
                    }

                    // Imprime los datos de la fila en el log
                    Log.d("ExcelData", "Fila ${row.rowNum}: ${filaDatos.joinToString(" | ")}")
                }
                workbook.close()
            }
        } catch (e: Exception) {
            Log.e("ExcelError", "Error al leer el archivo: ${e.message}")
            Toast.makeText(requireContext(), "Error al leer el archivo. Intenta nuevamente.", Toast.LENGTH_SHORT).show()
        }
    }

}
