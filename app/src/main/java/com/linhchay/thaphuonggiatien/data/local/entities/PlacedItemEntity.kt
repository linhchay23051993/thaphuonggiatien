package com.linhchay.thaphuonggiatien.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "placed_items")
data class PlacedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalId: Long,
    val type: String,
    val imageResId: Int,
    val x: Float,
    val y: Float,
    val width: Int,
    val height: Int,
    val batHuongId: String? = null,
    val price: Int
)