package com.linhchay.thaphuonggiatien

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPref = application.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    
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
        if (sharedPref.getInt("gold", 0) == 0) {
            updateGold(100)
        }
    }
}