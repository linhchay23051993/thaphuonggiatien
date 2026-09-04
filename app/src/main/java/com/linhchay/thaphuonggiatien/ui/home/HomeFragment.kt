package com.linhchay.thaphuonggiatien.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.linhchay.thaphuonggiatien.MainViewModel
import com.linhchay.thaphuonggiatien.R
import com.linhchay.thaphuonggiatien.databinding.FragmentHomeBinding
import com.linhchay.thaphuonggiatien.ui.home.adapter.EventAdapter
import com.linhchay.thaphuonggiatien.utils.ViewUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        interfaceInflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(interfaceInflater, container, false)
        
        ViewUtils.applyStatusBarMargin(binding.root)
        
        setupGoldObserver()
        setupEventsRecyclerView(homeViewModel)
        setupClickListeners()
        updateUserInfo()

        return binding.root
    }

    private fun updateUserInfo() {
        val context = context ?: return
        val sharedPref = context.getSharedPreferences("user_profile", android.content.Context.MODE_PRIVATE)
        val name = sharedPref.getString("name", "")
        
        binding.txtUserName.text = if (name.isNullOrEmpty()) "Hello" else "Hello, $name"

        val calendar = Calendar.getInstance()
        val localeVi = Locale("vi", "VN")
        val dayOfWeekFormat = SimpleDateFormat("EEEE", localeVi)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val dayOfWeek = dayOfWeekFormat.format(calendar.time).replaceFirstChar { it.uppercase() }
        binding.txtDayOfWeek.text = dayOfWeek
        binding.txtDate.text = dateFormat.format(calendar.time)
    }

    private fun setupGoldObserver() {
        mainViewModel.gold.observe(viewLifecycleOwner) { gold ->
            binding.layoutGold.txtGold.text = gold.toString()
        }
    }

    private fun setupClickListeners() {
        binding.cardBanThoGiaTien.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.selectedItemId = R.id.navigation_ancestor
        }
        
        binding.cardDangLe.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.selectedItemId = R.id.navigation_temple
        }
    }

    private fun setupEventsRecyclerView(viewModel: HomeViewModel) {
        val eventAdapter = EventAdapter(showActions = false)
        binding.rvEvents.adapter = eventAdapter
        
        viewModel.events.observe(viewLifecycleOwner) { events ->
            eventAdapter.submitList(events)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}