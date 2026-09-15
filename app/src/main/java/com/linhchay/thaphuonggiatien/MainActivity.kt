package com.linhchay.thaphuonggiatien

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.linhchay.thaphuonggiatien.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Map mỗi destination về tab tương ứng trên BottomNav
        val destinationToTab = mapOf(
            R.id.navigation_home     to R.id.navigation_home,
            R.id.allEventsFragment   to R.id.navigation_home,   // sub-screen của Home
            R.id.navigation_ancestor to R.id.navigation_ancestor,
            R.id.navigation_temple   to R.id.navigation_temple,
            R.id.templeListFragment  to R.id.navigation_temple,
            R.id.templeAltarFragment to R.id.navigation_temple,
            R.id.navigation_profile  to R.id.navigation_profile
        )

        // Flag để tránh vòng lặp khi set selectedItemId bằng code
        var updatingSelection = false

        // Đồng bộ highlight BottomNav theo NavController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val tabId = destinationToTab[destination.id] ?: return@addOnDestinationChangedListener
            if (!updatingSelection && navView.selectedItemId != tabId) {
                updatingSelection = true
                navView.selectedItemId = tabId
                updatingSelection = false
            }
        }

        // Xử lý khi user bấm tab
        navView.setOnItemSelectedListener { menuItem ->
            if (updatingSelection) return@setOnItemSelectedListener true

            // Pop toàn bộ nested navigation về start destination trước khi đổi tab
            // Điều này đảm bảo AllEventsFragment (và các sub-screen khác) bị xoá khỏi stack
            val startDestId = navController.graph.startDestinationId
            navController.popBackStack(startDestId, false)

            // Navigate đến tab được chọn
            NavigationUI.onNavDestinationSelected(menuItem, navController)
            true
        }

        viewModel.addInitialGold()
    }

    fun updateGold(amount: Int): Boolean {
        return viewModel.updateGold(amount)
    }
}