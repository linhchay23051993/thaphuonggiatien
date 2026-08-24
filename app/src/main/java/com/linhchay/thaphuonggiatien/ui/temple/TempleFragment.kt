package com.linhchay.thaphuonggiatien.ui.temple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.linhchay.thaphuonggiatien.databinding.FragmentTempleBinding

class TempleFragment : Fragment() {

    private var _binding: FragmentTempleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val templeViewModel = ViewModelProvider(this).get(TempleViewModel::class.java)

        _binding = FragmentTempleBinding.inflate(inflater, container, false)

        templeViewModel.temples.observe(viewLifecycleOwner) { temples ->
            binding.rvTemples.adapter = TempleAdapter(temples)
        }

        return binding.root
    }

    class TempleAdapter(private val items: List<String>) : RecyclerView.Adapter<TempleAdapter.ViewHolder>() {
        class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val tv = TextView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(32, 32, 32, 32)
                textSize = 18f
            }
            return ViewHolder(tv)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = items[position]
        }

        override fun getItemCount() = items.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}