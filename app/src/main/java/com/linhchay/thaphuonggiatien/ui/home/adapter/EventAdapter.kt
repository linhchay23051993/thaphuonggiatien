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
            // Cập nhật tên và trạng thái đếm ngược riêng biệt
            binding.txtEventName.text = event.name
            
            val remainingDays = com.linhchay.thaphuonggiatien.utils.DateUtils.getRemainingDaysDescription(event.eventDate)
            binding.txtStatus.apply {
                text = remainingDays
                visibility = android.view.View.VISIBLE
            }
            
            // Hàm helper để lấy ngày/tháng (bỏ 0 ở đầu, bỏ năm, bỏ chữ bổ trợ)
            fun getShortDate(date: String): String {
                // Xóa các chữ như (Âm lịch), Âm...
                val cleaned = date.replace(Regex("""(?i)\s*\(?Âm lịch\)?\s*"""), "")
                    .replace(Regex("""(?i)\s*Âm\s*"""), "")
                    .trim()
                
                // Tách lấy ngày/tháng/năm
                val parts = cleaned.split("/")
                return if (parts.size >= 2) {
                    val day = parts[0].toIntOrNull()?.toString() ?: parts[0]
                    val month = parts[1].toIntOrNull()?.toString() ?: parts[1]
                    "$day/$month"
                } else {
                    cleaned
                }
            }
            
            val solarDisplay = getShortDate(event.solarDate)
            val lunarDisplay = getShortDate(event.lunarDate)
            
            binding.txtSolarDate.text = "Dương: $solarDisplay - Âm: $lunarDisplay"

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