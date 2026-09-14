package com.linhchay.thaphuonggiatien.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.linhchay.thaphuonggiatien.data.local.AppDatabase
import com.linhchay.thaphuonggiatien.data.model.AltarItem

class CleanupOfferingsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TWENTY_FOUR_HOURS_MILLIS = 24 * 60 * 60 * 1000L
    }

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val expiryTime = now - TWENTY_FOUR_HOURS_MILLIS

        // 1. Xoá lễ vật hết hạn trong Room DB (Ancestor)
        try {
            val altarDao = AppDatabase.getDatabase(applicationContext).altarDao()
            altarDao.deleteExpiredOfferings(now)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Xoá lễ vật hết hạn trong SharedPreferences (Temple)
        try {
            cleanupTempleOfferings(expiryTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Result.success()
    }

    private fun cleanupTempleOfferings(expiryTime: Long) {
        val prefs = applicationContext.getSharedPreferences("temple_altar_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val type = object : TypeToken<List<AltarItem>>() {}.type
        val editor = prefs.edit()

        // Quét tất cả các temple (id 1-100 để an toàn)
        val allKeys = prefs.all.keys.filter { it.startsWith("temple_items_") }
        for (key in allKeys) {
            val json = prefs.getString(key, null) ?: continue
            val items: List<AltarItem> = try {
                gson.fromJson(json, type)
            } catch (e: Exception) {
                continue
            }

            val filtered = items.filter { item ->
                // Giữ lại nếu không phải offering, hoặc chưa hết hạn
                !item.isOffering || item.placedAt == 0L || item.placedAt > expiryTime
            }

            if (filtered.size != items.size) {
                editor.putString(key, gson.toJson(filtered))
            }
        }
        editor.apply()
    }
}
