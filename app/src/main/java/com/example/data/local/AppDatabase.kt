package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getHistory(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getHistoryDirect(): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAll()
}

@Dao
interface EmissionDao {
    @Query("SELECT * FROM emission_entries ORDER BY timestamp DESC")
    fun getAllEmissions(): Flow<List<EmissionEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EmissionEntry)

    @Query("DELETE FROM emission_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM emission_entries")
    suspend fun clearAll()
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM achievement_badges ORDER BY unlockedAt DESC")
    fun getUnlockedBadges(): Flow<List<AchievementBadge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun unlock(badge: AchievementBadge)

    @Query("DELETE FROM achievement_badges")
    suspend fun clearAll()
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1")
    fun getProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE id = 1")
    suspend fun getProfileDirect(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfileEntity)
}

@Database(
    entities = [
        ChatMessage::class,
        EmissionEntry::class,
        AchievementBadge::class,
        UserProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun emissionDao(): EmissionDao
    abstract fun badgeDao(): BadgeDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ecomind_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
