package org.kimp.mustep.rest

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MuStepServiceBuilder {
    fun build() : MuStepService {
        return Retrofit.Builder()
            .baseUrl("https://mu-server.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MuStepService::class.java)
    }
}