package org.kimp.mustep.rest

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object MuStepServiceBuilder {
    fun build(): MuStepService {
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
