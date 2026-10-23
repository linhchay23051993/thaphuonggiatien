package com.linhchay.thaphuonggiatien.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.linhchay.thaphuonggiatien.R
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

            // Icon
            val iconRes = when (event.iconType) {
                0 -> com.linhchay.thaphuonggiatien.R.drawable.icon_lehoi
                1 -> com.linhchay.thaphuonggiatien.R.drawable.icon_ram
                else -> com.linhchay.thaphuonggiatien.R.drawable.icon_gio
            }
            binding.imgEventIcon.setImageResource(iconRes)

            // Ngày hiển thị
            fun getShortDate(date: String): String {
                val cleaned = date.replace(Regex("""(?i)\s*\(?Âm lịch\)?\s*"""), "")
                    .replace(Regex("""(?i)\s*Âm\s*"""), "")
                    .trim()
                val parts = cleaned.split("/")
                return if (parts.size >= 2) {
                    val day = parts[0].toIntOrNull()?.toString() ?: parts[0]
                    val month = parts[1].toIntOrNull()?.toString() ?: parts[1]
                    "$day/$month"
                } else {
                    cleaned
                }
            }
            binding.txtSolarDate.text = "Dương: ${getShortDate(event.solarDate)} - Âm: ${getShortDate(event.lunarDate)}"

            // Số ngày còn lại (luôn tính)
            val remainingDays = com.linhchay.thaphuonggiatien.utils.DateUtils
                .getRemainingDaysDescription(event.eventDate)

            if (showActions) {
                // Màn AllEvents: badge hiện cạnh tên, menu 3 chấm bên phải
                binding.txtStatus.text = remainingDays
                binding.txtStatus.visibility = View.VISIBLE
                binding.layoutBadge.visibility = View.GONE
                binding.layoutActions.visibility = View.VISIBLE
                binding.btnMore.setOnClickListener { view ->
                    showPopupMenu(view, event)
                }
            } else {
                // Màn Home: badge ở bên phải (center-right), ẩn badge cạnh tên
                binding.txtBadge.text = remainingDays
                binding.txtStatus.visibility = View.GONE
                binding.layoutBadge.visibility = View.VISIBLE
                binding.layoutActions.visibility = View.GONE
            }
        }

        private fun showPopupMenu(anchor: View, event: Event) {
            val context = anchor.context
            val inflater = LayoutInflater.from(context)
            val popupView = inflater.inflate(R.layout.layout_menu_event_options, null)

            // Đặt chiều rộng cố định 85dp (gọn gàng, vừa đủ cho icon + chữ Sửa / Xoá)
            val widthPx = (85 * context.resources.displayMetrics.density).toInt()

            val popupWindow = android.widget.PopupWindow(
                popupView,
                widthPx,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            ).apply {
                setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                isOutsideTouchable = true
                elevation = 16f
            }

            popupView.findViewById<View>(R.id.menuItemEdit).setOnClickListener {
                popupWindow.dismiss()
                onEditClick?.invoke(event)
            }

            popupView.findViewById<View>(R.id.menuItemDelete).setOnClickListener {
                popupWindow.dismiss()
                onDeleteClick?.invoke(event)
            }

            // Căn lề phải menu khớp với lề phải của icon 3 chấm
            val xOffset = anchor.width - widthPx
            val yOffset = 4

            popupWindow.showAsDropDown(anchor, xOffset, yOffset)
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