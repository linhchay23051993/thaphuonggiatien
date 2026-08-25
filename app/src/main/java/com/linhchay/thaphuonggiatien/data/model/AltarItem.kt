package com.linhchay.thaphuonggiatien.data.model

data class AltarItem(
    val id: Long,
    val type: String,
    val imageResId: Int,
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Int = 200,
    var height: Int = 200,
    var batHuongId: String? = null
)
