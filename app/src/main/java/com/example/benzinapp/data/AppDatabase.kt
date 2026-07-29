package com.example.benzinapp.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface RefuelingDao {
    @Insert
    suspend fun insertRefueling(refueling: Refueling)

    @Update
    suspend fun updateRefueling(refueling: Refueling)

    @Delete
    suspend fun deleteRefueling(refueling: Refueling)

    @Query("SELECT * FROM refuelings ORDER BY dateMillis DESC")
    fun getAllRefuelings(): Flow<List<Refueling>>

    @Query("SELECT * FROM refuelings WHERE profileId = :profileId ORDER BY dateMillis DESC")
    fun getRefuelingsForProfile(profileId: Long): Flow<List<Refueling>>

    @Query("SELECT * FROM refuelings WHERE profileId = :profileId ORDER BY dateMillis DESC LIMIT 1")
    suspend fun getLastRefuelingForProfile(profileId: Long): Refueling?

    @Query("SELECT * FROM refuelings WHERE profileId = :profileId AND dateMillis < :currentDate ORDER BY dateMillis DESC LIMIT 1")
    suspend fun getPreviousRefuelingForProfile(profileId: Long, currentDate: Long): Refueling?

    @Query("DELETE FROM refuelings WHERE profileId = :profileId")
    suspend fun deleteRefuelingsForProfile(profileId: Long)
}

@Dao
interface ProfileDao {
    @Insert
    suspend fun insertProfile(profile: Profile): Long

    @Update
    suspend fun updateProfile(profile: Profile)

    @Delete
    suspend fun deleteProfile(profile: Profile)

    @Query("SELECT * FROM profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<Profile>>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): Profile?

    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun getProfileCount(): Int
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `vehicleName` TEXT NOT NULL, `iconName` TEXT NOT NULL, `colorHex` TEXT NOT NULL)")
        db.execSQL("INSERT OR IGNORE INTO `profiles` (`id`, `name`, `vehicleName`, `iconName`, `colorHex`) VALUES (1, 'Io', 'Auto Principale', 'car', '#1E88E5')")
        db.execSQL("ALTER TABLE `refuelings` ADD COLUMN `profileId` INTEGER NOT NULL DEFAULT 1")
    }
}

@Database(entities = [Refueling::class, Profile::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun refuelingDao(): RefuelingDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "benzinapp_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

