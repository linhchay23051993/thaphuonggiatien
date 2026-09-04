package com.linhchay.thaphuonggiatien.data.local.dao

import androidx.room.*
import com.linhchay.thaphuonggiatien.data.local.entities.PlacedItemEntity
import com.linhchay.thaphuonggiatien.data.local.entities.PurchasedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AltarDao {
    @Query("SELECT * FROM placed_items")
    fun getAllPlacedItems(): Flow<List<PlacedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacedItems(items: List<PlacedItemEntity>)

    @Query("DELETE FROM placed_items")
    suspend fun deleteAllPlacedItems()

    @Query("SELECT * FROM purchased_items")
    fun getAllPurchasedItems(): Flow<List<PurchasedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPurchasedItem(item: PurchasedItemEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPurchasedItems(items: List<PurchasedItemEntity>)

    @Transaction
    suspend fun updatePlacedItems(items: List<PlacedItemEntity>) {
        deleteAllPlacedItems()
        insertPlacedItems(items)
    }
}