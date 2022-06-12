package org.kimp.mustep.rest

import org.kimp.mustep.domain.Floor
import org.kimp.mustep.domain.University
import org.kimp.mustep.domain.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MuStepService {
    @GET("university/available")
    fun getAvailableUniversities(@Query("onlyOfficial") onlyOfficial: Boolean = false) : Call<List<University>>

    @GET("university/{uni}/data")
    fun getUniversityData(@Path("uni") uni: String) : Call<List<Floor>>

    @POST("user/new")
    fun newUser(@Body user: User) : Call<User>

    @GET("user/{uid}")
    fun getUser(@Path("uid") uid: String) : Call<User>

    @POST("user/{uid}/avatar")
    fun updateAvatar(@Path("uid") uid: String, @Query("path") avatarPath: String) : Call<Boolean>
}
