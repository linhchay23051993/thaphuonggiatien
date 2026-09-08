package com.linhchay.thaphuonggiatien.data.model

data class Event(
    val id: Int,
    val name: String,
    val solarDate: String,
    val lunarDate: String,
    val eventDate: Long = 0L,
    val status: String = ""
)