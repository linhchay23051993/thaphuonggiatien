package com.linhchay.thaphuonggiatien.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchased_items")
data class PurchasedItemEntity(
    @PrimaryKey val imageResId: Int
)