package com.linhchay.thaphuonggiatien.ui.ancestor

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.linhchay.thaphuonggiatien.data.model.AltarItem
import com.linhchay.thaphuonggiatien.data.model.Event

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

    private val _burningSticks = MutableLiveData<Int>(0)
    val burningSticks: LiveData<Int> = _burningSticks

    private var timer: CountDownTimer? = null

    init {
        loadAnniversaries()
        loadPlacedItems()
    }

    private fun loadAnniversaries() {
        val list = listOf(
            Event(1, "Giỗ ông nội", "25/08/2026", "15/07 Âm lịch"),
            Event(2, "Giỗ bà ngoại", "10/09/2026", "01/08 Âm lịch"),
            Event(3, "Giỗ cụ cố", "15/10/2026", "06/09 Âm lịch")
        )
        _anniversaries.value = list
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
