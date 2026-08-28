package com.linhchay.thaphuonggiatien.ui.ancestor

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.linhchay.thaphuonggiatien.R
import com.linhchay.thaphuonggiatien.data.model.AltarItem
import com.linhchay.thaphuonggiatien.data.model.Event
import com.linhchay.thaphuonggiatien.data.model.Prayer

class AncestorViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("altar_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

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
        val currentList = _anniversaries.value?.toMutableList() ?: mutableListOf()
        val nextId = (currentList.maxOfOrNull { it.id } ?: 0) + 1
        // For simplicity, we just set a dummy solar date as the user only inputs lunar date
        val newEvent = Event(nextId, name, "2026", lunarDate)
        currentList.add(newEvent)
        _anniversaries.value = currentList
        // In a real app, you would save this to persistent storage
    }

    private fun loadAnniversaries() {
        _anniversaries.value = emptyList()
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

        return currentItems.filter { it.id !in savedIds }.sumOf { it.price }
    }

    fun saveChanges() {
        savePlacedItems()
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
