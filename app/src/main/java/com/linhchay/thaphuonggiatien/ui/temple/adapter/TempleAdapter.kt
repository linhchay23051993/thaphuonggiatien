package com.linhchay.thaphuonggiatien.ui.temple.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linhchay.thaphuonggiatien.data.model.Temple
import com.linhchay.thaphuonggiatien.databinding.ItemTempleBinding

class TempleAdapter(private val onItemClick: (Temple) -> Unit) :
    ListAdapter<Temple, TempleAdapter.TempleViewHolder>(TempleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TempleViewHolder {
        val binding = ItemTempleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TempleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TempleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TempleViewHolder(private val binding: ItemTempleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(temple: Temple) {
            binding.txtTempleName.text = temple.name
            binding.txtTempleLocation.text = temple.location
            binding.imgTemple.setImageResource(temple.imageRes)
            
            binding.root.setOnClickListener { onItemClick(temple) }
            binding.btnDangLeTemple.setOnClickListener { onItemClick(temple) }
        }
    }

    class TempleDiffCallback : DiffUtil.ItemCallback<Temple>() {
        override fun areItemsTheSame(oldItem: Temple, newItem: Temple): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Temple, newItem: Temple): Boolean {
            return oldItem == newItem
        }
    }
}
