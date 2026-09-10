package com.linhchay.thaphuonggiatien

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.linhchay.thaphuonggiatien.data.local.AppDatabase
import com.linhchay.thaphuonggiatien.data.local.entities.EventEntity
import com.linhchay.thaphuonggiatien.data.worker.SyncEventWorker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPref = application.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    private val eventDao = AppDatabase.getDatabase(application).eventDao()
    
    private val _gold = MutableLiveData<Int>()
    val gold: LiveData<Int> = _gold

    init {
        _gold.value = sharedPref.getInt("gold", 0)
    }

    fun updateGold(amount: Int): Boolean {
        val currentGold = _gold.value ?: 0
        if (currentGold + amount < 0) return false
        
        val newGold = currentGold + amount
        sharedPref.edit().putInt("gold", newGold).apply()
        _gold.value = newGold
        return true
    }
    
    fun addInitialGold() {
        val isFirstLaunch = sharedPref.getBoolean("is_first_launch", true)
        val lastLoginDate = sharedPref.getString("last_login_date", "")
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        if (isFirstLaunch) {
            // Lần đầu mở app: cộng 100 gold và khởi tạo ngày lễ
            updateGold(100)
            initializeCommonEvents()
            
            sharedPref.edit()
                .putBoolean("is_first_launch", false)
                .putString("last_login_date", currentDate)
                .apply()
        } else if (lastLoginDate != currentDate) {
            // Mỗi ngày tiếp theo: cộng 30 gold
            updateGold(30)
            sharedPref.edit()
                .putString("last_login_date", currentDate)
                .apply()
        }
    }

    private fun initializeCommonEvents() {
        // Cấu trúc: Tên, Ngày, Tháng, Loại
        val initialEvents = listOf(
            Triple("Giỗ tổ Hùng Vương", 10, 3) to EventEntity.TYPE_SYSTEM,
            Triple("Rằm Tháng 7", 15, 7) to EventEntity.TYPE_SYSTEM,
            Triple("Tết Trung Thu", 15, 8) to EventEntity.TYPE_SYSTEM,
            Triple("Rằm Tháng 9", 15, 9) to EventEntity.TYPE_SYSTEM,
            Triple("Rằm Tháng 10", 15, 10) to EventEntity.TYPE_SYSTEM,
            Triple("Rằm Tháng 11", 15, 11) to EventEntity.TYPE_SYSTEM,
            Triple("Rằm Tháng 12", 15, 12) to EventEntity.TYPE_SYSTEM,
            Triple("Tết Nguyên Đán", 1, 1) to EventEntity.TYPE_SYSTEM,
            Triple("Giỗ Bà Nội", 16, 8) to EventEntity.TYPE_USER
        )

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        viewModelScope.launch {
            initialEvents.forEach { (info, type) ->
                val (name, day, month) = info
                val entity = EventEntity(
                    name = name,
                    solarDate = "Đang đồng bộ...",
                    lunarDate = "$day/$month/$currentYear (Âm lịch)",
                    type = type
                )
                val id = eventDao.insertEvent(entity).toInt()
                scheduleSyncWorker(id, name, day, month, currentYear, type)
            }
        }
    }

    private fun scheduleSyncWorker(eventId: Int, name: String, day: Int, month: Int, year: Int, type: String) {
        val data = Data.Builder()
            .putInt("event_id", eventId)
            .putString("name", name)
            .putInt("day", day)
            .putInt("month", month)
            .putInt("year", year)
            .putString("type", type)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncEventWorker>()
            .setInputData(data)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(getApplication()).enqueue(syncRequest)
    }
}