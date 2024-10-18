package edu.cram.mentoriapp.Service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


//.baseUrl("http://10.0.2.2:8095") // Cambia por la URL de tu servidor Ktor
object RetrofitClient {
    fun makeRetrofitClient(): ApiRest {
        return Retrofit.Builder()
            .baseUrl("http://192.168.116.128:8095") // Cambia por la URL de tu servidor Ktor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiRest::class.java)
    }
}