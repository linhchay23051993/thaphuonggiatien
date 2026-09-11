package com.linhchay.thaphuonggiatien.data.repository

import com.linhchay.thaphuonggiatien.data.model.HuyenMinhResponse
import com.linhchay.thaphuonggiatien.data.model.LunarDateRequest
import com.linhchay.thaphuonggiatien.data.model.SolarDateResponse
import com.linhchay.thaphuonggiatien.data.remote.RetrofitClient
import com.linhchay.thaphuonggiatien.utils.LunarSolarConverter
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

    // ========================================================================
    // Chuyển đổi offline bằng thuật toán Hồ Ngọc Đức
    // ========================================================================

    /**
     * Chuyển đổi âm lịch → dương lịch (OFFLINE, không cần mạng).
     * Sử dụng thuật toán của Hồ Ngọc Đức.
     *
     * @param lunarDay Ngày âm lịch
     * @param lunarMonth Tháng âm lịch
     * @param lunarYear Năm âm lịch
     * @param isLeap Có phải tháng nhuận không
     * @return [LunarSolarConverter.SolarDate] chứa ngày/tháng/năm dương lịch
     */
    fun convertLunarToSolarOffline(
        lunarDay: Int,
        lunarMonth: Int,
        lunarYear: Int,
        isLeap: Boolean = false
    ): LunarSolarConverter.SolarDate {
        return LunarSolarConverter.convertLunar2Solar(lunarDay, lunarMonth, lunarYear, isLeap)
    }

    /**
     * Chuyển đổi dương lịch → âm lịch (OFFLINE, không cần mạng).
     * Sử dụng thuật toán của Hồ Ngọc Đức.
     *
     * @param solarDay Ngày dương lịch
     * @param solarMonth Tháng dương lịch
     * @param solarYear Năm dương lịch
     * @return [LunarSolarConverter.LunarDate] chứa ngày/tháng/năm âm lịch
     */
    fun convertSolarToLunarOffline(
        solarDay: Int,
        solarMonth: Int,
        solarYear: Int
    ): LunarSolarConverter.LunarDate {
        return LunarSolarConverter.convertSolar2Lunar(solarDay, solarMonth, solarYear)
    }
}
