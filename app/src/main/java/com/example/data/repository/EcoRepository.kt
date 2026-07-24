package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.network.GeminiContent
import com.example.data.network.GeminiGenerateRequest
import com.example.data.network.GeminiPart
import com.example.data.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class EcoRepository(private val db: AppDatabase) {

    private val msgDao = db.chatMessageDao()
    private val emissionDao = db.emissionDao()
    private val badgeDao = db.badgeDao()
    private val profileDao = db.userProfileDao()

    val chatHistory: Flow<List<ChatMessage>> = msgDao.getHistory()
    val emissions: Flow<List<EmissionEntry>> = emissionDao.getAllEmissions()
    val unlockedBadges: Flow<List<AchievementBadge>> = badgeDao.getUnlockedBadges()
    val userProfile: Flow<UserProfileEntity?> = profileDao.getProfileFlow()

    suspend fun getProfileOrCreate(): UserProfileEntity = withContext(Dispatchers.IO) {
        var profile = profileDao.getProfileDirect()
        if (profile == null) {
            profile = UserProfileEntity()
            profileDao.insertOrUpdate(profile)
        }
        profile
    }

    suspend fun insertChatMessage(messageText: String, senderRole: String) = withContext(Dispatchers.IO) {
        val profile = getProfileOrCreate()
        msgDao.insert(
            ChatMessage(
                userId = profile.userId,
                senderRole = senderRole,
                messageText = messageText
            )
        )
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        msgDao.clearAll()
    }

    suspend fun addEmissionLog(category: String, amountKg: Double, title: String) = withContext(Dispatchers.IO) {
        emissionDao.insert(
            EmissionEntry(
                category = category,
                amountKg = amountKg,
                title = title
            )
        )

        // Award dynamic rewards & elevate XP
        val profile = getProfileOrCreate()
        val gainedXp = 25
        var currentXp = profile.xp + gainedXp
        var currentLevel = profile.level
        if (currentXp >= currentLevel * 250) {
            currentXp -= currentLevel * 250
            currentLevel += 1
            // Achieve level up badge!
            unlockBadge(
                "level_$currentLevel",
                "Level $currentLevel Achieved",
                "You reached level $currentLevel by logging eco-efforts consistently!",
                50
            )
        }

        // Adjust eco-score based on logged activities
        val ecoScoreDelta = if (amountKg < 5.0) 2 else -1
        val newEcoScore = (profile.ecoScore + ecoScoreDelta).coerceIn(30, 100)

        profileDao.insertOrUpdate(
            profile.copy(
                xp = currentXp,
                level = currentLevel,
                ecoScore = newEcoScore,
                savedCo2Kg = profile.savedCo2Kg + (if (amountKg < 10.0) 1.5 else 0.0)
            )
        )

        // Sync to N8N Gateway Webhook in real-time (saving to Firebase & returning points/badges)
        if (profile.webhookUrl.isNotEmpty()) {
            try {
                val transportType = if (category.lowercase() == "transport") "car" else "bicycle"
                val transportKm = if (category.lowercase() == "transport") amountKg else 0.0
                val electricityKwh = if (category.lowercase() == "energy") amountKg else 0.0
                val foodMeatServings = if (category.lowercase() == "food") amountKg.toInt() else 0
                val waterLiters = if (category.lowercase() == "water") amountKg else 0.0

                val payload = mapOf(
                    "user_id" to profile.userId,
                    "action" to "log_activity",
                    "transport_type" to transportType,
                    "transport_km" to transportKm,
                    "electricity_kwh" to electricityKwh,
                    "food_meat_servings" to foodMeatServings,
                    "water_liters" to waterLiters,
                    "context" to mapOf(
                        "user_name" to profile.name,
                        "user_city" to profile.city,
                        "eco_score" to profile.ecoScore.toString()
                    )
                )

                Log.d("EcoRepository", "Syncing logged activity to n8n webhook: $payload")
                val responseBody = NetworkClient.n8nService.sendRawJsonRequest(profile.webhookUrl, payload)
                val rawBody = responseBody.string()
                Log.d("EcoRepository", "Activity log response: $rawBody")

                val json = org.json.JSONObject(rawBody)
                if (json.has("status") && json.getString("status") == "success") {
                    val finalPoints = if (json.has("points_earned")) json.getInt("points_earned") else 25
                    val finalEcoDelta = if (json.has("eco_score_delta")) json.getInt("eco_score_delta") else 0
                    
                    val pFresh = getProfileOrCreate()
                    profileDao.insertOrUpdate(
                        pFresh.copy(
                            xp = pFresh.xp + finalPoints - 25, // offset our default optimistic 25 points
                            ecoScore = (pFresh.ecoScore + finalEcoDelta - ecoScoreDelta).coerceIn(10, 100)
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("EcoRepository", "Failed to sync logged activity to n8n workflow: ${e.message}")
            }
        }

        // Badge unlock triggers
        val count = emissionDao.getAllEmissions().firstOrNull()?.size ?: 0
        if (count >= 1) {
            unlockBadge("first_footprint", "Carbon Tracker", "Logged your very first emission entry!", 10)
        }
        if (count >= 5) {
            unlockBadge("footprint_veteran", "Carbon Analyst", "Logged 5 emission entries!", 30)
        }
    }

    suspend fun deleteEmissionLog(id: Int) = withContext(Dispatchers.IO) {
        emissionDao.deleteById(id)
    }

    suspend fun unlockBadge(key: String, title: String, description: String, xp: Int) = withContext(Dispatchers.IO) {
        val badge = AchievementBadge(key = key, title = title, description = description, xpEarned = xp)
        badgeDao.unlock(badge)

        val profile = getProfileOrCreate()
        profileDao.insertOrUpdate(
            profile.copy(
                xp = profile.xp + xp
            )
        )
    }

    suspend fun updateProfile(updated: UserProfileEntity) = withContext(Dispatchers.IO) {
        profileDao.insertOrUpdate(updated)
    }

    suspend fun syncProfileDynamics(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        if (profile.webhookUrl.isNotEmpty()) {
            try {
                val payload = mapOf(
                    "user_id" to profile.userId,
                    "action" to "update_profile",
                    "name" to profile.name,
                    "city" to profile.city,
                    "goals" to listOf(profile.goal),
                    "tone_pref" to "encouraging",
                    "vehicle_type" to profile.vehicleType,
                    "language_pref" to profile.languagePref.lowercase(),
                    "home_state" to "Karnataka"
                )
                Log.d("EcoRepository", "Syncing profile config to n8n webhook: $payload")
                val responseBody = NetworkClient.n8nService.sendRawJsonRequest(profile.webhookUrl, payload)
                Log.d("EcoRepository", "Profile synced successfully: ${responseBody.string()}")
            } catch (e: Exception) {
                Log.e("EcoRepository", "Failed to sync profile to n8n: ${e.message}")
            }
        }
    }

    suspend fun switchUserProfileData(email: String, selectWebhookUrl: String) = withContext(Dispatchers.IO) {
        // Retrieve existing webhook before clearing if it's customized and not default
        val existingProfile = profileDao.getProfileDirect()
        val existingWebhook = existingProfile?.webhookUrl ?: ""
        val finalWebhook = if (existingWebhook.isNotEmpty() && !existingWebhook.contains("n8n.example.com") && existingWebhook.isNotBlank()) {
            existingWebhook
        } else {
            selectWebhookUrl
        }

        // 1. Clear previous tables to avoid keeping static historical data of another user
        msgDao.clearAll()
        emissionDao.clearAll()
        badgeDao.clearAll()

        // 2. Initialize the profile according to the login details
        val sanitizedUserId = email.substringBefore("@").lowercase().replace(Regex("[^a-zA-Z0-9_]"), "")
        val nameCapitalized = email.substringBefore("@").replaceFirstChar { it.uppercase() }

        val profile = when {
            email.equals("pradhotkumar251@gmail.com", ignoreCase = true) -> {
                // Pre-seed matching Pradhot Kumar's authentic premium dashboard
                UserProfileEntity(
                    id = 1,
                    userId = sanitizedUserId,
                    name = nameCapitalized,
                    city = "Bengaluru",
                    ecoScore = 72,
                    currentStreak = 5,
                    xp = 450,
                    level = 3,
                    savedCo2Kg = 34.5,
                    webhookUrl = finalWebhook
                )
            }
            email.equals("eco.champion@gmail.com", ignoreCase = true) -> {
                // Pre-seed matching Eco Champion's authentic level
                UserProfileEntity(
                    id = 1,
                    userId = sanitizedUserId,
                    name = "Eco.champion",
                    city = "Bengaluru",
                    ecoScore = 88,
                    currentStreak = 12,
                    xp = 1200,
                    level = 5,
                    savedCo2Kg = 128.4,
                    webhookUrl = finalWebhook
                )
            }
            else -> {
                // Fresh, dynamic account matching the user email handle
                UserProfileEntity(
                    id = 1,
                    userId = sanitizedUserId,
                    name = nameCapitalized,
                    city = "Bengaluru",
                    ecoScore = 50,
                    currentStreak = 1,
                    xp = 10,
                    level = 1,
                    savedCo2Kg = 0.0,
                    webhookUrl = finalWebhook
                )
            }
        }
        
        profileDao.insertOrUpdate(profile)

        // Pre-seed corresponding user achievements/records
        if (profile.userId == "pradhotkumar251") {
            badgeDao.unlock(AchievementBadge("active_communicator", "First Connection", "Sparked an environmental dialog with EcoMind AI", 20))
            badgeDao.unlock(AchievementBadge("first_footprint", "Carbon Tracker", "Logged your very first emission entry!", 10))
        } else if (profile.userId == "ecochampion") {
            badgeDao.unlock(AchievementBadge("active_communicator", "First Connection", "Sparked an environmental dialog with EcoMind AI", 20))
            badgeDao.unlock(AchievementBadge("first_footprint", "Carbon Tracker", "Logged your very first emission entry!", 10))
            badgeDao.unlock(AchievementBadge("footprint_veteran", "Carbon Analyst", "Logged 5 emission entries!", 30))
        }

        // Insert welcoming chatbot intro
        msgDao.insert(
            ChatMessage(
                userId = profile.userId,
                senderRole = "assistant",
                messageText = "Hello, **${profile.name}**! 🌟 Welcome to your custom dashboard. Your account data has been authenticated and loaded successfully. Ask me anything about ecology or log your emissions to see updates!"
            )
        )
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        msgDao.clearAll()
        emissionDao.clearAll()
        badgeDao.clearAll()
        // Reset user profile to default guest values
        profileDao.insertOrUpdate(
            UserProfileEntity(
                id = 1,
                userId = "guest",
                name = "Guest",
                city = "Bengaluru",
                ecoScore = 50,
                currentStreak = 1,
                xp = 10,
                level = 1,
                savedCo2Kg = 0.0,
                webhookUrl = ""
            )
        )
    }

    /**
     * Dual API Service Connector
     * Tries: n8n webhook API
     * Fallback: Generative Gemini REST API using BuildConfig
     * Deep Fallback: Offline local sustainability reasoning engine
     */
    suspend fun getAiResponse(userMessage: String): EcoMindResponse = withContext(Dispatchers.IO) {
        val profile = getProfileOrCreate()
        val recentEmissionsList = emissionDao.getAllEmissions().firstOrNull() ?: emptyList()
        val recentEmissionsMap = recentEmissionsList.groupBy { it.category }.mapValues { entry ->
            entry.value.sumOf { it.amountKg }
        }

        val payload = mapOf(
            "user_id" to profile.userId,
            "message" to userMessage
        )

        var n8nError: String? = null

        // 1. Attempt N8N Webhook Call
        if (profile.webhookUrl.isNotEmpty()) {
            try {
                Log.d("EcoRepository", "Attempting N8N Call to: ${profile.webhookUrl}")
                val response = NetworkClient.n8nService.sendRawJsonRequest(profile.webhookUrl, payload)
                val rawBody = response.string()
                Log.d("EcoRepository", "N8N response fetched successfully. Length: ${rawBody.length}")
                return@withContext parseN8nResponse(rawBody)
            } catch (e: Exception) {
                n8nError = e.localizedMessage ?: e.message ?: "Unknown Connection Error"
                Log.e("EcoRepository", "N8N webhooks call failed: $n8nError. Falling back directly to Gemini API.", e)
            }
        }

        // 2. Attempt Google Gemini API Call
        val geminiKey = BuildConfig.GEMINI_API_KEY
        if (geminiKey.isNotEmpty() && geminiKey != "MY_GEMINI_API_KEY") {
            try {
                Log.d("EcoRepository", "Attempting direct Gemini REST API call as backup")
                val systemPrompt = """
                    You are EcoMind AI, a premium futuristic sustainability assistant. 
                    Your personality is charming, helpful, smart, and deeply committed to environmental protection. 
                    Style: Mix of friendly ChatGPT intelligence, encouraging Duolingo streak feedback, and metrics-driven Apple Health precision.
                    User data:
                    - Name: ${profile.name}
                    - City: ${profile.city}
                    - Current eco-score: ${profile.ecoScore}/100
                    - Active Day Streak: ${profile.currentStreak} days
                    - Current level: ${profile.level}, XP: ${profile.xp}
                    - Vehicle: ${profile.vehicleType}
                    - Recent emissions log summary: $recentEmissionsMap (in kg CO₂)
                    
                    Respond to the user's message thoughtfully in their preferred language (${profile.languagePref}). 
                    Give actionable, smart carbon saving suggestions, small lifestyle changes, and encouragement. Keep replies concise and beautifully formatted with bullet points if helpful.
                """.trimIndent()

                val prompt = "$systemPrompt\n\nUser message: $userMessage\n\nResponse:"
                val geminiRequest = GeminiGenerateRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = prompt))
                        )
                    )
                )

                val response = NetworkClient.geminiService.generateContent(geminiKey, geminiRequest)
                val replyText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "EcoMind is processing your query..."

                // Build recommendations dynamically from Gemini's context or static list for consistency
                val suggestions = generateSmartSuggestionsForCategory(recentEmissionsMap)

                return@withContext EcoMindResponse(
                    replyText = replyText,
                    voiceAudioBase64 = null,
                    ecoScoreDelta = 2,
                    xpEarned = 15,
                    streakUpdated = profile.currentStreak,
                    recommendations = suggestions,
                    systemAlert = if (n8nError != null) "gemini_fallback: N8N Error ($n8nError)" else "Powered by backup Gemini-3.5-Engine"
                )

            } catch (e: Exception) {
                Log.e("EcoRepository", "Backup Gemini REST API failed: ${e.message}. Using deep local sustainability engine.", e)
            }
        }

        // 3. Fallback: Intelligent Local Rules sustainability engine
        val replyLocalText = getOfflineSustainabilityReply(userMessage, profile, recentEmissionsMap)
        val localSuggestions = generateSmartSuggestionsForCategory(recentEmissionsMap)

        EcoMindResponse(
            replyText = replyLocalText,
            voiceAudioBase64 = null,
            ecoScoreDelta = 1,
            xpEarned = 10,
            streakUpdated = profile.currentStreak,
            recommendations = localSuggestions,
            systemAlert = if (n8nError != null) "local_fallback: N8N Error ($n8nError)" else "Offline EcoMind Assist Active"
        )
    }

    private fun generateSmartSuggestionsForCategory(emissionsMap: Map<String, Double>): List<RecommendationDto> {
        val list = mutableListOf<RecommendationDto>()
        
        val transportEmissions = emissionsMap["transport"] ?: 0.0
        val energyEmissions = emissionsMap["energy"] ?: 0.0
        val foodEmissions = emissionsMap["food"] ?: 0.0

        if (transportEmissions > 15 || emissionsMap.isEmpty()) {
            list.add(
                RecommendationDto(
                    title = "Work commute by public metro or electric scooter",
                    category = "transport",
                    co2SavingsKg = 8.4,
                    difficulty = "Medium",
                    priority = "High"
                )
            )
        }
        if (energyEmissions > 5 || emissionsMap.isEmpty()) {
            list.add(
                RecommendationDto(
                    title = "Switch off heavy appliances during peak load hours",
                    category = "energy",
                    co2SavingsKg = 3.2,
                    difficulty = "Easy",
                    priority = "Medium"
                )
            )
        }
        if (foodEmissions > 4 || emissionsMap.isEmpty()) {
            list.add(
                RecommendationDto(
                    title = "Incorporate a plant-based food meal today",
                    category = "food",
                    co2SavingsKg = 2.1,
                    difficulty = "Easy",
                    priority = "Low"
                )
            )
        }
        if (list.isEmpty()) {
            list.add(
                RecommendationDto(
                    title = "Car pool with neighbors on weekly chores",
                    category = "transport",
                    co2SavingsKg = 5.0,
                    difficulty = "Easy",
                    priority = "High"
                )
            )
        }
        return list
    }

    private fun getOfflineSustainabilityReply(query: String, profile: UserProfileEntity, emissionsMap: Map<String, Double>): String {
        val q = query.lowercase()
        return when {
            q.contains("hello") || q.contains("hi") || q.contains("hey") -> {
                "Hello ${profile.name}! 🌱 I'm EcoMind, your offline sustainability assistant. Let's make some simple, green choices today. Your current eco-score is ${profile.ecoScore}/100. Ask me about carbon footprints, green tips, or log some entries!"
            }
            q.contains("score") || q.contains("level") || q.contains("streak") -> {
                "You are at Level ${profile.level} with ${profile.xp} XP and an active streak of ${profile.currentStreak} days! Keep tracking your footprint to boost your eco score of ${profile.ecoScore}."
            }
            q.contains("transport") || q.contains("car") || q.contains("drive") || q.contains("travel") -> {
                "Transport represents about 25% of global energy-related greenhouse gases. Taking a public bus, ride-sharing, or switching to your ${profile.vehicleType} will save approx. 5-10kg CO₂ per trip. Try to log a public commute!"
            }
            q.contains("food") || q.contains("meat") || q.contains("dairy") || q.contains("eat") -> {
                "Sustainable dining has high impact! Going meat-free for just one day saves over 3kg of carbon footprint, 1500 gallons of water, and protects forest cover. I recommend trying an organic local vegetable meal."
            }
            q.contains("energy") || q.contains("electricity") || q.contains("power") || q.contains("light") -> {
                "Heating and electricity use substantial fuel loads. Simple tips: shift AC up to 24°C, replace older lights with modern low-power LEDs, and unplug adapters when not charging. It preserves power and trims carbon footprints!"
            }
            q.contains("report") || q.contains("weekly") || q.contains("insight") -> {
                "For your weekly report: you've saved ${profile.savedCo2Kg}kg of CO₂ overall. Your biggest emissions come from transport. Try substituting driving with cycling to further accelerate footprint reduction!"
            }
            else -> {
                "Excellent point! Carbon reduction starts with conscious habits. Offline suggestion: reduce plastic waste, use compostable packaging, and log transport or energy categories daily to analyze footprint trends."
            }
        }
    }

    /**
     * Seeds initial records so charts, badges, and emissions are populated instantly on first load.
     */
    suspend fun seedMockData() = withContext(Dispatchers.IO) {
        val profileFlow = profileDao.getProfileDirect()
        if (profileFlow == null) {
            profileDao.insertOrUpdate(UserProfileEntity())
        }
    }

    suspend fun writeProfileAndEmissionsToFirebase(firebaseDbUrl: String, profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        val sanitizedDbUrl = if (firebaseDbUrl.endsWith("/")) firebaseDbUrl else "$firebaseDbUrl/"
        val requestUrl = "${sanitizedDbUrl}users/${profile.userId}.json"
        try {
            val emissionsList = emissionDao.getAllEmissions().firstOrNull() ?: emptyList()
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val dailyEmissions = mutableMapOf<String, Any>()
            
            val grouped = emissionsList.groupBy { sdf.format(java.util.Date(it.timestamp)) }
            for ((dateStr, logs) in grouped) {
                val transportCo2 = logs.filter { it.category == "transport" }.sumOf { it.amountKg }
                val energyCo2 = logs.filter { it.category == "energy" }.sumOf { it.amountKg }
                val foodCo2 = logs.filter { it.category == "food" }.sumOf { it.amountKg }
                val wasteCo2 = logs.filter { it.category == "waste" }.sumOf { it.amountKg }
                val waterCo2 = logs.filter { it.category == "water" }.sumOf { it.amountKg }
                val totalCo2 = logs.sumOf { it.amountKg }
                
                dailyEmissions[dateStr] = mapOf(
                    "transport_co2" to transportCo2,
                    "energy_co2" to energyCo2,
                    "food_co2" to foodCo2,
                    "waste_co2" to wasteCo2,
                    "water_co2" to waterCo2,
                    "total_co2" to totalCo2
                )
            }
            
            val agentMemory = mapOf(
                "eco_score" to profile.ecoScore,
                "motivation" to (profile.xp + (profile.level - 1) * 250),
                "streak" to profile.currentStreak
            )
            
            val behaviorPatterns = mapOf(
                "fav_category" to profile.vehicleType
            )

            // Gather and embed actual local chat history
            val chats = msgDao.getHistoryDirect()
            val chatHistoryList = chats.map {
                mapOf(
                    "sender" to it.senderRole,
                    "text" to it.messageText,
                    "timestamp" to it.timestamp
                )
            }
            
            val payload = mapOf(
                "agent_memory" to agentMemory,
                "behavior_patterns" to behaviorPatterns,
                "emissions" to mapOf("daily" to dailyEmissions),
                "chat_history" to chatHistoryList,
                "name" to profile.name,
                "city" to profile.city,
                "goal" to profile.goal,
                "language_pref" to profile.languagePref,
                "webhook_url" to profile.webhookUrl
            )
            
            Log.d("EcoRepository", "Writing profile state to Firebase at: $requestUrl")
            val response = NetworkClient.n8nService.putRawJson(requestUrl, payload)
            Log.d("EcoRepository", "Firebase write response: ${response.string()}")
        } catch (e: Exception) {
            Log.e("EcoRepository", "Failed to write profile state to Firebase: ${e.message}")
        }
    }

    private fun parseN8nResponse(rawBody: String): EcoMindResponse {
        try {
            val trimmed = rawBody.trim()
            if (trimmed.startsWith("{")) {
                val json = org.json.JSONObject(trimmed)
                // Check multiple possible keys for the reply text
                val replyText = when {
                    json.has("reply") -> json.getString("reply")
                    json.has("ai_text") -> json.getString("ai_text")
                    json.has("reply_text") -> json.getString("reply_text")
                    json.has("replyText") -> json.getString("replyText")
                    json.has("response") -> json.getString("response")
                    json.has("text") -> json.getString("text")
                    json.has("message") -> json.getString("message")
                    json.has("output") -> json.getString("output")
                    json.has("content") -> json.getString("content")
                    else -> {
                        // Look for any string field dynamically if the common ones don't exist
                        var found: String? = null
                        val keys = json.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val value = json.opt(key)
                            if (value is String && value.isNotBlank() && key != "voice_audio_base64" && key != "system_alert") {
                                found = value
                                break
                            }
                        }
                        found ?: trimmed
                    }
                }

                // Check possible keys for recommendations_v2 or recommendations
                val recommendationsList = mutableListOf<RecommendationDto>()
                val recommendationsArray = when {
                    json.has("recommendations_v2") -> json.optJSONArray("recommendations_v2")
                    json.has("recommendations") -> json.optJSONArray("recommendations")
                    else -> null
                }

                if (recommendationsArray != null) {
                    for (i in 0 until recommendationsArray.length()) {
                        try {
                            val item = recommendationsArray.getJSONObject(i)
                            recommendationsList.add(
                                RecommendationDto(
                                    title = item.optString("title", "Eco Suggestion"),
                                    category = item.optString("category", "general"),
                                    co2SavingsKg = item.optDouble("co2_savings_kg", item.optDouble("co2SavingsKg", 1.0)),
                                    difficulty = item.optString("difficulty", "Easy"),
                                    priority = item.optString("priority", "Medium")
                                )
                            )
                        } catch (itemEx: Exception) {
                            Log.e("EcoRepository", "Failed to parse individual recommendation item from N8N response", itemEx)
                        }
                    }
                }

                val ecoScore = if (json.has("eco_score")) json.optInt("eco_score") else if (json.has("ecoScore")) json.optInt("ecoScore") else null
                val carbonSaved = if (json.has("carbon_saved")) json.optDouble("carbon_saved") else if (json.has("carbonSaved")) json.optDouble("carbonSaved") else null
                val streak = if (json.has("streak")) json.optInt("streak") else null

                return EcoMindResponse(
                    replyText = replyText,
                    voiceAudioBase64 = if (json.has("voice_audio_base64")) json.optString("voice_audio_base64") else if (json.has("voiceAudioBase64")) json.optString("voiceAudioBase64") else null,
                    ecoScoreDelta = if (json.has("eco_score_delta")) json.optInt("eco_score_delta") else if (json.has("ecoScoreDelta")) json.optInt("ecoScoreDelta") else 0,
                    xpEarned = if (json.has("xp_earned")) json.optInt("xp_earned") else if (json.has("xpEarned")) json.optInt("xpEarned") else 10,
                    streakUpdated = if (json.has("streak_updated")) json.optInt("streak_updated") else if (json.has("streakUpdated")) json.optInt("streakUpdated") else null,
                    recommendations = if (recommendationsList.isNotEmpty()) recommendationsList else null,
                    systemAlert = if (json.has("system_alert")) json.optString("system_alert") else if (json.has("systemAlert")) json.optString("systemAlert") else "n8n_active",
                    ecoScore = ecoScore,
                    carbonSaved = carbonSaved,
                    streak = streak
                )
            } else if (trimmed.startsWith("[")) {
                // If it's a JSON array of responses, parse the first element or combine them
                val jsonArray = org.json.JSONArray(trimmed)
                if (jsonArray.length() > 0) {
                    val first = jsonArray.get(0)
                    if (first is org.json.JSONObject) {
                        return parseN8nResponse(first.toString())
                    } else {
                        return EcoMindResponse(
                            replyText = first.toString(),
                            voiceAudioBase64 = null,
                            ecoScoreDelta = 0,
                            xpEarned = 10,
                            streakUpdated = null,
                            recommendations = null,
                            systemAlert = "n8n_active"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("EcoRepository", "JSON parsing failed for N8N stream: ${e.message}. Treating as raw string content.")
        }

        // If not JSON or parsing failed completely, return raw plain text response!
        return EcoMindResponse(
            replyText = rawBody,
            voiceAudioBase64 = null,
            ecoScoreDelta = 0,
            xpEarned = 10,
            streakUpdated = null,
            recommendations = null,
            systemAlert = "n8n_active"
        )
    }

    suspend fun syncFromFirebase(firebaseDbUrl: String, userId: String): String = withContext(Dispatchers.IO) {
        val sanitizedDbUrl = if (firebaseDbUrl.endsWith("/")) firebaseDbUrl else "$firebaseDbUrl/"
        val requestUrl = "${sanitizedDbUrl}users/${userId}.json"
        try {
            Log.d("EcoRepository", "Syncing from Firebase URL: $requestUrl")
            val responseBody = NetworkClient.n8nService.getRawJson(requestUrl)
            val rawBody = responseBody.string().trim()
            Log.d("EcoRepository", "Firebase response: $rawBody")
            
            if (rawBody == "null" || rawBody.isEmpty() || rawBody == "{}") {
                return@withContext "User ID '$userId' not found on Firebase Realtime Database. Please double-check the ID."
            }
            
            val json = org.json.JSONObject(rawBody)
            
            // 1. Fetch current profile
            val currentProfile = getProfileOrCreate()
            
            // 2. Parse agent_memory
            var score = currentProfile.ecoScore
            var xp = currentProfile.xp
            var streak = currentProfile.currentStreak
            var level = currentProfile.level
            
            if (json.has("agent_memory")) {
                val agentMemory = json.getJSONObject("agent_memory")
                if (agentMemory.has("eco_score")) {
                    score = agentMemory.getInt("eco_score")
                }
                if (agentMemory.has("motivation")) {
                    xp = agentMemory.getInt("motivation")
                }
                if (agentMemory.has("streak")) {
                    streak = agentMemory.getInt("streak")
                }
            }
            
            // Determine level based on XP / Motivation
            level = (xp / 250) + 1
            if (level < 1) level = 1
            val cleanXp = xp % 250
            
            // Let's get vehicle pref from behavior_patterns
            var vehicle = currentProfile.vehicleType
            if (json.has("behavior_patterns")) {
                val behaviorPatterns = json.getJSONObject("behavior_patterns")
                if (behaviorPatterns.has("fav_category")) {
                    vehicle = behaviorPatterns.getString("fav_category")
                }
            }
            
            // 3. Parse emissions -> daily and insert into local Room DB
            var co2Sum = 0.0
            if (json.has("emissions")) {
                val emissionsObj = json.getJSONObject("emissions")
                if (emissionsObj.has("daily")) {
                    val dailyObj = emissionsObj.getJSONObject("daily")
                    
                    // Clear previous emissions first to match server exactly and prevent duplicate entries
                    emissionDao.clearAll()
                    
                    val dates = dailyObj.keys()
                    while (dates.hasNext()) {
                        val dateStr = dates.next()
                        val dayLog = dailyObj.optJSONObject(dateStr) ?: continue
                        
                        // Parse values safely (supporting integer or double)
                        val energy = dayLog.optDouble("energy_co2", 0.0)
                        val food = dayLog.optDouble("food_co2", 0.0)
                        val transport = dayLog.optDouble("transport_co2", 0.0)
                        val waste = dayLog.optDouble("waste_co2", 0.0)
                        val water = dayLog.optDouble("water_co2", 0.0)
                        
                        val total = dayLog.optDouble("total_co2", 0.0)
                        co2Sum += total
                        
                        // Insert each category if non-zero
                        if (transport > 0.0) {
                            emissionDao.insert(EmissionEntry(category = "transport", amountKg = transport, title = "Transport logged on $dateStr"))
                        }
                        if (energy > 0.0) {
                            emissionDao.insert(EmissionEntry(category = "energy", amountKg = energy, title = "Energy logged on $dateStr"))
                        }
                        if (food > 0.0) {
                            emissionDao.insert(EmissionEntry(category = "food", amountKg = food, title = "Food logged on $dateStr"))
                        }
                        if (waste > 0.0) {
                            emissionDao.insert(EmissionEntry(category = "waste", amountKg = waste, title = "Waste logged on $dateStr"))
                        }
                        if (water > 0.0) {
                            emissionDao.insert(EmissionEntry(category = "water", amountKg = water, title = "Water logged on $dateStr"))
                        }
                    }
                }
            }
            
            // 3.5 Parse chat_history and insert into local Room DB if online records exist
            if (json.has("chat_history")) {
                val chatArray = json.optJSONArray("chat_history")
                if (chatArray != null && chatArray.length() > 0) {
                    msgDao.clearAll()
                    for (i in 0 until chatArray.length()) {
                        val chatObj = chatArray.optJSONObject(i) ?: continue
                        val sender = chatObj.optString("sender", "user")
                        val text = chatObj.optString("text", "")
                        val ts = chatObj.optLong("timestamp", System.currentTimeMillis())
                        if (text.isNotEmpty()) {
                            msgDao.insert(
                                ChatMessage(
                                    userId = userId,
                                    senderRole = sender,
                                    messageText = text,
                                    timestamp = ts
                                )
                            )
                        }
                    }
                }
            }
            
            // 4. Update core profile
            val updatedProfile = currentProfile.copy(
                userId = userId,
                ecoScore = score.coerceIn(10, 100),
                xp = cleanXp,
                level = level,
                currentStreak = streak,
                vehicleType = vehicle,
                savedCo2Kg = co2Sum
            )
            profileDao.insertOrUpdate(updatedProfile)
            
            return@withContext "Success: Synced user '$userId' successfully! Loaded Score: $score, Motivation (XP): $xp, Streak: $streak, Total CO2: $co2Sum kg."
        } catch (e: Exception) {
            Log.e("EcoRepository", "Failed to sync from Firebase: ${e.message}")
            return@withContext "Error: Failed to fetch/parse from Firebase Realtime Database. [Details: ${e.message}]"
        }
    }
}
