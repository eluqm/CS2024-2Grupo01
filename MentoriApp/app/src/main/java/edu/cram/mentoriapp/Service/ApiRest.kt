package edu.cram.mentoriapp.Service

import edu.cram.mentoriapp.Model.Cities
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

}