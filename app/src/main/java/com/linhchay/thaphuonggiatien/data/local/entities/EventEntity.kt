package com.linhchay.thaphuonggiatien.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val solarDate: String,
    val lunarDate: String,
    val eventDate: Long = 0L,
    val type: String = TYPE_USER, // "USER" or "SYSTEM"
    val iconType: Int = -1 // -1 = icon_gio (mặc định), 0 = icon_lehoi, 1 = icon_ram
) {
    companion object {
        const val TYPE_USER = "USER"
        const val TYPE_SYSTEM = "SYSTEM"
    }
}