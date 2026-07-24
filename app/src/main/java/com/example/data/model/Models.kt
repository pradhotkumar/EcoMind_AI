package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EcoContext(
    @Json(name = "user_name") val userName: String,
    @Json(name = "user_city") val userCity: String,
    @Json(name = "language_pref") val languagePref: String,
    @Json(name = "eco_score") val ecoScore: Int,
    @Json(name = "current_streak") val currentStreak: Int,
    @Json(name = "recent_emissions") val recentEmissions: Map<String, Double>
)

@JsonClass(generateAdapter = true)
data class EcoMindRequest(
    @Json(name = "user_id") val userId: String,
    @Json(name = "action") val action: String,
    @Json(name = "voice_text") val voiceText: String,
    @Json(name = "context") val context: EcoContext,
    @Json(name = "transport_type") val transportType: String? = null,
    @Json(name = "transport_km") val transportKm: Double? = null,
    @Json(name = "electricity_kwh") val electricityKwh: Double? = null,
    @Json(name = "food_meat_servings") val foodMeatServings: Int? = null,
    @Json(name = "food_vegan_days") val foodVeganDays: Int? = null,
    @Json(name = "waste_bags") val wasteBags: Int? = null,
    @Json(name = "water_liters") val waterLiters: Double? = null
)

@JsonClass(generateAdapter = true)
data class RecommendationDto(
    @Json(name = "title") val title: String,
    @Json(name = "category") val category: String,
    @Json(name = "co2_savings_kg") val co2SavingsKg: Double,
    @Json(name = "difficulty") val difficulty: String, // Easy, Medium, Hard
    @Json(name = "priority") val priority: String // High, Medium, Low
)

@JsonClass(generateAdapter = true)
data class EcoMindResponse(
    @Json(name = "reply_text") val replyText: String,
    @Json(name = "voice_audio_base64") val voiceAudioBase64: String?,
    @Json(name = "eco_score_delta") val ecoScoreDelta: Int?,
    @Json(name = "xp_earned") val xpEarned: Int?,
    @Json(name = "streak_updated") val streakUpdated: Int?,
    @Json(name = "recommendations_v2") val recommendations: List<RecommendationDto>?,
    @Json(name = "system_alert") val systemAlert: String?,
    @Json(name = "eco_score") val ecoScore: Int? = null,
    @Json(name = "carbon_saved") val carbonSaved: Double? = null,
    @Json(name = "streak") val streak: Int? = null
)
