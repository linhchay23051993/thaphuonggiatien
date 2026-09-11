package com.linhchay.thaphuonggiatien.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import com.linhchay.thaphuonggiatien.data.local.AppDatabase
import com.linhchay.thaphuonggiatien.data.local.entities.EventEntity
import com.linhchay.thaphuonggiatien.utils.LunarSolarConverter
import java.text.SimpleDateFormat
import java.util.Locale

class SyncEventWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        val eventId = inputData.getInt("event_id", -1)
        val name = inputData.getString("name") ?: return ListenableWorker.Result.failure()
        val day = inputData.getInt("day", -1)
        val month = inputData.getInt("month", -1)
        val year = inputData.getInt("year", -1)

        val type = inputData.getString("type") ?: EventEntity.TYPE_USER

        if (day == -1 || month == -1 || year == -1) return ListenableWorker.Result.failure()

        val eventDao = AppDatabase.getDatabase(applicationContext).eventDao()

        return try {
            // Chuyển đổi offline bằng thuật toán Hồ Ngọc Đức
            val solarDate = LunarSolarConverter.convertLunar2Solar(day, month, year)
            val solarDateStr = LunarSolarConverter.formatSolarDate(solarDate)
            val solarDateParsed = try {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }.parse(solarDateStr)
            } catch (e: Exception) {
                null
            }
            val eventEntity = EventEntity(
                id = if (eventId != -1) eventId else 0,
                name = name,
                solarDate = solarDateStr,
                lunarDate = "$day/$month/$year (Âm lịch)",
                eventDate = solarDateParsed?.time ?: 0L,
                type = type
            )
            
            if (eventId != -1) {
                eventDao.updateEvent(eventEntity)
            } else {
                eventDao.insertEvent(eventEntity)
            }
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            ListenableWorker.Result.failure()
        }
    }
}
