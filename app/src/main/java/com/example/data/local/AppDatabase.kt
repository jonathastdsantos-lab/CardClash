package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.model.*

@Database(
    entities = [
        UserProfile::class,
        UserInventory::class,
        BattleLog::class,
        TradeOffer::class,
        LiveMatch::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun futDao(): FutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fut_cards_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
