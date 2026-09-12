package com.linhchay.thaphuonggiatien.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.linhchay.thaphuonggiatien.data.local.dao.AltarDao
import com.linhchay.thaphuonggiatien.data.local.dao.EventDao
import com.linhchay.thaphuonggiatien.data.local.entities.EventEntity
import com.linhchay.thaphuonggiatien.data.local.entities.PlacedItemEntity
import com.linhchay.thaphuonggiatien.data.local.entities.PurchasedItemEntity

@Database(entities = [EventEntity::class, PlacedItemEntity::class, PurchasedItemEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun altarDao(): AltarDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE placed_items ADD COLUMN isOffering INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE placed_items ADD COLUMN placedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "thap_huong_gia_tien_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}