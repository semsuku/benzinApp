package com.example.benzinapp.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface RefuelingDao {
    @Insert
    suspend fun insertRefueling(refueling: Refueling)

    @Query("SELECT * FROM refuelings ORDER BY dateMillis DESC")
    fun getAllRefuelings(): Flow<List<Refueling>>

    @Query("SELECT * FROM refuelings ORDER BY dateMillis DESC LIMIT 1")
    suspend fun getLastRefueling(): Refueling?
}

@Database(entities = [Refueling::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun refuelingDao(): RefuelingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "benzinapp_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
