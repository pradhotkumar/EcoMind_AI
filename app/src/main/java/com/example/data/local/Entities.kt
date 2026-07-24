package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val senderRole: String, // "user" or "assistant"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "emission_entries")
data class EmissionEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "transport", "food", "energy"
    val amountKg: Double,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievement_badges")
data class AchievementBadge(
    @PrimaryKey val key: String, // e.g., "first_chat", "perfect_week", "streak_3"
    val title: String,
    val description: String,
    val xpEarned: Int,
    val unlockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1, // Singleton row
    val userId: String = "firebase_u1",
    val name: String = "Eco Warrior",
    val city: String = "Bengaluru",
    val languagePref: String = "English", // English, Hindi, Tamil, etc.
    val ecoScore: Int = 72,
    val currentStreak: Int = 5,
    val xp: Int = 450,
    val level: Int = 3,
    val vehicleType: String = "Electric Scooter", // Electric, Hybrid, Petrol, None
    val goal: String = "Reduce daily footprint by 20%",
    val savedCo2Kg: Double = 34.5,
    val webhookUrl: String = "https://n8n-production-c08e.up.railway.app:5678/webhook/ecomind-v7"
)
