package com.linhchay.thaphuonggiatien

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.linhchay.thaphuonggiatien.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        navView.setupWithNavController(navController)

        handleGold()
    }

    fun updateGold(amount: Int): Boolean {
        val sharedPref = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        var currentGold = sharedPref.getInt("gold", 0)
        
        if (currentGold + amount < 0) return false
        
        currentGold += amount
        sharedPref.edit().putInt("gold", currentGold).apply()
        binding.txtGold.text = currentGold.toString()
        return true
    }

    private fun handleGold() {
        val sharedPref = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        var currentGold = sharedPref.getInt("gold", 0)
        currentGold += 100
        
        sharedPref.edit().putInt("gold", currentGold).apply()

        binding.txtGold.text = currentGold.toString()
    }
}