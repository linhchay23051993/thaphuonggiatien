package com.linhchay.thaphuonggiatien.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import com.linhchay.thaphuonggiatien.data.local.AppDatabase
import com.linhchay.thaphuonggiatien.data.local.entities.EventEntity
import com.linhchay.thaphuonggiatien.data.repository.LunarSolarRepository

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

        if (day == -1 || month == -1 || year == -1) return ListenableWorker.Result.failure()

        val repository = LunarSolarRepository()
        val eventDao = AppDatabase.getDatabase(applicationContext).eventDao()

        return try {
            val result = repository.getLunarDate(day, month, year)
            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response != null) {
                    val solarDateStr = response.duongLich
                    val eventEntity = EventEntity(
                        id = if (eventId != -1) eventId else 0,
                        name = name,
                        solarDate = solarDateStr,
                        lunarDate = "$day/$month/$year (Âm lịch)"
                    )
                    
                    if (eventId != -1) {
                        eventDao.updateEvent(eventEntity)
                    } else {
                        eventDao.insertEvent(eventEntity)
                    }
                    ListenableWorker.Result.success()
                } else {
                    ListenableWorker.Result.retry()
                }
            } else {
                ListenableWorker.Result.retry()
            }
        } catch (e: Exception) {
            ListenableWorker.Result.retry()
        }
    }
}
