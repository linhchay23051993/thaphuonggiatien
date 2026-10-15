package com.linhchay.thaphuonggiatien.data.model

data class Event(
    val id: Int,
    val name: String,
    val solarDate: String,
    val lunarDate: String,
    val eventDate: Long = 0L,
    val type: String = "",
    val status: String = "",
    val iconType: Int = -1 // -1 = icon_gio (mặc định), 0 = icon_lehoi, 1 = icon_ram
)