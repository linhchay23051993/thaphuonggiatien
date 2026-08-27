package com.linhchay.thaphuonggiatien.ui.ancestor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linhchay.thaphuonggiatien.data.model.Prayer
import com.linhchay.thaphuonggiatien.databinding.ItemPrayerBinding

class PrayerAdapter(private val onItemClick: (Prayer) -> Unit) :
    ListAdapter<Prayer, PrayerAdapter.PrayerViewHolder>(PrayerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerViewHolder {
        val binding = ItemPrayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PrayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PrayerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PrayerViewHolder(private val binding: ItemPrayerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(prayer: Prayer) {
            binding.txtPrayerTitle.text = prayer.title
            binding.root.setOnClickListener { onItemClick(prayer) }
        }
    }

    class PrayerDiffCallback : DiffUtil.ItemCallback<Prayer>() {
        override fun areItemsTheSame(oldItem: Prayer, newItem: Prayer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Prayer, newItem: Prayer): Boolean {
            return oldItem == newItem
        }
    }
}
