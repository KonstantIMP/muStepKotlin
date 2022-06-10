package org.kimp.mustep.rest

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MuStepServiceBuilder {
    fun build() : MuStepService {
        return Retrofit.Builder()
            .baseUrl("https://mu-server.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(700, TimeUnit.MILLISECONDS)
                    .build()
            )
            .build()
            .create(MuStepService::class.java)
    }
}