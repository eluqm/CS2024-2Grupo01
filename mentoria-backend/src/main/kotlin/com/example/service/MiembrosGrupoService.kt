package com.example.service

import com.example.DAO.MiembrosGrupoDAO
import com.example.model.MiembroGrupo
import java.sql.Connection

class MiembrosGrupoService(connection: Connection) {
    private val miembroDAO = MiembrosGrupoDAO(connection)

    suspend fun create(miembro: MiembroGrupo): Int {
        return miembroDAO.create(miembro)
    }

    suspend fun read(id: Int): MiembroGrupo? {
        return try {
            miembroDAO.read(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: Int, miembro: MiembroGrupo) {
        miembroDAO.update(id, miembro)
    }

    suspend fun delete(id: Int) {
        miembroDAO.delete(id)
    }
}
