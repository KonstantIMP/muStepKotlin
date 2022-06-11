package org.kimp.mustep.rest

import org.kimp.mustep.domain.Floor
import org.kimp.mustep.domain.University
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MuStepService {
    @GET("university/available")
    fun getAvailableUniversities(@Query("onlyOfficial") onlyOfficial: Boolean = false) : Call<List<University>>

    @GET("university/{uni}/data")
    fun getUniversityData(@Path("uni") uni: String) : Call<List<Floor>>
}
