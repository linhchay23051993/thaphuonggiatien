package com.linhchay.thaphuonggiatien.ui.ancestor

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.linhchay.thaphuonggiatien.R
import com.linhchay.thaphuonggiatien.data.local.AppDatabase
import com.linhchay.thaphuonggiatien.data.local.entities.EventEntity
import com.linhchay.thaphuonggiatien.data.model.AltarItem
import com.linhchay.thaphuonggiatien.data.model.Event
import com.linhchay.thaphuonggiatien.data.model.Prayer
import com.linhchay.thaphuonggiatien.data.repository.LunarSolarRepository
import com.linhchay.thaphuonggiatien.data.worker.SyncEventWorker
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AncestorViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("altar_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val repository = LunarSolarRepository()
    private val eventDao = AppDatabase.getDatabase(application).eventDao()

    private val _placedItems = MutableLiveData<List<AltarItem>>(emptyList())
    val placedItems: LiveData<List<AltarItem>> = _placedItems

    private val _anniversaries = MutableLiveData<List<Event>>()
    val anniversaries: LiveData<List<Event>> = _anniversaries

    private val _isBurning = MutableLiveData<Boolean>(false)
    val isBurning: LiveData<Boolean> = _isBurning

    private val _remainingTime = MutableLiveData<Long>(0L)
    val remainingTime: LiveData<Long> = _remainingTime

    private val _isEditMode = MutableLiveData<Boolean>(false)
    val isEditMode: LiveData<Boolean> = _isEditMode

    private val _burningSticks = MutableLiveData<Int>(0)
    val burningSticks: LiveData<Int> = _burningSticks

    private val _prayers = MutableLiveData<List<Prayer>>()
    val prayers: LiveData<List<Prayer>> = _prayers

    private val _purchasedResIds = MutableLiveData<Set<Int>>(emptySet())
    val purchasedResIds: LiveData<Set<Int>> = _purchasedResIds

    private var timer: CountDownTimer? = null

    val allCategories = listOf(
        "Bàn thờ" to listOf(
            R.drawable.ban_tho_1,
            R.drawable.ban_tho_2,
            R.drawable.ban_tho_3,
            R.drawable.ban_tho_4,
            R.drawable.ban_tho_5
        ),

        "Khung ảnh" to listOf(
            R.drawable.khung_anh_1,
            R.drawable.khung_anh_2,
            R.drawable.khung_anh_3,
            R.drawable.khung_anh_4,
            R.drawable.khung_anh_5
        ),

        "Bát hương" to listOf(
            R.drawable.bat_huong_1,
            R.drawable.bat_huong_2,
            R.drawable.bat_huong_3,
            R.drawable.bat_huong_4,
            R.drawable.bat_huong_5
        ),

        "Hoành phi" to listOf(
            R.drawable.hoanh_phi_1,
            R.drawable.hoanh_phi_2,
            R.drawable.hoanh_phi_3,
            R.drawable.hoanh_phi_4,
            R.drawable.hoanh_phi_5
        ),

        "Mâm hoa quả" to listOf(
            R.drawable.hoa_qua_1,
            R.drawable.hoa_qua_2,
            R.drawable.hoa_qua_3,
            R.drawable.hoa_qua_4,
            R.drawable.hoa_qua_5
        ),

        "Chén rượu" to listOf(
            R.drawable.chen_1,
            R.drawable.chen_2,
            R.drawable.chen_3,
            R.drawable.chen_4,
            R.drawable.chen_5
        )
    )

    private val offerings = listOf("Mâm hoa quả", "Chén rượu")

    init {
        loadAnniversaries()
        loadPlacedItems()
        loadPrayers()
        loadPurchasedItems()
    }

    private fun loadPurchasedItems() {
        val json = sharedPrefs.getString("purchased_res_ids", null)
        if (json != null) {
            val type = object : TypeToken<Set<Int>>() {}.type
            val ids: Set<Int> = gson.fromJson(json, type)
            _purchasedResIds.value = ids
        }
    }

    private fun loadPrayers() {
        val list = listOf(
            Prayer(1, "Bài khấn Gia Tiên (Hàng ngày)", "Con lạy chín phương Trời, mười phương Chư Phật...\n\nHôm nay là ngày... tháng... năm...\n\nTín chủ con là...\nNgụ tại...\n\nThành tâm dâng nén hương thơm, hoa quả, lễ vật...\nCầu xin gia tiên phù hộ độ trì cho gia đình bình an, mạnh khỏe..."),
            Prayer(2, "Bài khấn Rằm, Mùng 1", "Nam mô A Di Đà Phật!\nNam mô A Di Đà Phật!\nNam mô A Di Đà Phật!\n\nCon lạy chín phương Trời, mười phương Chư Phật...\n\nHôm nay là ngày Rằm (Mùng 1) tháng...\n\nTín chủ con kính mời vong linh tổ tiên nội ngoại..."),
            Prayer(3, "Bài khấn Thổ Công, Táo Quân", "Con kính lạy Ngài Đông trù Tư mệnh Táo phủ Thần quân...\n\nHôm nay là ngày...\n\nTín chủ con thành tâm sắm sửa lễ vật, hương hoa trà quả..."),
            Prayer(4, "Bài khấn Tất Niên", "Nam mô A Di Đà Phật!\n\nKính lạy Hoàng thiên Hậu thổ Chư vị Tôn thần...\nNgài Bản cảnh Thành hoàng, Ngài Bản xứ Thổ địa...\n\nHôm nay là ngày 30 tháng Chạp năm...\n\nSắm sửa lễ vật, cơm canh thịnh soạn..."),
            Prayer(5, "Bài khấn Giao Thừa", "Nam mô A Di Đà Phật!\n\nKính lạy Cựu niên đương cai Thái tuế Chí đức Tôn thần, Tân niên đương cai Khúc Tào Phán quan...")
        )
        _prayers.value = list
    }

    fun getCategories(onlyOfferings: Boolean): List<Pair<String, List<Int>>> {
        return if (onlyOfferings) {
            allCategories.filter { it.first in offerings }
        } else {
            allCategories.filter { it.first !in offerings }
        }
    }

    fun setEditMode(enabled: Boolean) {
        _isEditMode.value = enabled
    }

    fun addEvent(name: String, lunarDate: String) {
        val parts = lunarDate.split("/", "-")
        if (parts.size < 2) return

        val day = parts[0].trim().toIntOrNull() ?: return
        val month = parts[1].trim().toIntOrNull() ?: return
        val year = Calendar.getInstance().get(Calendar.YEAR)

        viewModelScope.launch {
            val tempEvent = EventEntity(
                name = name,
                solarDate = "Đang đồng bộ...",
                lunarDate = "$day/$month/$year (Âm lịch)"
            )
            val insertedId = eventDao.insertEvent(tempEvent).toInt()

            val result = repository.getLunarDate(day, month, year)
            result.onSuccess { response ->
                response?.let {
                    val solarDateStr = it.duongLich
                    eventDao.updateEvent(tempEvent.copy(id = insertedId, solarDate = solarDateStr))
                } ?: run {
                    eventDao.updateEvent(tempEvent.copy(id = insertedId, solarDate = "Đồng bộ sau"))
                    scheduleSyncWorker(insertedId, name, day, month, year)
                }
            }.onFailure {
                eventDao.updateEvent(tempEvent.copy(id = insertedId, solarDate = "Đồng bộ sau"))
                scheduleSyncWorker(insertedId, name, day, month, year)
            }
        }
    }

    fun updateEvent(id: Int, name: String, lunarDate: String) {
        val parts = lunarDate.split("/", "-")
        if (parts.size < 2) return

        val day = parts[0].trim().toIntOrNull() ?: return
        val month = parts[1].trim().toIntOrNull() ?: return
        val year = Calendar.getInstance().get(Calendar.YEAR)

        viewModelScope.launch {
            val tempEvent = EventEntity(
                id = id,
                name = name,
                solarDate = "Đang đồng bộ...",
                lunarDate = "$day/$month/$year (Âm lịch)"
            )
            eventDao.updateEvent(tempEvent)

            val result = repository.getLunarDate(day, month, year)
            result.onSuccess { response ->
                response?.let {
                    val solarDateStr = it.duongLich
                    eventDao.updateEvent(tempEvent.copy(solarDate = solarDateStr))
                } ?: run {
                    eventDao.updateEvent(tempEvent.copy(solarDate = "Đồng bộ sau"))
                    scheduleSyncWorker(id, name, day, month, year)
                }
            }.onFailure {
                eventDao.updateEvent(tempEvent.copy(solarDate = "Đồng bộ sau"))
                scheduleSyncWorker(id, name, day, month, year)
            }
        }
    }

    private fun scheduleSyncWorker(eventId: Int?, name: String, day: Int, month: Int, year: Int) {
        val data = Data.Builder()
            .putInt("event_id", eventId ?: -1)
            .putString("name", name)
            .putInt("day", day)
            .putInt("month", month)
            .putInt("year", year)
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

    fun deleteEvent(eventId: Int) {
        viewModelScope.launch {
            eventDao.deleteEventById(eventId)
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

    private fun loadAnniversaries() {
        viewModelScope.launch {
            eventDao.getAllEvents().collectLatest { entities ->
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val events = entities.map { entity ->
                    Event(
                        id = entity.id,
                        name = entity.name,
                        solarDate = entity.solarDate,
                        lunarDate = entity.lunarDate,
                        status = calculateStatus(entity.solarDate, today)
                    )
                }.sortedWith { e1, e2 ->
                    val s1 = e1.solarDate == "Đang đồng bộ..." || e1.solarDate == "Đồng bộ sau"
                    val s2 = e2.solarDate == "Đang đồng bộ..." || e2.solarDate == "Đồng bộ sau"

                    if (s1 && !s2) return@sortedWith -1
                    if (!s1 && s2) return@sortedWith 1
                    if (s1 && s2) return@sortedWith e2.id.compareTo(e1.id)

                    val t1 = parseDate(e1.solarDate)?.time ?: 0L
                    val t2 = parseDate(e2.solarDate)?.time ?: 0L

                    val diff1 = t1 - today
                    val diff2 = t2 - today

                    when {
                        // Cả 2 đều chưa tới hoặc là hôm nay: Ngày gần hơn xếp trên (ASC)
                        diff1 >= 0 && diff2 >= 0 -> diff1.compareTo(diff2)
                        // Cả 2 đều đã qua: Ngày vừa qua (gần 0 hơn) xếp trên (DESC)
                        diff1 < 0 && diff2 < 0 -> diff2.compareTo(diff1)
                        // Ưu tiên ngày chưa tới lên trên
                        diff1 >= 0 -> -1
                        else -> 1
                    }
                }
                _anniversaries.postValue(events)
            }
        }
    }

    private fun loadPlacedItems() {
        val json = sharedPrefs.getString("placed_items", null)
        if (json != null) {
            val type = object : TypeToken<List<AltarItem>>() {}.type
            val items: List<AltarItem> = gson.fromJson(json, type)
            _placedItems.value = items
        } else {
            _placedItems.value = emptyList()
        }
    }

    fun addAltarItem(item: AltarItem) {
        val currentList = _placedItems.value?.toMutableList() ?: mutableListOf()
        currentList.add(item)
        _placedItems.value = currentList
    }

    fun updateItemPosition(itemId: Long, x: Float, y: Float, width: Int, height: Int) {
        val currentList = _placedItems.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = currentList[index]
            item.x = x
            item.y = y
            item.width = width
            item.height = height
            _placedItems.value = currentList
        }
    }

    fun removeItem(itemId: Long) {
        val currentList = _placedItems.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index != -1) {
            currentList.removeAt(index)
            _placedItems.value = currentList
        }
    }

    fun calculateNewItemsCost(): Int {
        val savedJson = sharedPrefs.getString("placed_items", null)
        val savedItems: List<AltarItem> = if (savedJson != null) {
            val type = object : TypeToken<List<AltarItem>>() {}.type
            gson.fromJson(savedJson, type)
        } else {
            emptyList()
        }

        val savedIds = savedItems.map { it.id }.toSet()
        val currentItems = _placedItems.value ?: emptyList()
        
        val purchased = _purchasedResIds.value ?: emptySet()

        return currentItems.filter { it.id !in savedIds && it.imageResId !in purchased }
            .sumOf { it.price }
    }

    fun saveChanges() {
        savePlacedItems()
        savePurchasedItems()
    }

    private fun savePurchasedItems() {
        val currentItems = _placedItems.value ?: return
        val currentPurchased = _purchasedResIds.value?.toMutableSet() ?: mutableSetOf()
        
        currentItems.forEach { 
            currentPurchased.add(it.imageResId)
        }
        
        _purchasedResIds.value = currentPurchased
        val json = gson.toJson(currentPurchased)
        sharedPrefs.edit().putString("purchased_res_ids", json).apply()
    }

    fun cancelChanges() {
        loadPlacedItems()
    }

    private fun savePlacedItems() {
        val json = gson.toJson(_placedItems.value)
        sharedPrefs.edit().putString("placed_items", json).apply()
    }

    fun startBurning(sticks: Int) {
        val durationMillis = 30000L // 30 seconds for all types of incense
        if (_isBurning.value == true) return

        _isBurning.value = true
        _burningSticks.value = sticks
        _remainingTime.value = durationMillis

        timer?.cancel()
        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingTime.value = millisUntilFinished
            }

            override fun onFinish() {
                _isBurning.value = false
                _burningSticks.value = 0
                _remainingTime.value = 0L
            }
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}