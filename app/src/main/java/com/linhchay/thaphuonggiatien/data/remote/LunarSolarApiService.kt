package com.linhchay.thaphuonggiatien.data.remote

import com.linhchay.thaphuonggiatien.data.model.HuyenMinhResponse
import com.linhchay.thaphuonggiatien.data.model.LunarDateRequest
import com.linhchay.thaphuonggiatien.data.model.SolarDateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LunarSolarApiService {
    @POST("lunar-to-solar")
    suspend fun convertLunarToSolar(@Body request: LunarDateRequest): Response<SolarDateResponse>

    @GET("api/amlich")
    suspend fun getLunarDate(
        @Query("d") day: Int,
        @Query("m") month: Int,
        @Query("y") year: Int
    ): Response<HuyenMinhResponse>

    companion object {
        const val BASE_URL = "https://huyenminh.com.vn/"
    }
}
