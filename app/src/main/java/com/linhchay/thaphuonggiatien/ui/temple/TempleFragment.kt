package com.linhchay.thaphuonggiatien.ui.temple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.linhchay.thaphuonggiatien.MainViewModel
import com.linhchay.thaphuonggiatien.R
import com.linhchay.thaphuonggiatien.databinding.FragmentTempleBinding
import com.linhchay.thaphuonggiatien.ui.temple.adapter.TempleAdapter
import com.linhchay.thaphuonggiatien.utils.ViewUtils

class TempleFragment : Fragment() {

    private var _binding: FragmentTempleBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val templeViewModel = ViewModelProvider(this).get(TempleViewModel::class.java)

        _binding = FragmentTempleBinding.inflate(inflater, container, false)

        ViewUtils.applyStatusBarMargin(binding.root)

        setupGoldObserver()

        val adapter = TempleAdapter { temple ->
            val bundle = Bundle().apply {
                putSerializable("temple", temple)
            }
            findNavController().navigate(R.id.action_navigation_temple_to_templeAltarFragment, bundle)
        }
        binding.rvTemples.adapter = adapter

        templeViewModel.temples.observe(viewLifecycleOwner) { temples ->
            adapter.submitList(temples)
        }

        return binding.root
    }

    private fun setupGoldObserver() {
        mainViewModel.gold.observe(viewLifecycleOwner) { gold ->
            binding.layoutGold.txtGold.text = gold.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
