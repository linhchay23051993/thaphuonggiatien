package com.linhchay.thaphuonggiatien.ui.temple

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
import com.linhchay.thaphuonggiatien.data.model.Temple
import com.linhchay.thaphuonggiatien.data.model.Prayer

class TempleViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("temple_altar_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _temples = MutableLiveData<List<Temple>>().apply {
        value = listOf(
            Temple(1, "Chùa Một Cột", "Đội Cấn, Ba Đình, Hà Nội", R.drawable.ic_event_placeholder, R.drawable.ban_tho_1),
            Temple(2, "Chùa Hương", "Hương Sơn, Mỹ Đức, Hà Nội", R.drawable.ic_event_placeholder, R.drawable.ban_tho_1),
            Temple(3, "Chùa Bái Đính", "Gia Sinh, Gia Viễn, Ninh Bình", R.drawable.ic_event_placeholder, R.drawable.ban_tho_1),
            Temple(4, "Chùa Tam Chúc", "Ba Sao, Kim Bảng, Hà Nam", R.drawable.ic_event_placeholder, R.drawable.ban_tho_1),
            Temple(5, "Đền Hùng", "Hy Cương, Việt Trì, Phú Thọ", R.drawable.ic_event_placeholder, R.drawable.ban_tho_1)
        )
    }
    val temples: LiveData<List<Temple>> = _temples

    private val _placedItems = MutableLiveData<List<AltarItem>>(emptyList())
    val placedItems: LiveData<List<AltarItem>> = _placedItems

    private val _isEditMode = MutableLiveData<Boolean>(false)
    val isEditMode: LiveData<Boolean> = _isEditMode

    private val _isBurning = MutableLiveData<Boolean>(false)
    val isBurning: LiveData<Boolean> = _isBurning

    private val _burningSticks = MutableLiveData<Int>(0)
    val burningSticks: LiveData<Int> = _burningSticks

    private var currentTempleId: Int = -1
    private var timer: CountDownTimer? = null

    private val _prayers = MutableLiveData<List<Prayer>>().apply {
        value = listOf(
            Prayer(1, "Văn khấn lễ Phật", "Nam mô A Di Đà Phật! (3 lần)\n\nCon lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương...\n\nHôm nay là ngày... tháng... năm...\n\nTín chủ con là...\nNgụ tại...\n\nThành tâm dâng lễ, thắp nén hương thơm trước án...\n\nCẩn cáo!"),
            Prayer(2, "Văn khấn đền Tam Tòa Thánh Mẫu", "Nam mô A Di Đà Phật! (3 lần)\n\nCon kính lạy đức Thánh Mẫu Tam Tòa..."),
            Prayer(3, "Văn khấn lễ Đức Ông", "Nam mô A Di Đà Phật! (3 lần)\n\nCon kính lạy Đức Ông tu vi tôn giả...")
        )
    }
    val prayers: LiveData<List<Prayer>> = _prayers

    val allCategories = listOf(
        "Bàn thờ" to listOf(R.drawable.ban_tho_1, R.drawable.ban_tho_1, R.drawable.ban_tho_1),
        "Bát hương" to listOf(R.drawable.bat_huong_1, R.drawable.bat_huong_1, R.drawable.bat_huong_1),
        "Mâm hoa quả" to listOf(R.drawable.hoa_qua_1, R.drawable.hoa_qua_1, R.drawable.hoa_qua_1),
        "Chén rượu" to listOf(R.drawable.chen_1, R.drawable.chen_1, R.drawable.chen_1),
        "Nến" to listOf(android.R.drawable.ic_menu_edit)
    )

    private val offerings = listOf("Mâm hoa quả", "Chén rượu", "Nến")

    fun loadTempleAltar(templeId: Int) {
        currentTempleId = templeId
        val json = sharedPrefs.getString("temple_items_$templeId", null)
        if (json != null) {
            val type = object : TypeToken<List<AltarItem>>() {}.type
            _placedItems.value = gson.fromJson(json, type)
        } else {
            _placedItems.value = emptyList()
        }
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

    fun saveChanges() {
        if (currentTempleId != -1) {
            val json = gson.toJson(_placedItems.value)
            sharedPrefs.edit().putString("temple_items_$currentTempleId", json).apply()
        }
    }

    fun cancelChanges() {
        if (currentTempleId != -1) loadTempleAltar(currentTempleId)
    }

    fun startBurning(sticks: Int) {
        if (_isBurning.value == true) return
        _isBurning.value = true
        _burningSticks.value = sticks

        timer?.cancel()
        timer = object : CountDownTimer(30000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                _isBurning.value = false
                _burningSticks.value = 0
            }
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
