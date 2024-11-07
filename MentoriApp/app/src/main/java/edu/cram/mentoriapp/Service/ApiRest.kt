package edu.cram.mentoriapp.Service

import edu.cram.mentoriapp.Model.AsistenciaSesion
import edu.cram.mentoriapp.Model.Cities
import edu.cram.mentoriapp.Model.Coordinador
import edu.cram.mentoriapp.Model.Escuela
import edu.cram.mentoriapp.Model.EvaluacionComentario
import edu.cram.mentoriapp.Model.Evento
import edu.cram.mentoriapp.Model.GrupoMentoria
import edu.cram.mentoriapp.Model.Horario
import edu.cram.mentoriapp.Model.MensajeGrupo
import edu.cram.mentoriapp.Model.Mentor
import edu.cram.mentoriapp.Model.Mentoriado
import edu.cram.mentoriapp.Model.MiembroGrupo
import edu.cram.mentoriapp.Model.Notificacion
import edu.cram.mentoriapp.Model.Psicologia
import edu.cram.mentoriapp.Model.SesionMentoria
import edu.cram.mentoriapp.Model.SesionMentoriaLista
import edu.cram.mentoriapp.Model.SolicitudMentoria
import edu.cram.mentoriapp.Model.UserExistResponse
import edu.cram.mentoriapp.Model.Usuario
import edu.cram.mentoriapp.Model.UsuarioLista
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiRest {
    @GET("/cities/{id}")
    suspend fun getCity(@Path("id") id: Int): retrofit2.Response<Cities>

    @POST("/cities")
    suspend fun createCity(@Body city: Cities): retrofit2.Response<Int>

    @PUT("/cities/{id}") // Actualizar ciudad por ID
    suspend fun updateCity(@Path("id") id: Int, @Body city: Cities): retrofit2.Response<Unit>

    @DELETE("/cities/{id}") // Eliminar ciudad por ID
    suspend fun deleteCity(@Path("id") id: Int): retrofit2.Response<Unit>
    // Escuelas
    @GET("/escuelas/{id}")
    suspend fun getEscuela(@Path("id") id: Int): retrofit2.Response<Escuela>

    @POST("/escuelas")
    suspend fun createEscuela(@Body escuela: Escuela): retrofit2.Response<Int>

    @PUT("/escuelas/{id}")
    suspend fun updateEscuela(@Path("id") id: Int, @Body escuela: Escuela): retrofit2.Response<Unit>

    @DELETE("/escuelas/{id}")
    suspend fun deleteEscuela(@Path("id") id: Int): retrofit2.Response<Unit>

    @GET("/escuelas")
    suspend fun getEscuelas(): retrofit2.Response<List<Escuela>>

    //Usuarios
    @GET("/usuarios/dni/{dni}")
    suspend fun getUsuarioByDni(@Path("dni") dni: String): retrofit2.Response<Usuario>

    @GET("/usuarios/{id}")
    suspend fun getUsuario(@Path("id") id: Int): retrofit2.Response<Usuario>

    @GET("/usuarios/exist/{dni}")
    suspend fun userExists(@Path("dni") dni: String): retrofit2.Response<UserExistResponse>

    @POST("/usuarios")
    suspend fun createUsuario(@Body usuario: Usuario): retrofit2.Response<Int>

    @PUT("/usuarios/{id}")
    suspend fun updateUsuario(@Path("id") id: Int, @Body usuario: Usuario): retrofit2.Response<Unit>

    @DELETE("/usuarios/{id}")
    suspend fun deleteUsuario(@Path("id") id: Int): retrofit2.Response<Unit>

    @GET("/usuarios/tipo/{tipo}")
    suspend fun getUsuariosByType(@Path("tipo") tipo: String): retrofit2.Response<List<Usuario>>

    @GET("/usuarios/tipo/{tipo}/escuela/{escuelaId}")
    suspend fun findUsuariosByTypeAndSchool(
        @Path("tipo") tipo: String,
        @Path("escuelaId") escuelaId: Int
    ): retrofit2.Response<List<Usuario>>

    //Psicologia
    @GET("/psicologia/{id}")
    suspend fun getPsicologia(@Path("id") id: Int): retrofit2.Response<Psicologia>

    @POST("/psicologia")
    suspend fun createPsicologia(@Body psicologia: Psicologia): retrofit2.Response<Int>

    @PUT("/psicologia/{id}")
    suspend fun updatePsicologia(@Path("id") id: Int, @Body psicologia: Psicologia): retrofit2.Response<Unit>

    @DELETE("/psicologia/{id}")
    suspend fun deletePsicologia(@Path("id") id: Int): retrofit2.Response<Unit>
    //Coordinadores
    @GET("/coordinadores/{id}")
    suspend fun getCoordinador(@Path("id") id: Int): retrofit2.Response<Coordinador>

    @POST("/coordinadores")
    suspend fun createCoordinador(@Body coordinador: Coordinador): retrofit2.Response<Int>

    @PUT("/coordinadores/{id}")
    suspend fun updateCoordinador(@Path("id") id: Int, @Body coordinador: Coordinador): retrofit2.Response<Unit>

    @DELETE("/coordinadores/{id}")
    suspend fun deleteCoordinador(@Path("id") id: Int): retrofit2.Response<Unit>
    //Mentores
    @GET("/mentores/{id}")
    suspend fun getMentor(@Path("id") id: Int): retrofit2.Response<Mentor>

    @POST("/mentores")
    suspend fun createMentor(@Body mentor: Mentor): retrofit2.Response<Int>

    @PUT("/mentores/{id}")
    suspend fun updateMentor(@Path("id") id: Int, @Body mentor: Mentor): retrofit2.Response<Unit>

    @DELETE("/mentores/{id}")
    suspend fun deleteMentor(@Path("id") id: Int): retrofit2.Response<Unit>
    //Mentoriados
    @GET("/mentoriados/{id}")
    suspend fun getMentoriado(@Path("id") id: Int): retrofit2.Response<Mentoriado>

    @POST("/mentoriados")
    suspend fun createMentoriado(@Body mentoriado: Mentoriado): retrofit2.Response<Int>

    @PUT("/mentoriados/{id}")
    suspend fun updateMentoriado(@Path("id") id: Int, @Body mentoriado: Mentoriado): retrofit2.Response<Unit>

    @DELETE("/mentoriados/{id}")
    suspend fun deleteMentoriado(@Path("id") id: Int): retrofit2.Response<Unit>
    //horarios
    @GET("/horarios/{id}")
    suspend fun getHorario(@Path("id") id: Int): retrofit2.Response<Horario>

    @POST("/horarios")
    suspend fun createHorario(@Body horario: Horario): retrofit2.Response<Int>

    @PUT("/horarios/{id}")
    suspend fun updateHorario(@Path("id") id: Int, @Body horario: Horario): retrofit2.Response<Unit>

    @DELETE("/horarios/{id}")
    suspend fun deleteHorario(@Path("id") id: Int): retrofit2.Response<Unit>
//grupos
    @GET("/grupo_mentoriados/{mentorId}")
    suspend fun getUsuariosMentoriadosPorMentor(@Path("mentorId") mentorId: Int): Response<List<UsuarioLista>>

    @GET("grupos/{jefeId}/sesiones")
    suspend fun getSesionesPorJefe(@Path("jefeId") jefeId: Int): Response<List<SesionMentoriaLista>>

    @GET("/grupos/{id}")
    suspend fun getGrupo(@Path("id") id: Int): retrofit2.Response<GrupoMentoria>

    @POST("/grupos")
    suspend fun createGrupo(@Body grupo: GrupoMentoria): retrofit2.Response<Int>

    @PUT("/grupos/{id}")
    suspend fun updateGrupo(@Path("id") id: Int, @Body grupo: GrupoMentoria): retrofit2.Response<Unit>

    @DELETE("/grupos/{id}")
    suspend fun deleteGrupo(@Path("id") id: Int): retrofit2.Response<Unit>

    @GET("/grupos/escuela/{escuelaId}")
    suspend fun getGrupoByEscuela(@Path("escuelaId") escuelaId: Int): retrofit2.Response<List<GrupoMentoria>>



    //Mienbros_Grupo
    @GET("/miembros_grupo/{id}")
    suspend fun getMiembroGrupo(@Path("id") id: Int): retrofit2.Response<MiembroGrupo>

    @POST("/miembros_grupo")
    suspend fun createMiembroGrupo(@Body miembroGrupo: MiembroGrupo): retrofit2.Response<Int>

    @PUT("/miembros_grupo/{id}")
    suspend fun updateMiembroGrupo(@Path("id") id: Int, @Body miembroGrupo: MiembroGrupo): retrofit2.Response<Unit>

    @DELETE("/miembros_grupo/{id}")
    suspend fun deleteMiembroGrupo(@Path("id") id: Int): retrofit2.Response<Unit>
    //Sesiones_Mentoria
    @GET("/sesiones_mentoria/{id}")
    suspend fun getSesionMentoria(@Path("id") id: Int): retrofit2.Response<SesionMentoria>

    @POST("/sesiones_mentoria")
    suspend fun createSesionMentoria(@Body sesionMentoria: SesionMentoria): retrofit2.Response<Int>

    @PUT("/sesiones_mentoria/{id}")
    suspend fun updateSesionMentoria(@Path("id") id: Int, @Body sesionMentoria: SesionMentoria): retrofit2.Response<Unit>

    @DELETE("/sesiones_mentoria/{id}")
    suspend fun deleteSesionMentoria(@Path("id") id: Int): retrofit2.Response<Unit>
    //Asistemcias_Sesiones
    @GET("/asistencias_sesiones/{id}")
    suspend fun getAsistenciaSesion(@Path("id") id: Int): retrofit2.Response<AsistenciaSesion>

    @POST("/asistencias_sesiones")
    suspend fun createAsistenciaSesion(@Body asistencia: AsistenciaSesion): retrofit2.Response<Int>

    @PUT("/asistencias_sesiones/{id}")
    suspend fun updateAsistenciaSesion(@Path("id") id: Int, @Body asistencia: AsistenciaSesion): retrofit2.Response<Unit>

    @DELETE("/asistencias_sesiones/{id}")
    suspend fun deleteAsistenciaSesion(@Path("id") id: Int): retrofit2.Response<Unit>
    //Solicitud_Mentoria
    @GET("/solicitudes_mentoria/{id}")
    suspend fun getSolicitudMentoria(@Path("id") id: Int): retrofit2.Response<SolicitudMentoria>

    @POST("/solicitudes_mentoria")
    suspend fun createSolicitudMentoria(@Body solicitudMentoria: SolicitudMentoria): retrofit2.Response<Int>

    @PUT("/solicitudes_mentoria/{id}")
    suspend fun updateSolicitudMentoria(@Path("id") id: Int, @Body solicitudMentoria: SolicitudMentoria): retrofit2.Response<Unit>

    @DELETE("/solicitudes_mentoria/{id}")
    suspend fun deleteSolicitudMentoria(@Path("id") id: Int): retrofit2.Response<Unit>
    //Evaluacion_Comentario
    @GET("/evaluaciones_comentarios/{id}")
    suspend fun getEvaluacionComentario(@Path("id") id: Int): retrofit2.Response<EvaluacionComentario>

    @POST("/evaluaciones_comentarios")
    suspend fun createEvaluacionComentario(@Body evaluacion: EvaluacionComentario): retrofit2.Response<Int>

    @PUT("/evaluaciones_comentarios/{id}")
    suspend fun updateEvaluacionComentario(@Path("id") id: Int, @Body evaluacion: EvaluacionComentario): retrofit2.Response<Unit>

    @DELETE("/evaluaciones_comentarios/{id}")
    suspend fun deleteEvaluacionComentario(@Path("id") id: Int): retrofit2.Response<Unit>
    //Mensajes_Grupo
    @GET("/mensajes_grupo/{id}")
    suspend fun getMensajeGrupo(@Path("id") id: Int): retrofit2.Response<MensajeGrupo>

    @POST("/mensajes_grupo")
    suspend fun createMensajeGrupo(@Body mensaje: MensajeGrupo): retrofit2.Response<Int>

    @PUT("/mensajes_grupo/{id}")
    suspend fun updateMensajeGrupo(@Path("id") id: Int, @Body mensaje: MensajeGrupo): retrofit2.Response<Unit>

    @DELETE("/mensajes_grupo/{id}")
    suspend fun deleteMensajeGrupo(@Path("id") id: Int): retrofit2.Response<Unit>
    //Eventos
    @GET("/eventos/{id}")
    suspend fun getEvento(@Path("id") id: Int): retrofit2.Response<Evento>

    @POST("/eventos")
    suspend fun createEvento(@Body evento: Evento): retrofit2.Response<Int>

    @PUT("/eventos/{id}")
    suspend fun updateEvento(@Path("id") id: Int, @Body evento: Evento): retrofit2.Response<Unit>

    @DELETE("/eventos/{id}")
    suspend fun deleteEvento(@Path("id") id: Int): retrofit2.Response<Unit>
    //Notificaciones
    @GET("/notificaciones/{id}")
    suspend fun getNotificacion(@Path("id") id: Int): retrofit2.Response<Notificacion>

    @POST("/notificaciones")
    suspend fun createNotificacion(@Body notificacion: Notificacion): retrofit2.Response<Int>

    @PUT("/notificaciones/{id}")
    suspend fun updateNotificacion(@Path("id") id: Int, @Body notificacion: Notificacion): retrofit2.Response<Unit>

    @DELETE("/notificaciones/{id}")
    suspend fun deleteNotificacion(@Path("id") id: Int): retrofit2.Response<Unit>
}