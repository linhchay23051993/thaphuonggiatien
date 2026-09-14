package com.linhchay.thaphuonggiatien.ui.home

import android.net.Uri
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

    override fun onResume() {
        super.onResume()
        updateUserInfo()
    }

    private fun updateUserInfo() {
        val context = context ?: return
        val sharedPref = context.getSharedPreferences("user_profile", android.content.Context.MODE_PRIVATE)
        val name = sharedPref.getString("name", "")
        val avatarUriString = sharedPref.getString("avatar_uri", null)

        // Greeting always "Xin chào!", name on line below
        binding.txtGreeting.text = "Xin chào!"
        binding.txtUserName.text = if (name.isNullOrEmpty()) "" else name

        // Load avatar
        if (!avatarUriString.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(avatarUriString)
                binding.imgAvatar.setImageURI(uri)
            } catch (e: Exception) {
                binding.imgAvatar.setImageResource(R.drawable.ic_user_placeholder)
            }
        } else {
            binding.imgAvatar.setImageResource(R.drawable.ic_user_placeholder)
        }

        val calendar = Calendar.getInstance()
        val localeVi = Locale("vi", "VN")
        val dayOfWeekFormat = SimpleDateFormat("EEEE", localeVi)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val dayOfWeek = dayOfWeekFormat.format(calendar.time).replaceFirstChar { it.uppercase() }
        binding.txtDayOfWeek.text = "$dayOfWeek,"
        binding.txtDate.text = dateFormat.format(calendar.time)

        // Âm lịch
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        val lunar = com.linhchay.thaphuonggiatien.utils.LunarSolarConverter.convertSolar2Lunar(day, month, year)
        val leapStr = if (lunar.isLeapMonth) " (Nhuận)" else ""
        binding.txtLunarDate.text = "Âm lịch: %02d/%02d%s".format(lunar.day, lunar.month, leapStr)
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