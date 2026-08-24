package com.linhchay.thaphuonggiatien.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.linhchay.thaphuonggiatien.databinding.FragmentHomeBinding
import com.linhchay.thaphuonggiatien.ui.home.adapter.EventAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        interfaceInflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(interfaceInflater, container, false)
        
        setupEventsRecyclerView(homeViewModel)

        return binding.root
    }

    private fun setupEventsRecyclerView(viewModel: HomeViewModel) {
        val eventAdapter = EventAdapter()
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