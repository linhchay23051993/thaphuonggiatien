package com.linhchay.thaphuonggiatien.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.linhchay.thaphuonggiatien.data.local.AppDatabase
import com.linhchay.thaphuonggiatien.data.model.Event
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val eventDao = AppDatabase.getDatabase(application).eventDao()
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            eventDao.getAllEvents().collectLatest { entities ->
                val now = Calendar.getInstance()
                val currentYear = now.get(Calendar.YEAR)
                val today = now.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val twoMonthsLater = Calendar.getInstance().apply {
                    timeInMillis = today
                    add(Calendar.MONTH, 2)
                }.timeInMillis

                val filteredEvents = entities.filter { entity ->
                    // Giữ lại các mục đang đồng bộ để người dùng biết
                    if (entity.eventDate == 0L) return@filter true
                    
                    val eventCal = Calendar.getInstance().apply { timeInMillis = entity.eventDate }
                    val eventYear = eventCal.get(Calendar.YEAR)

                    // Điều kiện: Trong năm hiện tại AND >= hôm nay AND <= 2 tháng tới
                    eventYear == currentYear && entity.eventDate >= today && entity.eventDate <= twoMonthsLater
                }.map { entity ->
                    Event(
                        id = entity.id,
                        name = entity.name,
                        solarDate = entity.solarDate,
                        lunarDate = entity.lunarDate,
                        eventDate = entity.eventDate,
                        status = calculateStatus(entity.solarDate, today)
                    )
                }.sortedWith { e1, e2 ->
                    val s1 = e1.solarDate == "Đang đồng bộ..." || e1.solarDate == "Đồng bộ sau"
                    val s2 = e2.solarDate == "Đang đồng bộ..." || e2.solarDate == "Đồng bộ sau"

                    if (s1 && !s2) return@sortedWith -1
                    if (!s1 && s2) return@sortedWith 1
                    if (s1 && s2) return@sortedWith e2.id.compareTo(e1.id)

                    // Sắp xếp theo thời gian tăng dần (ngày gần nhất lên đầu)
                    e1.eventDate.compareTo(e2.eventDate)
                }
                _events.postValue(filteredEvents)
            }
        }
    }

    private fun parseDate(dateStr: String): Date? {
        val formats = listOf("dd/MM/yyyy", "yyyy-MM-dd")
        for (format in formats) {
            try {
                return SimpleDateFormat(format, Locale.getDefault()).apply { isLenient = false }.parse(dateStr)
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    private fun calculateStatus(solarDateStr: String, todayMillis: Long): String {
        if (solarDateStr == "Đang đồng bộ..." || solarDateStr == "Đồng bộ sau") return solarDateStr
        return try {
            val eventDate = parseDate(solarDateStr) ?: return ""
            val diff = eventDate.time - todayMillis
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            when {
                days == 0L -> "Hôm nay là ngày giỗ"
                days > 0 -> "Còn $days ngày"
                else -> "Đã qua"
            }
        } catch (e: Exception) {
            ""
        }
    }
}