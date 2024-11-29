package com.example.plugins

import com.example.service.*
import com.example.service.Escuela
import com.example.service.EvaluacionComentario
import com.example.service.MiembroGrupo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*

fun Application.configureDatabases() {
    val dbConnection: Connection = connectToPostgres(embedded = true)
    val cityService = CityService(dbConnection)

    routing {

        // Create city
        post("/cities") {
            val city = call.receive<City>()
            val id = cityService.create(city)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read city
        get("/cities/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val city = cityService.read(id)
                call.respond(HttpStatusCode.OK, city)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update city
        put("/cities/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = call.receive<City>()
            cityService.update(id, user)
            call.respond(HttpStatusCode.OK)
        }

        // Delete city
        delete("/cities/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            cityService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }

    val usuariosService = UsuariosService(dbConnection)
    routing {
        // Create user
        post("/usuarios") {
            val usuarios = call.receive<Usuarios>()
            val id = usuariosService.create(usuarios)
            call.respond(HttpStatusCode.Created, id)
        }

        get("/usuarios/dni/{dni}") {
            val dni = call.parameters["dni"] ?: throw IllegalArgumentException("Invalid DNI")
            try {
                val usuarios = usuariosService.readByDni(dni)
                call.respond(HttpStatusCode.OK, usuarios)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            }
        }
        // Read user
        get("/usuarios/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val usuarios = usuariosService.read(id)
            if (usuarios != null) {
                call.respond(HttpStatusCode.OK, usuarios)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update user
        put("/usuarios/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val usuarios = call.receive<Usuarios>()
            usuariosService.update(id, usuarios)
            call.respond(HttpStatusCode.OK)
        }

        // Delete user
        delete("/usuarios/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            usuariosService.delete(id)
            call.respond(HttpStatusCode.OK)
        }

        get("/usuarios/tipo/{tipo}") {
            val tipo = call.parameters["tipo"] ?: throw IllegalArgumentException("Invalid user type")

            // Llama al servicio para obtener los usuarios por tipo
            val usuarios = usuariosService.readByType(tipo)

            if (usuarios.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, usuarios) // Responde con la lista de usuarios
            } else {
                call.respond(HttpStatusCode.NotFound, "No users found for type: $tipo")
            }
        }

        get("/usuarios/tipo/{tipo}/escuela/{escuelaId}") {
            val tipo = call.parameters["tipo"] ?: throw IllegalArgumentException("Invalid user type")
            val escuelaId = call.parameters["escuelaId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid school ID")

            // Llama al servicio para obtener los usuarios por tipo y escuela
            val usuarios = usuariosService.findUsuariosByTypeAndSchool(tipo, escuelaId)

            if (usuarios.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, usuarios) // Responde con la lista de usuarios
            } else {
                call.respond(HttpStatusCode.NotFound, "No users found for type: $tipo in school ID: $escuelaId")
            }
        }

        get("/usuarios/exist/{dni}") {
            val dniUsuario = call.parameters["dni"] ?: throw IllegalArgumentException("DNI missing")
            val exists = usuariosService.userExistsByDni(dniUsuario)
            val response = UserExistResponse(exists)
            // Siempre responde con HttpStatusCode.OK
            call.respond(HttpStatusCode.OK, response)
        }





    }

    val escuelasService = EscuelasService(dbConnection)
    routing {
        // Create school
        post("/escuelas") {
            val escuela = call.receive<Escuela>()
            val id = escuelasService.create(escuela)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read school
        get("/escuelas/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val escuela = escuelasService.read(id)
            call.respond(HttpStatusCode.OK, escuela)
        }

        // Update school
        put("/escuelas/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val escuela = call.receive<Escuela>()
            escuelasService.update(id, escuela)
            call.respond(HttpStatusCode.OK)
        }

        // Delete school
        delete("/escuelas/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            escuelasService.delete(id)
            call.respond(HttpStatusCode.OK)
        }

        // Read all schools
        get("/escuelas") {
            val escuelas = escuelasService.readAll() // Llama al método que obtiene todas las escuelas
            if (escuelas.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, escuelas) // Responde con una lista de escuelas
            } else {
                call.respond(HttpStatusCode.NoContent) // Responde con No Content si no hay escuelas
            }
        }


    }

    val psicologiaService = PsicologiaService(dbConnection)
    routing {
        // Create psychology
        post("/psicologia") {
            val psicologia = call.receive<Psicologia>()
            val id = psicologiaService.create(psicologia)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read psychology
        get("/psicologia/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val psicologia = psicologiaService.read(id)
            if (psicologia != null) {
                call.respond(HttpStatusCode.OK, psicologia)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update psychology
        put("/psicologia/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val psicologia = call.receive<Psicologia>()
            psicologiaService.update(id, psicologia)
            call.respond(HttpStatusCode.OK)
        }

        // Delete psychology
        delete("/psicologia/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            psicologiaService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }

    val coordinadoresService = CoordinadoresService(dbConnection)
    routing {
        // Create coordinator
        post("/coordinadores") {
            val coordinador = call.receive<Coordinador>()
            val id = coordinadoresService.create(coordinador)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read coordinator
        get("/coordinadores/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val coordinador = coordinadoresService.read(id)
            if (coordinador != null) {
                call.respond(HttpStatusCode.OK, coordinador)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update coordinator
        put("/coordinadores/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val coordinador = call.receive<Coordinador>()
            coordinadoresService.update(id, coordinador)
            call.respond(HttpStatusCode.OK)
        }

        // Delete coordinator
        delete("/coordinadores/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            coordinadoresService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
    val mentoresService = MentoresService(dbConnection)
    routing {
        // Create mentor
        post("/mentores") {
            val mentor = call.receive<Mentor>()
            val id = mentoresService.create(mentor)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read mentor
        get("/mentores/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val mentor = mentoresService.read(id)
            if (mentor != null) {
                call.respond(HttpStatusCode.OK, mentor)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update mentor
        put("/mentores/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val mentor = call.receive<Mentor>()
            mentoresService.update(id, mentor)
            call.respond(HttpStatusCode.OK)
        }

        // Delete mentor
        delete("/mentores/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            mentoresService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
    val mentoriadosService = MentoriadosService(dbConnection)
    routing {
        // Create mentoriado
        post("/mentoriados") {
            val mentoriado = call.receive<Mentoriado>()
            val id = mentoriadosService.create(mentoriado)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read mentoriado
        get("/mentoriados/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val mentoriado = mentoriadosService.read(id)
            if (mentoriado != null) {
                call.respond(HttpStatusCode.OK, mentoriado)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update mentoriado
        put("/mentoriados/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val mentoriado = call.receive<Mentoriado>()
            mentoriadosService.update(id, mentoriado)
            call.respond(HttpStatusCode.OK)
        }

        // Delete mentoriado
        delete("/mentoriados/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            mentoriadosService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
    val horariosService = HorariosService(dbConnection)
    routing {
        // Create schedule
        // Create schedule
        post("/horarios") {
            val horario = call.receive<Horario>()
            val id = horariosService.create(horario)
            call.respond(HttpStatusCode.Created, id)
        }

        post("/horarios2") {
            try {
                // Recibir el cuerpo del horario y el jefeId desde la solicitud
                val horario = call.receive<Horario>()
                val jefeId = call.request.queryParameters["jefeId"]?.toIntOrNull()

                // Validar que jefeId no sea nulo
                if (jefeId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Missing or invalid jefeId")
                    return@post
                }

                // Crear el horario y asociarlo al grupo correspondiente
                val horarioId = horariosService.create2(horario, jefeId)

                // Responder con el ID del horario creado
                call.respond(HttpStatusCode.Created, mapOf("horarioId" to horarioId))
            } catch (e: Exception) {
                // Manejo de errores
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        // Read schedule
        get("/horarios/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val horario = horariosService.read(id)
            if (horario != null) {
                call.respond(HttpStatusCode.OK, horario)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/horarios") {
            val horarios = horariosService.readAll() // Llama al método que devuelve todos los horarios
            if (horarios.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, horarios)
            } else {
                call.respond(HttpStatusCode.NoContent) // Responde con 204 si no hay horarios
            }
        }


        // Update schedule
        /*put("/horarios/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val horario = call.receive<Horario>()
            horariosService.update(id, horario)
            call.respond(HttpStatusCode.OK)
        }*/
        put("/horariosPut/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")

            // Recibir los datos parciales para la actualización
            val horarioUpdate = call.receive<HorarioUpdate>()

            // Validar que el ID del URL y el ID del cuerpo coincidan
            if (id != horarioUpdate.horarioId) {
                call.respond(HttpStatusCode.BadRequest, "ID mismatch between path and body")
                return@put
            }

            // Llamar al servicio para actualizar los datos
            horariosService.updatePartial(horarioUpdate)

            // Responder con un OK si todo fue bien
            call.respond(HttpStatusCode.OK)
        }


        // Delete schedule
        delete("/horarios/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            horariosService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
    val miembrosGrupoService = MiembrosGrupoService(dbConnection)
    routing {
        // Create group member
        post("/miembros_grupo") {
            val miembro = call.receive<MiembroGrupo>()
            val id = miembrosGrupoService.create(miembro)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read group member
        get("/miembros_grupo/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val miembro = miembrosGrupoService.read(id)
            if (miembro != null) {
                call.respond(HttpStatusCode.OK, miembro)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update group member
        put("/miembros_grupo/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val miembro = call.receive<MiembroGrupo>()
            miembrosGrupoService.update(id, miembro)
            call.respond(HttpStatusCode.OK)
        }

        // Delete group member
        delete("/miembros_grupo/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            miembrosGrupoService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
    val sesionesMentoriaService = SesionesMentoriaService(dbConnection)
    routing {
        // Create mentoring session
        post("/sesiones_mentoria") {
            val sesion = call.receive<SesionMentoria>()
            val id = sesionesMentoriaService.create(sesion)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read mentoring session
        get("/sesiones_mentoria/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val sesion = sesionesMentoriaService.read(id)
            if (sesion != null) {
                call.respond(HttpStatusCode.OK, sesion)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update mentoring session
        put("/sesiones_mentoria/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val sesion = call.receive<SesionMentoria>()
            sesionesMentoriaService.update(id, sesion)
            call.respond(HttpStatusCode.OK)
        }

        // Delete mentoring session
        delete("/sesiones_mentoria/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            sesionesMentoriaService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
    val gruposService = GruposService(dbConnection)
    routing {
        // Create group
        post("/grupos") {
            val grupo = call.receive<Grupo>()
            val id = gruposService.create(grupo)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read group
        get("/grupos/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val grupo = gruposService.read(id)
            call.respond(HttpStatusCode.OK, grupo)
        }

        // Update group
        put("/grupos/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val grupo = call.receive<Grupo>()
            gruposService.update(id, grupo)
            call.respond(HttpStatusCode.OK)
        }

        // Delete group
        delete("/grupos/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            gruposService.delete(id)
            call.respond(HttpStatusCode.OK)
        }

        get("/grupos/{jefeId}/miembros") {
            val jefeId = call.parameters["jefeId"]?.toInt() ?: throw IllegalArgumentException("Invalid jefeId")
            try {
                val miembros = gruposService.getMiembrosPorJefe(jefeId)
                call.respond(HttpStatusCode.OK, miembros)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }


        get("/grupoId") {
            try {
                // Obtener el parámetro userId de la consulta
                val userId = call.request.queryParameters["userId"]?.toIntOrNull()

                // Validar que userId no sea nulo
                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Missing or invalid userId")
                    return@get
                }

                // Llamar a la función de GruposSchema para obtener el grupo_id
                val grupoId = gruposService.hallarGrupoID(userId)

                // Responder con el grupo_id
                call.respond(HttpStatusCode.OK, mapOf("grupoId" to grupoId))
            } catch (e: Exception) {
                // Manejo de errores
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage ?: "Unknown error")
            }
        }

        // Obtener usuarios mentoriados por id Mentor
        get("/grupo_mentoriados/{mentorId}") {
            val mentorId = call.parameters["mentorId"]?.toInt() ?: throw IllegalArgumentException("Invalid grupo ID")
            val mentoriados = gruposService.getUsuariosMentoriadosPorMentor(mentorId)
            call.respond(HttpStatusCode.OK, mentoriados)
        }

        get("/grupos/{jefeId}/sesiones") {
            val jefeId = call.parameters["jefeId"]?.toInt() ?: throw IllegalArgumentException("Invalid Jefe ID")
            try {
                val sesiones = gruposService.getSesionesPorJefe(jefeId)
                if (sesiones.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, sesiones)
                } else {
                    call.respond(HttpStatusCode.NotFound, "No se encontraron sesiones para el jefe con ID proporcionado.")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }


        get("/grupos/escuela/{escuelaId}") {
            val escuelaId = call.parameters["escuelaId"]?.toInt() ?: throw IllegalArgumentException("Invalid Escuela ID")
            try {
                val grupos = gruposService.readAllByEscuelaId(escuelaId)
                if (grupos.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, grupos)
                } else {
                    call.respond(HttpStatusCode.NotFound, "No se encontraron grupos para el escuela_id proporcionado.")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }
    }

    val asistenciasSesionesService = AsistenciasSesionesService(dbConnection)
    routing {
        // Create attendance
        post("/asistencias_sesiones") {
            val asistencia = call.receive<AsistenciaSesion>()
            val id = asistenciasSesionesService.create(asistencia)
            call.respond(HttpStatusCode.Created, id)
        }

        post("/asistencias_sesiones_grupal") {
            // Recibir una lista de asistencias
            val asistencias = call.receive<List<AsistenciaSesion>>()

            // Llamar al servicio para registrar todas las asistencias
            asistencias.forEach { asistencia ->
                asistenciasSesionesService.create(asistencia)
            }

            // Responder con un estado 201 Created
            call.respond(HttpStatusCode.Created)
        }


        // Read attendance
        get("/asistencias_sesiones/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val asistencia = asistenciasSesionesService.read(id)
            if (asistencia != null) {
                call.respond(HttpStatusCode.OK, asistencia)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update attendance
        put("/asistencias_sesiones/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val asistencia = call.receive<AsistenciaSesion>()
            asistenciasSesionesService.update(id, asistencia)
            call.respond(HttpStatusCode.OK)
        }

        // Delete attendance
        delete("/asistencias_sesiones/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            asistenciasSesionesService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }

    val solicitudesMentoriaService = SolicitudesMentoriaService(dbConnection)
    routing {
        // Create mentoring request
        post("/solicitudes_mentoria") {
            val solicitudMentoria = call.receive<SolicitudMentoria>()
            val id = solicitudesMentoriaService.create(solicitudMentoria)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read mentoring request
        get("/solicitudes_mentoria/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val solicitudMentoria = solicitudesMentoriaService.read(id)
            if (solicitudMentoria != null) {
                call.respond(HttpStatusCode.OK, solicitudMentoria)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update mentoring request
        put("/solicitudes_mentoria/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val solicitudMentoria = call.receive<SolicitudMentoria>()
            solicitudesMentoriaService.update(id, solicitudMentoria)
            call.respond(HttpStatusCode.OK)
        }

        // Delete mentoring request
        delete("/solicitudes_mentoria/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            solicitudesMentoriaService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
    val evaluacionesComentariosService = EvaluacionesComentariosService(dbConnection)
    routing {
        // Create evaluation
        post("/evaluaciones_comentarios") {
            val evaluacion = call.receive<EvaluacionComentario>()
            val id = evaluacionesComentariosService.create(evaluacion)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read evaluation
        get("/evaluaciones_comentarios/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val evaluacion = evaluacionesComentariosService.read(id)
            if (evaluacion != null) {
                call.respond(HttpStatusCode.OK, evaluacion)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update evaluation
        put("/evaluaciones_comentarios/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val evaluacion = call.receive<EvaluacionComentario>()
            evaluacionesComentariosService.update(id, evaluacion)
            call.respond(HttpStatusCode.OK)
        }

        // Delete evaluation
        delete("/evaluaciones_comentarios/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            evaluacionesComentariosService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
    val mensajesGrupoService = MensajesGrupoService(dbConnection)
    routing {
        // Create group message
        post("/mensajes_grupo") {
            val mensajeGrupo = call.receive<MensajeGrupo>()
            val id = mensajesGrupoService.create(mensajeGrupo)
            call.respond(HttpStatusCode.Created, id)
        }

        get("/mensajes_usuario/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val chats = mensajesGrupoService.readChatsByUser(id)
            if (chats.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, chats)
            } else {
                call.respond(HttpStatusCode.NotFound, "No chats found for user ID $id")
            }
        }



        // Read group message
        get("/mensajes_grupo/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val mensajeGrupo = mensajesGrupoService.read(id)
            if (mensajeGrupo != null) {
                call.respond(HttpStatusCode.OK, mensajeGrupo)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update group message
        put("/mensajes_grupo/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val mensajeGrupo = call.receive<MensajeGrupo>()
            mensajesGrupoService.update(id, mensajeGrupo)
            call.respond(HttpStatusCode.OK)
        }

        // Delete group message
        delete("/mensajes_grupo/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            mensajesGrupoService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
    val eventosService = EventosService(dbConnection)
    routing {
        // Create event
        post("/eventos") {
            val evento = call.receive<Evento>()
            val id = eventosService.create(evento)
            call.respond(HttpStatusCode.Created, id)
        }

        get("/eventos") {
            try {
                // Llamamos a la función readAll() para obtener todos los eventos
                val eventos = eventosService.readAll()

                // Respondemos con la lista de eventos en formato JSON
                call.respond(eventos)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error al obtener eventos")
            }
        }

        // Read event
        get("/eventos/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val evento = eventosService.read(id)
            if (evento != null) {
                call.respond(HttpStatusCode.OK, evento)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update event
        put("/eventos/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val evento = call.receive<Evento>()
            eventosService.update(id, evento)
            call.respond(HttpStatusCode.OK)
        }

        // Delete event
        delete("/eventos/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            eventosService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
    val notificacionesService = NotificacionesService(dbConnection)
    routing {
        // Create notification
        post("/notificaciones") {
            val notificacion = call.receive<Notificacion>()
            val id = notificacionesService.create(notificacion)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read notification
        get("/notificaciones/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val notificacion = notificacionesService.read(id)
            if (notificacion != null) {
                call.respond(HttpStatusCode.OK, notificacion)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update notification
        put("/notificaciones/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val notificacion = call.receive<Notificacion>()
            notificacionesService.update(id, notificacion)
            call.respond(HttpStatusCode.OK)
        }

        // Delete notification
        delete("/notificaciones/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            notificacionesService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }



}

/**
 * Makes a connection to a Postgres database.
 *
 * In order to connect to your running Postgres process,
 * please specify the following parameters in your configuration file:
 * - postgres.url -- Url of your running database process.
 * - postgres.user -- Username for database connection
 * - postgres.password -- Password for database connection
 *
 * If you don't have a database process running yet, you may need to [download]((https://www.postgresql.org/download/))
 * and install Postgres and follow the instructions [here](https://postgresapp.com/).
 * Then, you would be able to edit your url,  which is usually "jdbc:postgresql://host:port/database", as well as
 * user and password values.
 *
 *
 * @param embedded -- if [true] defaults to an embedded database for tests that runs locally in the same process.
 * In this case you don't have to provide any parameters in configuration file, and you don't have to run a process.
 *
 * @return [Connection] that represent connection to the database. Please, don't forget to close this connection when
 * your application shuts down by calling [Connection.close]
 * */

fun Application.connectToPostgres(embedded: Boolean): Connection {
    Class.forName("org.postgresql.Driver")
    if (embedded) {
        return DriverManager.getConnection("jdbc:postgresql://serverikus.postgres.database.azure.com:5432/mentoriapp", "foxi", "ola.cmma.bd1")
    } else {
        val url = environment.config.property("postgres.url").getString()
        val user = environment.config.property("postgres.user").getString()
        val password = environment.config.property("postgres.password").getString()

        return DriverManager.getConnection(url, user, password)
    }
}