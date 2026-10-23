package com.linhchay.thaphuonggiatien

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.linhchay.thaphuonggiatien.data.local.AppDatabase
import com.linhchay.thaphuonggiatien.data.local.entities.EventEntity
import com.linhchay.thaphuonggiatien.utils.LunarSolarConverter
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
            updateGold(10000)
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
        // Cấu trúc: Tên, Ngày, Tháng → (Loại, iconType)
        // iconType: 0 = icon_lehoi, 1 = icon_ram, -1 = icon_gio (mặc định)
        val initialEvents = listOf(
            Triple("Tết Nguyên Đán", 1, 1) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Ngày vía thần tài", 10, 1) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Rằm tháng Giêng", 15, 1) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Mùng 1 Tháng 2", 1, 2) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Rằm Tháng 2", 15, 2) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Mùng 1 Tháng 3", 1, 3) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Tết Hàn Thực", 3, 3) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Giỗ tổ Hùng Vương", 10, 3) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Rằm Tháng 3", 15, 3) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Mùng 1 Tháng 4", 1, 4) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Rằm Tháng 4 - Lễ Phật Đản", 15, 4) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Mùng 1 Tháng 5", 1, 5) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Tết Đoan Ngọ", 5, 5) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Rằm Tháng 5", 15, 5) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Mùng 1 Tháng 6", 1, 6) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Rằm Tháng 6", 15, 6) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Mùng 1 Tháng 7", 1, 7) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Lễ Thất Tịch", 7, 7) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Rằm tháng 7 - Lễ Vu Lan", 15, 7) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Mùng 1 Tháng 8", 1, 8) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Tết Trung Thu", 15, 8) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Mùng 1 Tháng 9", 1, 9) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Tết Trùng Cửu", 9, 9) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Rằm Tháng 9", 15, 9) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Mùng 1 Tháng 10", 1, 10) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Rằm Tháng 10", 15, 10) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Mùng 1 Tháng 11", 1, 11) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Rằm Tháng 11", 15, 11) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Mùng 1 Tháng 12", 1, 12) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Rằm Tháng 12", 15, 12) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Ông Công ông Táo", 23, 12) to Pair(EventEntity.TYPE_SYSTEM, 1),
            Triple("Đêm Giao Thừa", 30, 12) to Pair(EventEntity.TYPE_SYSTEM, 1)
        )

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        viewModelScope.launch {
            initialEvents.forEach { (info, meta) ->
                val (name, day, month) = info
                val (type, iconType) = meta
                try {
                    // Chuyển đổi offline bằng thuật toán Hồ Ngọc Đức
                    val solarDate = LunarSolarConverter.convertLunar2Solar(day, month, currentYear)
                    val solarDateStr = LunarSolarConverter.formatSolarDate(solarDate)
                    val solarDateParsed = try {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .apply { isLenient = false }
                            .parse(solarDateStr)
                    } catch (e: Exception) {
                        null
                    }
                    val entity = EventEntity(
                        name = name,
                        solarDate = solarDateStr,
                        lunarDate = "$day/$month/$currentYear (Âm lịch)",
                        eventDate = solarDateParsed?.time ?: 0L,
                        type = type,
                        iconType = iconType
                    )
                    eventDao.insertEvent(entity)
                } catch (e: Exception) {
                    val entity = EventEntity(
                        name = name,
                        solarDate = "Lỗi chuyển đổi",
                        lunarDate = "$day/$month/$currentYear (Âm lịch)",
                        type = type,
                        iconType = iconType
                    )
                    eventDao.insertEvent(entity)
                }
            }
        }
    }
}