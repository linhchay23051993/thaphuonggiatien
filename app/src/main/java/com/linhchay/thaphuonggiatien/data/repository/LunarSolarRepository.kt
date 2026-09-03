package com.linhchay.thaphuonggiatien.data.repository

import com.linhchay.thaphuonggiatien.data.model.HuyenMinhResponse
import com.linhchay.thaphuonggiatien.data.model.LunarDateRequest
import com.linhchay.thaphuonggiatien.data.model.SolarDateResponse
import com.linhchay.thaphuonggiatien.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LunarSolarRepository {
    private val apiService = RetrofitClient.instance

    suspend fun convertLunarToSolar(day: Int, month: Int, year: Int, isLeap: Boolean = false): Result<SolarDateResponse?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.convertLunarToSolar(LunarDateRequest(day, month, year, isLeap))
                if (response.isSuccessful) {
                    Result.success(response.body())
                } else {
                    Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getLunarDate(day: Int, month: Int, year: Int): Result<HuyenMinhResponse?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLunarDate(day, month, year)
                if (response.isSuccessful) {
                    Result.success(response.body())
                } else {
                    Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
