package edu.cram.mentoriapp.Service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



object RetrofitClient {
    fun makeRetrofitClient(): ApiRest {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080") // Cambia por la URL de tu servidor Ktor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiRest::class.java)
    }
}