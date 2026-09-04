package com.linhchay.thaphuonggiatien.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linhchay.thaphuonggiatien.data.model.Event
import com.linhchay.thaphuonggiatien.databinding.ItemHomeEventBinding

class EventAdapter(
    private val showActions: Boolean = true,
    private val onEditClick: ((Event) -> Unit)? = null,
    private val onDeleteClick: ((Event) -> Unit)? = null
) : ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemHomeEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding, showActions, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventViewHolder(
        private val binding: ItemHomeEventBinding,
        private val showActions: Boolean,
        private val onEditClick: ((Event) -> Unit)?,
        private val onDeleteClick: ((Event) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.txtEventName.text = event.name
            binding.txtSolarDate.text = event.solarDate
            binding.txtLunarDate.text = event.lunarDate
            binding.txtStatus.text = event.status

            if (showActions) {
                binding.layoutActions.visibility = android.view.View.VISIBLE
                binding.btnEdit.setOnClickListener { onEditClick?.invoke(event) }
                binding.btnDelete.setOnClickListener { onDeleteClick?.invoke(event) }
            } else {
                binding.layoutActions.visibility = android.view.View.GONE
            }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}