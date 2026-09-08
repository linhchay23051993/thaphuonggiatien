package com.linhchay.thaphuonggiatien

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        val isFirstLaunch = sharedPref.getBoolean("is_first_launch", true)
        val lastLoginDate = sharedPref.getString("last_login_date", "")
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        if (isFirstLaunch) {
            // Lần đầu mở app: cộng 100 gold
            updateGold(100)
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
}