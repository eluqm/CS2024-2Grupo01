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

/**
 * Configura la conexión a la base de datos y define las rutas de la API relacionadas
 * con los servicios de acceso a datos.
 *
 * Esta función es el punto central de configuración para todos los endpoints de la API
 * que requieren acceso a la base de datos. Establece una conexión a PostgreSQL y define
 * rutas para verificar la disponibilidad de la base de datos.
 *
 * @param Application Receptor de extensión para la aplicación Ktor.
 */
fun Application.configureDatabases() {
    val dbConnection: Connection = connectToPostgres(embedded = true)

    FirebaseAdmin.initialize()

    // Configurar rutas
    routing {
        // Endpoint: GET /tokens/user/{userId}
        get("/tokens/user/{userId}") {
            val userIdParam = call.parameters["userId"] ?: run {
                call.respond(HttpStatusCode.BadRequest, TokenResponse(success = false, message = "User ID es requerido"))
                return@get
            }

            val userId = userIdParam.toIntOrNull() ?: run {
                call.respond(HttpStatusCode.BadRequest, TokenResponse(success = false, message = "User ID inválido"))
                return@get
            }

            val deviceTokenService = DeviceTokenService(dbConnection)
            val tokens = deviceTokenService.getTokensByUserId(userId)

            if (tokens.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, TokenResponse(success = false, message = "No se encontraron tokens para este usuario"))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("success" to true, "tokens" to tokens))
            }
        }
        // Endpoint para registrar un dispositivo
        post("/register-device") {
            val registration = call.receive<DeviceRegistration>()

            val userId = registration.userId ?: run {
                call.respond(HttpStatusCode.BadRequest, TokenResponse(success = false, message = "User ID es requerido"))
                return@post
            }

            val token = registration.token

            // Guardar en BD
            val deviceTokenService = DeviceTokenService(dbConnection)
            val success = deviceTokenService.registerToken(userId, token)

            if (success) {
                call.respond(HttpStatusCode.OK, TokenResponse(success = true, message = "Token registrado/actualizado"))
            } else {
                call.respond(HttpStatusCode.InternalServerError, TokenResponse(success = false, message = "Error al guardar el token"))
            }
        }

        // Endpoint para enviar notificación a un dispositivo específico
        post("/send-notification") {
            val request = call.receive<NotificationRequest>()

            // Crear servicio FCM
            val fcmService = FCMService()

            try {
                // Enviar notificación
                val result = withContext(Dispatchers.IO) {
                    fcmService.sendMessageToDevice(
                        request.token,
                        request.title,
                        request.body
                    )
                }

                call.respond(HttpStatusCode.OK, NotificationResponse(success = true, messageId = result))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    NotificationResponse(success = false, messageId = e.message)
                )
            }
        }

        // Endpoint: POST /notify-user
        post("/notify-user") {
            val request = call.receive<NotificationRequest>()
            val userId = request.token.toIntOrNull() // Aquí puedes cambiar esto por otro campo si prefieres

            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, NotificationResponse(success = false, messageId = "Invalid user ID"))
                return@post
            }

            val deviceTokenService = DeviceTokenService(dbConnection)
            val tokens = deviceTokenService.getTokensByUserId(userId)

            if (tokens.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, NotificationResponse(success = false, messageId = "No tokens found for this user"))
                return@post
            }

            val fcmService = FCMService()
            var allSuccess = true
            var lastMessageId: String? = null

            for (token in tokens) {
                try {
                    val messageId = withContext(Dispatchers.IO) {
                        fcmService.sendMessageToDevice(token, request.title, request.body)
                    }
                    lastMessageId = messageId
                } catch (e: Exception) {
                    allSuccess = false
                    e.printStackTrace()
                }
            }

            if (allSuccess && lastMessageId != null) {
                call.respond(HttpStatusCode.OK, NotificationResponse(success = true, messageId = lastMessageId))
            } else {
                call.respond(HttpStatusCode.OK, NotificationResponse(success = false, messageId = lastMessageId))
            }
        }
    }

    //Rutas generales
    routing {
        /**
         * Endpoint para verificar la disponibilidad de la conexión a la base de datos.
         *
         * GET /db-check
         * @return Boolean - true si la conexión está activa, false en caso contrario.
         */
        get("/db-check") {
            call.respond(dbConnection.isValid(2))
        }
    }

    val tokensService = TokensService(dbConnection)

    routing {
        get("/tokens/psicologia") {
            try {
                val tokens = tokensService.getTokensPsicologia()
                call.respond(HttpStatusCode.OK, tokens)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }
        //jefes-mentores
        get("/tokens/horario/{id}") {
            val horarioId = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("ID de horario inválido")
            try {
                val tokens = tokensService.getTokensByHorario(horarioId)
                call.respond(HttpStatusCode.OK, tokens)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }
        //mentoriados
        get("/tokens/grupo/horario/{horarioId}") {
            try {
                val horarioId = call.parameters["horarioId"]?.toIntOrNull()
                if (horarioId == null) {
                    call.respond(HttpStatusCode.BadRequest, "El parámetro horarioId es requerido y debe ser un número")
                    return@get
                }

                val tokens = tokensService.getTokensByGrupoHorario(horarioId)
                call.respond(HttpStatusCode.OK, tokens)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message.toString())
            }
        }

    }

    val usuariosService = UsuariosService(dbConnection)
    routing {

        // Get all users
        get("/usuarios") {
            val usuarios = usuariosService.getAll()
            call.respond(HttpStatusCode.OK, usuarios)
        }


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


        get("/usuarios/tipo/{tipo}/escuela/{escuelaId}/semestre/{semestre}") {
            val tipo = call.parameters["tipo"] ?: throw IllegalArgumentException("Invalid user type")
            val escuelaId = call.parameters["escuelaId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid school ID")
            val semestre = call.parameters["semestre"] ?: throw IllegalArgumentException("Invalid semester")

            // Llama al servicio para obtener los usuarios por tipo y escuela
            val usuarios = usuariosService.findUsuariosByTypeAndSchoolAndSemester(tipo, escuelaId, semestre)

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

        get("/mentoresByGrupo/{grupoId}") {
            val id = call.parameters["grupoId"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val mentor = mentoresService.readMentorByGroupID(id)
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

        // Endpoint para obtener el horario por grupoId
        get("/horarios/grupo/{grupoId}") {
            val grupoId = call.parameters["grupoId"]?.toInt() ?: throw IllegalArgumentException("Invalid grupoId")
            try {
                val horario = horariosService.readHorarioByGrupo(grupoId)
                call.respond(HttpStatusCode.OK, horario)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, "Horario not found for grupoId: $grupoId")
            }
        }



        // Update schedule
        put("/horarios/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val horario = call.receive<Horario>()
            horariosService.update(id, horario)
            call.respond(HttpStatusCode.OK)
        }
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

        // Get all sessions
        get("/sesiones_mentoria") {

            val sesiones = sesionesMentoriaService.readAll()
            if (sesiones.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, sesiones)
            }else {
                call.respond(HttpStatusCode.NoContent, "No se encontraron sesiones")
            }
        }

        // Read mentoring session
        get("/sesiones_mentoria/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val sesion = sesionesMentoriaService.read(id)
                call.respond(HttpStatusCode.OK, sesion)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Sesión no encontrada"))
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

        get("/sesiones_mentoria/existe/{grupoId}") {
            val grupoId = call.parameters["grupoId"]?.toIntOrNull()

            if (grupoId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "El grupoId debe ser un número válido"))
                return@get
            }

            val existe = sesionesMentoriaService.existeSesionHoy(grupoId)
            call.respond(mapOf("existe" to existe))  // Devuelve { "existe": true/false }
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

        // Get all groups
        get("/grupos") {

            val grupos = gruposService.getAllGrupos()
            if (grupos.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, grupos)
            } else {
                call.respond(HttpStatusCode.NoContent, "No se encontraron grupos")
            }
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

        // Get all attendances
        get("/asistencias_sesiones") {
            val asistencias = asistenciasSesionesService.readAll()
            if (asistencias.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, asistencias)
            } else {
                call.respond(HttpStatusCode.NoContent, "No se encontraron asistencias")
            }
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

        get("/readAllEventosreadAllEventos") {
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
        // Conexión a AWS RDS con nombre de BD explícito
        return DriverManager.getConnection(
            "jdbc:postgresql://mentoriapp.cwlak0wa0zo6.us-east-1.rds.amazonaws.com:5432/mentoriapp",
            "postgresito",
            "2GEENv4pmGbna4OGUWRA"
        )
    } else {
        // Conexión configurable (para diferentes entornos)
        val url = environment.config.property("postgres.url").getString()
        val user = environment.config.property("postgres.user").getString()
        val password = environment.config.property("postgres.password").getString()

        return DriverManager.getConnection(url, user, password)
    }
}