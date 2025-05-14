package edu.cram.mentoriapp.Service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


//.baseUrl("http://10.0.2.2:8095") // Cambia por la URL de tu servidor Ktor
// AZURE: 104.41.57.147
object RetrofitClient {
    fun makeRetrofitClient(): ApiRest {
        return Retrofit.Builder()
            .baseUrl("https://e6c0-190-119-187-251.ngrok-free.app ") // Cambia por la URL de tu servidor Ktor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiRest::class.java)
    }
}