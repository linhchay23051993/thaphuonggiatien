package com.linhchay.thaphuonggiatien.ui.ancestor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.linhchay.thaphuonggiatien.databinding.FragmentAncestorBinding
import java.util.Locale
import androidx.core.view.isVisible

class AncestorFragment : Fragment() {

    private var _binding: FragmentAncestorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ancestorViewModel = ViewModelProvider(this).get(AncestorViewModel::class.java)
        _binding = FragmentAncestorBinding.inflate(inflater, container, false)

        binding.btnLightIncense.setOnClickListener {
            ancestorViewModel.startBurning(60000) // 1 minute
        }

        ancestorViewModel.isBurning.observe(viewLifecycleOwner) { isBurning ->
            binding.btnLightIncense.isEnabled = !isBurning
            binding.smokeEffect.isVisible = isBurning
            binding.txtTimer.isVisible = isBurning
            if (isBurning) {
                startSmokeAnimation()
            } else {
                binding.smokeEffect.clearAnimation()
            }
        }

        ancestorViewModel.remainingTime.observe(viewLifecycleOwner) { millis ->
            val seconds = millis / 1000
            binding.txtTimer.text = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60)
        }

        return binding.root
    }

    private fun startSmokeAnimation() {
        binding.smokeEffect.alpha = 0f
        binding.smokeEffect.animate()
            .alpha(0.5f)
            .setDuration(2000)
            .withEndAction {
                binding.smokeEffect.animate()
                    .alpha(0f)
                    .setDuration(2000)
                    .withEndAction { if (binding.smokeEffect.isVisible) startSmokeAnimation() }
                    .start()
            }
            .start()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}