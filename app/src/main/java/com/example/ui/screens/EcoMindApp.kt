package com.example.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.AgentActivityLog

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.ChatMessage
import com.example.data.local.EmissionEntry
import com.example.data.local.UserProfileEntity
import com.example.data.model.RecommendationDto
import com.example.ui.theme.*
import com.example.ui.viewmodel.EcoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Navigation items definition
sealed class Screen(val route: String, val title: String, val selectIcon: androidx.compose.ui.graphics.vector.ImageVector, val unselectIcon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Chat : Screen("chat", "AI Chat", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Analytics, Icons.Outlined.Analytics)
    object Rewards : Screen("rewards", "Rewards", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents)
    object Reports : Screen("reports", "Reports", Icons.Filled.Assessment, Icons.Outlined.Assessment)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EcoMindApp(viewModel: EcoViewModel) {
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = profileState ?: UserProfileEntity()

    val orbState by viewModel.orbState.collectAsStateWithLifecycle()
    val voiceStatus by viewModel.voiceStatus.collectAsStateWithLifecycle()
    val isInWebCall by viewModel.isInWebCall.collectAsStateWithLifecycle()

    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = isUserLoggedIn,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally(animationSpec = tween(500)) { width -> width } + fadeIn(animationSpec = tween(500))) togetherWith
                        (slideOutHorizontally(animationSpec = tween(500)) { width -> -width } + fadeOut(animationSpec = tween(500)))
            } else {
                (slideInHorizontally(animationSpec = tween(500)) { width -> -width } + fadeIn(animationSpec = tween(500))) togetherWith
                        (slideOutHorizontally(animationSpec = tween(500)) { width -> width } + fadeOut(animationSpec = tween(500)))
            }
        },
        label = "login_transition_system"
    ) { loggedIn ->
        if (loggedIn) {
            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "EcoMind",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = ElectricEmerald
                                    )
                                    Text(
                                        text = "AI",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .background(NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                        color = NeonCyan
                                    )
                                }
                            },
                            actions = {
                                // Flame Streak
                                Row(
                                    modifier = Modifier
                                        .background(DarkCard, RoundedCornerShape(12.dp))
                                        .border(1.dp, AccentGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = AccentGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${profile.currentStreak} Days",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = AccentGold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))

                                // Floating assistant orb in the Header
                                FloatingOrbHeader(
                                    orbState = orbState,
                                    tooltipText = voiceStatus,
                                    onOrbClick = {
                                        if (micPermissionState.status.isGranted) {
                                            if (isInWebCall) {
                                                viewModel.endWebCall()
                                            } else {
                                                viewModel.startWebCall()
                                            }
                                        } else {
                                            micPermissionState.launchPermissionRequest()
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = DarkBg,
                                titleContentColor = TextPrimary
                            )
                        )
                    },
                    bottomBar = {
                        EcoBottomNavBar(
                            navController = navController,
                            currentRoute = currentRoute
                        )
                    },
                    containerColor = DarkBg
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Radial background glow for futuristic vibe
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    val glowBrush = Brush.radialGradient(
                                        colors = listOf(
                                            ElectricEmerald.copy(alpha = 0.08f),
                                            Color.Transparent
                                        ),
                                        radius = size.width * 0.8f
                                    )
                                    drawRect(glowBrush)
                                }
                        )

                        NavHost(
                            navController = navController,
                            startDestination = Screen.Home.route
                        ) {
                            composable(Screen.Home.route) {
                                HomeScreen(viewModel = viewModel, onNavigateToChat = {
                                    navController.navigate(Screen.Chat.route)
                                })
                            }
                            composable(Screen.Chat.route) {
                                ChatScreen(viewModel = viewModel)
                            }
                            composable(Screen.Dashboard.route) {
                                DashboardScreen(viewModel = viewModel)
                            }
                            composable(Screen.Rewards.route) {
                                RewardsScreen(viewModel = viewModel)
                            }
                            composable(Screen.Reports.route) {
                                ReportsScreen(viewModel = viewModel)
                            }
                            composable(Screen.Profile.route) {
                                ProfileScreen(viewModel = viewModel)
                            }
                        }
                    }
                }

                if (isInWebCall) {
                    WebCallOverlay(viewModel = viewModel)
                }
            }
        } else {
            LoginScreen(viewModel = viewModel)
        }
    }
}

// Custom Bottom Nav bar
@Composable
fun EcoBottomNavBar(
    navController: NavController,
    currentRoute: String
) {
    val items = listOf(
        Screen.Home,
        Screen.Chat,
        Screen.Dashboard,
        Screen.Rewards,
        Screen.Reports,
        Screen.Profile
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        color = Color.Transparent,
        contentColor = TextPrimary
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = DarkCard.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(1.dp, StrokeHighlight, RoundedCornerShape(24.dp))
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    val tint by animateColorAsState(
                        targetValue = if (isSelected) ElectricEmerald else TextSecondary,
                        animationSpec = tween(300),
                        label = "NavColor"
                    )

                    Column(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isSelected) screen.selectIcon else screen.unselectIcon,
                            contentDescription = screen.title,
                            tint = tint,
                            modifier = Modifier
                                .size(22.dp)
                        )
                        Text(
                            text = screen.title,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = tint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// 1. HOME SCREEN SECTION
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: EcoViewModel,
    onNavigateToChat: () -> Unit
) {
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = profileState ?: UserProfileEntity()
    val suggestions by viewModel.recommendations.collectAsStateWithLifecycle()

    var triviaAnswered by remember { mutableStateOf<Boolean?>(null) }
    var triviaResultText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome panel with Glassmorphic gradient card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(DarkCard, SoftGlassOverlay)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(1.dp, StrokeHighlight, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Good day, ${profile.name}! 🌟",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Your carbon score reflects active sustainability habits. Let's make some simple gains today.",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        StreakFlameAndLeaf(days = profile.currentStreak)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Progress indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Eco level", fontSize = 11.sp, color = TextSecondary)
                                Text("Lv. ${profile.level}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ElectricEmerald)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Carbon Saved", fontSize = 11.sp, color = TextSecondary)
                                Text("${String.format("%.1f", profile.savedCo2Kg)} kg", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("XP", fontSize = 11.sp, color = TextSecondary)
                                Text("${profile.xp}/750", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                            }
                        }
                        LinearProgressIndicator(
                            progress = { (profile.xp % 250) / 250f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = ElectricEmerald,
                            trackColor = DarkBg
                        )
                    }
                }
            }
        }

        // Concentric Eco score metrics wheel card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCard, RoundedCornerShape(20.dp))
                    .border(1.dp, StrokeHighlightCyan, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Radial score arc drawn via standard Jetpack Canvas
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val targetScore = profile.ecoScore.toFloat()
                    val animatedScore by animateFloatAsState(
                        targetValue = targetScore,
                        animationSpec = tween(1200, easing = LinearOutSlowInEasing),
                        label = "EcoScore"
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Background track arc
                        drawArc(
                            color = Color(0xFF1E2E38),
                            startAngle = -220f,
                            sweepAngle = 260f,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // Active gradient arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(ElectricEmerald, NeonCyan, ElectricEmerald)
                            ),
                            startAngle = -220f,
                            sweepAngle = (animatedScore / 100f) * 260f,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${animatedScore.toInt()}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricEmerald
                        )
                        Text(
                            text = "Score",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Premium Eco Score",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Calculated on transport, electricity and meat logs. Great job!",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToChat,
                            modifier = Modifier.height(36.dp).weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricEmerald)
                        ) {
                            Icon(Icons.Filled.AutoAwesome, "AI", modifier = Modifier.size(14.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ask AI", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                if (micPermissionState.status.isGranted) {
                                    viewModel.startWebCall()
                                } else {
                                    micPermissionState.launchPermissionRequest()
                                }
                            },
                            modifier = Modifier.height(36.dp).weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricEmerald),
                            border = BorderStroke(1.dp, ElectricEmerald)
                        ) {
                            Icon(Icons.Filled.Call, "Web Call", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Web Call", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Autonomous AI Agent Live Telemetry Console
        item {
            AgentActivityConsoleCard(viewModel = viewModel)
        }

        // Daily Trivia Quiz Card (Duolingo Style Gamification)

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StrokeHighlight, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SoftGlassOverlay)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Extension,
                            contentDescription = "Quiz",
                            tint = AccentGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Daily Carbon Trivia Quiz",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Which of these travel alternatives generates the lowest carbon emission metrics per km?",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    if (triviaAnswered == null) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "Petrol SUV (Passenger size: 1)" to false,
                                "Standard Electric Bus (Grid source model)" to true,
                                "Domestic Regional Flight tour" to false
                            ).forEach { (option, correct) ->
                                Button(
                                    onClick = {
                                        triviaAnswered = correct
                                        triviaResultText = if (correct) {
                                            "Superb choice! +40 XP awarded. Clean grids make transit standard solutions."
                                        } else {
                                            "Incorrect. SUV & flight sectors produce some of the heaviest emission spikes."
                                        }
                                        viewModel.triggerQuizAnswer(correct)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(option, fontSize = 12.sp, color = TextPrimary)
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (triviaAnswered == true) ElectricEmerald.copy(alpha = 0.15f) else HighCarbonRed.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (triviaAnswered == true) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                    contentDescription = "Status",
                                    tint = if (triviaAnswered == true) ElectricEmerald else HighCarbonRed
                                )
                                Text(
                                    text = triviaResultText,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Smart Recommendations Checklist
        item {
            Text(
                text = "Smart Recommended Action Items",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        items(suggestions) { rec ->
            RecommendationItem(
                rec = rec,
                onMarkDone = {
                    // Instantly trigger logging to DB based on action item click
                    viewModel.addEmission(
                        category = rec.category,
                        amountKg = 0.0, // savings
                        title = "Achieved recommendation: ${rec.title}"
                    )
                }
            )
        }
    }
}

// 2. AI CHAT SCREEN SECTION (ChatGPT + AI Voice Playback + suggested prompts)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(viewModel: EcoViewModel) {
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val orbState by viewModel.orbState.collectAsStateWithLifecycle()
    val voiceStatus by viewModel.voiceStatus.collectAsStateWithLifecycle()
    val isInWebCall by viewModel.isInWebCall.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val suggestedPrompts = listOf(
        "Suggest vegan recipes with low CO₂ values",
        "Explain grid carbon offset benefits",
        "Tips to reduce vehicle commuter footprints"
    )

    // Scroll to bottom on updates
    LaunchedEffect(chatHistory.size, isLoading) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Chat header with speech play toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(ElectricEmerald, CircleShape)
                )
                Text("Chat Coach", style = MaterialTheme.typography.titleSmall, color = TextPrimary)

                val lastBackend by viewModel.aiLastBackendUsed.collectAsStateWithLifecycle()
                val lastError by viewModel.aiLastBackendError.collectAsStateWithLifecycle()
                var showDiagnosticDialog by remember { mutableStateOf(false) }

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = when (lastBackend) {
                                "N8N_ACTIVE" -> ElectricEmerald.copy(alpha = 0.15f)
                                "GEMINI_FALLBACK" -> AccentGold.copy(alpha = 0.15f)
                                else -> TextSecondary.copy(alpha = 0.1f)
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = when (lastBackend) {
                                "N8N_ACTIVE" -> ElectricEmerald
                                "GEMINI_FALLBACK" -> AccentGold
                                else -> TextSecondary.copy(alpha = 0.3f)
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { showDiagnosticDialog = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (lastBackend) {
                            "N8N_ACTIVE" -> "n8n Connected"
                            "GEMINI_FALLBACK" -> "Gemini Backup"
                            "LOCAL_FALLBACK" -> "Local Offline"
                            else -> "n8n Inactive"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (lastBackend) {
                            "N8N_ACTIVE" -> ElectricEmerald
                            "GEMINI_FALLBACK" -> AccentGold
                            else -> TextSecondary
                        }
                    )
                }

                if (showDiagnosticDialog) {
                    AlertDialog(
                        onDismissRequest = { showDiagnosticDialog = false },
                        containerColor = DarkCard,
                        title = {
                            Text("Backend Route Information", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        },
                        text = {
                            Column {
                                Text(
                                    text = "Current Status: " + when (lastBackend) {
                                        "N8N_ACTIVE" -> "Connected directly to your customized N8N Webhook workflow."
                                        "GEMINI_FALLBACK" -> "Fell back to Gemini-3.5-Engine backup."
                                        "LOCAL_FALLBACK" -> "Fell back to Local Rules Offline logic."
                                        else -> "Waiting for first chat..."
                                    },
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                                if (lastError.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Routing Log / Error detail:",
                                        color = HighCarbonRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .border(1.dp, StrokeHighlight, RoundedCornerShape(4.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text(lastError, color = TextPrimary, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showDiagnosticDialog = false }) {
                                Text("Acknowledge", color = ElectricEmerald)
                            }
                        }
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Web calling launch trigger
                IconButton(onClick = {
                    if (micPermissionState.status.isGranted) {
                        viewModel.startWebCall()
                    } else {
                        micPermissionState.launchPermissionRequest()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Start Voice Web Call",
                        tint = ElectricEmerald
                    )
                }
            }
        }

        // Conversation history list
        Box(modifier = Modifier.weight(1f)) {
            if (chatHistory.isEmpty()) {
                // Empty state instructions
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "EcoMind",
                        tint = ElectricEmerald.copy(alpha = 0.5f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Start your Sustainability Dialogue",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Ask about eco measures, request emissions estimations, or tap the microphone to chat vocally in Hindi and Indian regional languages.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatHistory) { msg ->
                        ChatBubbleItem(message = msg)
                    }

                    // Loading/typing indicator dots
                    if (isLoading) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
            }
        }

        // Suggested prompts lists
        if (chatHistory.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestedPrompts.forEach { text ->
                    Box(
                        modifier = Modifier
                            .background(DarkCard, RoundedCornerShape(12.dp))
                            .border(1.dp, StrokeHighlight, RoundedCornerShape(12.dp))
                            .clickable {
                                textInput = text
                                viewModel.sendMessage(text)
                                textInput = ""
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(text, fontSize = 11.sp, color = NeonCyan)
                    }
                }
            }
        }

        // Custom Soundwave voice active graphics
        if (orbState == "listening" || orbState == "speaking") {
            SoundWaveGraphic(orbState = orbState)
            Text(
                text = voiceStatus,
                fontSize = 11.sp,
                color = NeonCyan,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )
        }

        // Bottom text field and voice speech recognitions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Speech recording mic button
            IconButton(
                onClick = {
                    if (micPermissionState.status.isGranted) {
                        if (isInWebCall) {
                            viewModel.endWebCall()
                        } else {
                            viewModel.startWebCall()
                        }
                    } else {
                        micPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isInWebCall) HighCarbonRed.copy(alpha = 0.2f) else SoftGlassOverlay,
                        CircleShape
                    )
                    .border(
                        1.dp,
                        if (isInWebCall) HighCarbonRed else ElectricEmerald,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isInWebCall) Icons.Filled.CallEnd else Icons.Filled.Mic,
                    contentDescription = "Voice Input",
                    tint = if (isInWebCall) HighCarbonRed else ElectricEmerald
                )
            }

            // Input TextField
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_text_input"),
                placeholder = { Text("Ask anything sustainable...", fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricEmerald,
                    unfocusedBorderColor = StrokeHighlight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                trailingIcon = {
                    if (textInput.isNotBlank()) {
                        IconButton(
                            onClick = {
                                viewModel.sendMessage(textInput)
                                textInput = ""
                            }
                        ) {
                            Icon(Icons.Filled.Send, "Send", tint = ElectricEmerald)
                        }
                    }
                }
            )
        }
    }
}

// 3. ECO DASHBOARD SCREEN SECTION (Calculators + Canvas rings + Live logging)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: EcoViewModel) {
    val emissionsState by viewModel.emissions.collectAsStateWithLifecycle()
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = profileState ?: UserProfileEntity()

    var logCategory by remember { mutableStateOf("transport") }
    var logAmountTxt by remember { mutableStateOf("") }
    var logTitle by remember { mutableStateOf("") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily limit progress meter (Apple Health concentric ring)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCard, RoundedCornerShape(20.dp))
                    .border(1.dp, StrokeHighlight, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "Carbon Footprint Limits",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    // Draw layered rings side by side with analytics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Dynamic draw concentric limit circle
                                drawArc(
                                    color = Color(0x1F2979FF),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx())
                                )
                                // Active arc
                                drawArc(
                                    color = ElectricEmerald,
                                    startAngle = -90f,
                                    sweepAngle = (profile.ecoScore / 100f) * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${profile.ecoScore}%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ElectricEmerald
                                )
                                Text("Score", fontSize = 10.sp, color = TextSecondary)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.size(12.dp).background(ElectricEmerald, CircleShape))
                                Text("Eco Score Target Limits", fontSize = 12.sp, color = TextPrimary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.size(12.dp).background(NeonCyan, CircleShape))
                                Text("Weekly reduction quota", fontSize = 12.sp, color = TextPrimary)
                            }
                            Text(
                                "Streak Factor: ${profile.currentStreak} day efforts keeps indices clean",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Weekly Emissions Chart (Custom Canvas Bars)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCard, RoundedCornerShape(20.dp))
                    .border(1.dp, StrokeHighlightCyan, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Weekly Sector Emissions (kg CO₂)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )

                    // Draw custom bars
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val categories = listOf("Transport", "Energy", "Food", "Offsets")
                        val values = listOf(
                            emissionsState.filter { it.category == "transport" }.sumOf { it.amountKg },
                            emissionsState.filter { it.category == "energy" }.sumOf { it.amountKg },
                            emissionsState.filter { it.category == "food" }.sumOf { it.amountKg },
                            profile.savedCo2Kg
                        )
                        val maxCo2Double = maxOf(values.maxOrNull() ?: 1.0, 1.0)
                        val maxCo2 = maxCo2Double.toFloat()

                        values.forEachIndexed { index, value ->
                            val percent = (value.toFloat() / maxCo2).coerceIn(0.1f, 1.0f)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = String.format("%.1f", value),
                                    fontSize = 10.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight(percent * 0.8f)
                                        .width(28.dp)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = when (index) {
                                                    0 -> listOf(HighCarbonRed, HighCarbonRed.copy(alpha = 0.5f))
                                                    1 -> listOf(NeonCyan, NeonCyan.copy(alpha = 0.5f))
                                                    2 -> listOf(AccentGold, AccentGold.copy(alpha = 0.5f))
                                                    else -> listOf(ElectricEmerald, ElectricEmerald.copy(alpha = 0.5f))
                                                }
                                            ),
                                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = categories[index],
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Real-time Footprint Logging Form
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftGlassOverlay, RoundedCornerShape(16.dp))
                    .border(1.dp, StrokeHighlight, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Log Dynamic Emission Event",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    // Category picker
                    Text("Select Sector:", fontSize = 11.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Transport" to "transport",
                            "Energy" to "energy",
                            "Food" to "food"
                        ).forEach { (label, catCode) ->
                            val isSel = logCategory == catCode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSel) ElectricEmerald.copy(alpha = 0.2f) else DarkCard,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSel) ElectricEmerald else StrokeHighlight,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { logCategory = catCode }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) ElectricEmerald else TextSecondary
                                )
                            }
                        }
                    }

                    // Log description input
                    OutlinedTextField(
                        value = logTitle,
                        onValueChange = { logTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("E.g., Commuted using electric hybrid car", fontSize = 12.sp) },
                        label = { Text("Event description", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricEmerald,
                            unfocusedBorderColor = StrokeHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg
                        ),
                        singleLine = true
                    )

                    // Carbon amount input
                    OutlinedTextField(
                        value = logAmountTxt,
                        onValueChange = { logAmountTxt = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("E.g., 4.2", fontSize = 12.sp) },
                        label = { Text("Metric weight (kg CO₂)", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricEmerald,
                            unfocusedBorderColor = StrokeHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    // Log Submit button
                    Button(
                        onClick = {
                            val amt = logAmountTxt.toDoubleOrNull()
                            if (amt != null && logTitle.isNotBlank()) {
                                viewModel.addEmission(logCategory, amt, logTitle)
                                Toast.makeText(context, "Log logged! XP +25 earned dynamically.", Toast.LENGTH_SHORT).show()
                                logTitle = ""
                                logAmountTxt = ""
                            } else {
                                Toast.makeText(context, "Enter valid log inputs to update indexes", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricEmerald)
                    ) {
                        Text("Add Activity Log entry", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Log Entries History
        item {
            Text(
                text = "Live Log History",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (emissionsState.isEmpty()) {
            item {
                Text(
                    "No emissions logged yet.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(emissionsState) { entry ->
                EmissionHistoryItem(entry = entry, onDelete = {
                    viewModel.deleteEmission(entry.id)
                })
            }
        }
    }
}

// 4. REWARDS SCREEN SECTION (Badges + XP points gamifications)
@Composable
fun RewardsScreen(viewModel: EcoViewModel) {
    val badges by viewModel.unlockedBadges.collectAsStateWithLifecycle()
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = profileState ?: UserProfileEntity()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gamified level banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(DarkCard, Color(0xFF1B2F2A))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(1.dp, StrokeHighlight, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Your Gamification Level",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                "Level ${profile.level} Eco Warrior",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = ElectricEmerald
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.Stars,
                            contentDescription = "Achievements star",
                            tint = AccentGold,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    // Level XP Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Progress to Level ${profile.level + 1}", fontSize = 11.sp, color = TextSecondary)
                        Text("${profile.xp % 250}/250 XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                    }

                    LinearProgressIndicator(
                        progress = { (profile.xp % 250) / 250f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = ElectricEmerald,
                        trackColor = DarkBg
                    )

                    Text(
                        "Achieve higher tiers by maintaining daily streaks and recording clean transit activities with lower weight sizes.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Leaderboard teaser architecture
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftGlassOverlay, RoundedCornerShape(16.dp))
                    .border(1.dp, StrokeHighlightCyan, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Group, "Leaderboard", tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Text("Active Regional Leaderboards", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    }
                    Text("Compete with sustainability champions around ${profile.city}:", fontSize = 11.sp, color = TextSecondary)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Top rankings mockup
                    listOf(
                        Triple("1. Aarav S.", "94 Score", "Lv. 8"),
                        Triple("2. Divya K.", "89 Score", "Lv. 6"),
                        Triple("3. ${profile.name} (You)", "${profile.ecoScore} Score", "Lv. ${profile.level}")
                    ).forEach { (rankName, score, lvl) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkBg.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(rankName, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(score, fontSize = 11.sp, color = ElectricEmerald)
                                Text(lvl, fontSize = 11.sp, color = AccentGold)
                            }
                        }
                    }
                }
            }
        }

        // 5 Premium Eco Score Badges System
        item {
            Text(
                "Premium Eco Score Badges System",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val xp = profile.xp
                // Five levels specified in gamified design guidelines
                EcoScoreBadgeCard(
                    levelName = "Starter",
                    isRequiredXp = "0",
                    isActive = xp in 0..99
                )
                EcoScoreBadgeCard(
                    levelName = "Conscious",
                    isRequiredXp = "100",
                    isActive = xp in 100..499
                )
                EcoScoreBadgeCard(
                    levelName = "Impactful",
                    isRequiredXp = "500",
                    isActive = xp in 500..999
                )
                EcoScoreBadgeCard(
                    levelName = "Eco Hero",
                    isRequiredXp = "1000",
                    isActive = xp in 1000..2499
                )
                EcoScoreBadgeCard(
                    levelName = "Planet Guardian",
                    isRequiredXp = "2500+",
                    isActive = xp >= 2500
                )
            }
        }
    }
}

// 5. WEEKLY REPORTS HUB SCREEN SECTION (Personalized forecasts + Share trigger)
@Composable
fun ReportsScreen(viewModel: EcoViewModel) {
    val reportText = viewModel.triggerWeeklyReport()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(colors = listOf(DarkCard, SoftGlassOverlay)),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(1.dp, StrokeHighlightCyan, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.Assessment, "Report Title", tint = ElectricEmerald)
                            Text("Adaptive Weekly Report", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        }
                        
                        // Action share trigger button
                        IconButton(
                            onClick = {
                                val shareIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, "My EcoMind AI Weekly Report Summary:\n\n$reportText\n\nDownload EcoMind AI Tracker!")
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share carbon metrics"))
                            },
                        ) {
                            Icon(Icons.Filled.Share, "Share Report", tint = NeonCyan)
                        }
                    }

                    Divider(color = StrokeHighlight, thickness = 1.dp)

                    Text(
                        text = reportText,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        "* Personalized insights generated organically by EcoMind reasoning models.",
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Carbon Savings forecasts details Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCard, RoundedCornerShape(16.dp))
                    .border(1.dp, StrokeHighlight, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("CO₂ Reduction Suggested Forecasts", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text(
                        "Achieving dynamic target savings over consecutive weeks protects local natural resources around your city region. Switch to modern smart appliances to cut 45kg CO₂ grids emission targets yearly.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// 6. PROFILE & SETTINGS SCREEN SECTION (Config name, language, custom hook url)
@Composable
fun ProfileScreen(viewModel: EcoViewModel) {
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = profileState ?: UserProfileEntity()

    val editName by viewModel.editName.collectAsStateWithLifecycle()
    val editCity by viewModel.editCity.collectAsStateWithLifecycle()
    val editLang by viewModel.editLanguage.collectAsStateWithLifecycle()
    val editVehicle by viewModel.editVehicleType.collectAsStateWithLifecycle()
    val editWebhook by viewModel.editWebhookUrl.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCard, RoundedCornerShape(16.dp))
                    .border(1.dp, StrokeHighlight, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(ElectricEmerald.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, ElectricEmerald, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, "Avatar Symbol", tint = ElectricEmerald, modifier = Modifier.size(36.dp))
                    }
                    Text(profile.name, fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextPrimary)
                    Text("Eco Level ${profile.level} Premium Tracker", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        // Firebase Realtime Database Sync Panel
        item {
            var firebaseSyncId by remember(profile.userId) { mutableStateOf(profile.userId) }
            val firebaseSyncStatus by viewModel.firebaseSyncStatus.collectAsStateWithLifecycle()
            val firebaseSyncMessage by viewModel.firebaseSyncMessage.collectAsStateWithLifecycle()
            val firebaseUrlState by viewModel.firebaseDbUrl.collectAsStateWithLifecycle()

            // Update local state when profile changes
            LaunchedEffect(profile.userId) {
                if (firebaseSyncId.isEmpty() || firebaseSyncId == "guest" || firebaseSyncId == "firebase_u1") {
                    firebaseSyncId = profile.userId
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftGlassOverlay, RoundedCornerShape(16.dp))
                    .border(1.dp, StrokeHighlightCyan, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(NeonCyan.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Refresh, "Sync Logo", tint = NeonCyan, modifier = Modifier.size(18.dp))
                        }
                        Text("Firebase RTB Real-Time Sync", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    }

                    Text(
                        "Synchronize your real-time carbon metrics, daily emission breakdowns, and profile scores directly from your custom Firebase project endpoints.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )

                    // Target User ID Field
                    OutlinedTextField(
                        value = firebaseSyncId,
                        onValueChange = { firebaseSyncId = it },
                        modifier = Modifier.fillMaxWidth().testTag("firebase_sync_id_input"),
                        label = { Text("Remote Firebase User ID", fontSize = 11.sp) },
                        placeholder = { Text("e.g. padc_002, padc_003") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = StrokeHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg
                        ),
                        singleLine = true
                    )

                    // Database URL display
                    Text(
                        text = "Real-time DB Endpoint:\n$firebaseUrlState",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        lineHeight = 12.sp
                    )

                    // Sync Status feedback
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBg, RoundedCornerShape(8.dp))
                            .border(1.dp, StrokeHighlight, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when(firebaseSyncStatus) {
                                        "SYNCING" -> Icons.Filled.Sync
                                        "SUCCESS" -> Icons.Filled.CheckCircle
                                        "FAILED" -> Icons.Filled.Warning
                                        else -> Icons.Filled.Cloud
                                    },
                                    contentDescription = "Sync Status Icon",
                                    tint = when(firebaseSyncStatus) {
                                        "SYNCING" -> NeonCyan
                                        "SUCCESS" -> ElectricEmerald
                                        "FAILED" -> HighCarbonRed
                                        else -> TextSecondary
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Sync Status:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = when(firebaseSyncStatus) {
                                        "SYNCING" -> "SYNCING..."
                                        "SUCCESS" -> "CONNECTED & LOADED"
                                        "FAILED" -> "SYNC ERROR"
                                        else -> "NOT SYNCHRONIZED"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = when(firebaseSyncStatus) {
                                        "SYNCING" -> AccentGold
                                        "SUCCESS" -> ElectricEmerald
                                        "FAILED" -> HighCarbonRed
                                        else -> TextSecondary
                                }
                            )
                        }

                        if (firebaseSyncMessage.isNotEmpty()) {
                            Text(
                                text = firebaseSyncMessage,
                                fontSize = 10.sp,
                                color = if (firebaseSyncStatus == "FAILED") HighCarbonRed else TextSecondary,
                                lineHeight = 13.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Button(
                            onClick = { viewModel.syncFromFirebase(firebaseSyncId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Filled.CloudDownload, "Download Check", modifier = Modifier.size(14.dp), tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pull & Sync Remote Database Node", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftGlassOverlay, RoundedCornerShape(16.dp))
                    .border(1.dp, StrokeHighlightCyan, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("User Custom Configurations", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)

                    // User Name Field
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { viewModel.updateProfileName(it) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                        label = { Text("Display Name", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricEmerald,
                            unfocusedBorderColor = StrokeHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg
                        ),
                        singleLine = true
                    )

                    // User City Field
                    OutlinedTextField(
                        value = editCity,
                        onValueChange = { viewModel.updateProfileCity(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Resident City", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricEmerald,
                            unfocusedBorderColor = StrokeHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg
                        ),
                        singleLine = true
                    )

                    // Language Selector
                    Text("Language Preference:", fontSize = 11.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("English", "Hindi", "Regional").forEach { lang ->
                            val isSel = editLang == lang
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSel) ElectricEmerald.copy(alpha = 0.2f) else DarkBg,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSel) ElectricEmerald else StrokeHighlight,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.updateProfileLanguage(lang) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(lang, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) ElectricEmerald else TextSecondary)
                            }
                        }
                    }

                    // Vehicle Type Selector
                    OutlinedTextField(
                        value = editVehicle,
                        onValueChange = { viewModel.updateProfileVehicle(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Vehicle Type", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricEmerald,
                            unfocusedBorderColor = StrokeHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg
                        ),
                        singleLine = true
                    )

                    // Webhook URL endpoint for n8n Webhooks API integration
                    OutlinedTextField(
                        value = editWebhook,
                        onValueChange = { viewModel.updateProfileWebhook(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("N8N Webhook Endpoint (POST /v7)", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricEmerald,
                            unfocusedBorderColor = StrokeHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg
                        ),
                        singleLine = true
                    )

                    // Diagnostic Connection feedback for N8N workflow
                    val diagStatus by viewModel.n8nDiagnosticStatus.collectAsStateWithLifecycle()
                    val diagMessage by viewModel.n8nDiagnosticMessage.collectAsStateWithLifecycle()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBg, RoundedCornerShape(8.dp))
                            .border(1.dp, StrokeHighlight, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when(diagStatus) {
                                        "TESTING" -> Icons.Filled.Sync
                                        "CONNECTED_SUCCESS" -> Icons.Filled.CheckCircle
                                        "CONNECTION_FAILED" -> Icons.Filled.Warning
                                        else -> Icons.Filled.Notifications
                                    },
                                    contentDescription = "Diagnostic Status",
                                    tint = when(diagStatus) {
                                        "TESTING" -> ElectricEmerald
                                        "CONNECTED_SUCCESS" -> ElectricEmerald
                                        "CONNECTION_FAILED" -> HighCarbonRed
                                        else -> TextSecondary
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "N8N Connection Status:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = when(diagStatus) {
                                    "TESTING" -> "CHECKING CONNECT..."
                                    "CONNECTED_SUCCESS" -> "CONNECTED"
                                    "CONNECTION_FAILED" -> "CONNECTION ERROR"
                                    else -> "NOT TESTED"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = when(diagStatus) {
                                    "TESTING" -> AccentGold
                                    "CONNECTED_SUCCESS" -> ElectricEmerald
                                    "CONNECTION_FAILED" -> HighCarbonRed
                                    else -> TextSecondary
                                }
                            )
                        }

                        if (diagMessage.isNotEmpty()) {
                            Text(
                                text = diagMessage,
                                fontSize = 10.sp,
                                color = if (diagStatus == "CONNECTION_FAILED") HighCarbonRed else TextSecondary,
                                lineHeight = 13.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        OutlinedButton(
                            onClick = { viewModel.testN8nConnection(editWebhook) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricEmerald),
                            border = BorderStroke(1.dp, ElectricEmerald.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Test Webhook Connection Flow", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            viewModel.updateProfileSettings(
                                name = editName,
                                city = editCity,
                                language = editLang,
                                vehicle = editVehicle,
                                goal = profile.goal,
                                webhookUrl = editWebhook
                            )
                            Toast.makeText(context, "All configurations are live and auto-saved in real-time!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricEmerald)
                    ) {
                        Text("Save Profile Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.logout()
                            Toast.makeText(context, "Logged out from EcoMind session.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HighCarbonRed),
                        border = BorderStroke(1.dp, HighCarbonRed)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.ExitToApp, "Sign Out", tint = HighCarbonRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign Out from Session", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Subcomponent: ChatBubbleItem (ChatGPT chat design style)
@Composable
fun ChatBubbleItem(message: ChatMessage) {
    val isUser = message.senderRole == "user"
    val bubbleColor = if (isUser) ElectricEmerald.copy(alpha = 0.15f) else DarkCard
    val borderLight = if (isUser) StrokeHighlight else StrokeHighlightCyan

    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .background(bubbleColor, RoundedCornerShape(16.dp))
                .border(1.dp, borderLight, RoundedCornerShape(16.dp))
                .padding(14.dp)
                .widthIn(max = 280.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Role badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "You" else "EcoMind Coach",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) ElectricEmerald else NeonCyan
                    )
                }
                Text(
                    text = parseChatMarkdown(message.messageText),
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )
                
                // Formatted custom timestamp
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                Text(
                    text = sdf.format(Date(message.timestamp)),
                    fontSize = 8.sp,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

// Subcomponent: Typing Indicator . . .
@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dotScales = (0..2).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(offsetMillis = index * 200)
            ),
            label = "dot_$index"
        )
    }

    Row(
        modifier = Modifier
            .padding(8.dp)
            .background(DarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, StrokeHighlightCyan, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dotScales.forEach { scaleState ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(ElectricEmerald.copy(alpha = scaleState.value), CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text("AI Coach is typing...", fontSize = 11.sp, color = TextSecondary)
    }
}

// Subcomponent: Recommendation item Checklist
@Composable
fun RecommendationItem(
    rec: RecommendationDto,
    onMarkDone: () -> Unit
) {
    var isDone by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isDone) ElectricEmerald.copy(alpha = 0.5f) else StrokeHighlight,
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checkmark button log active
            IconButton(
                onClick = {
                    if (!isDone) {
                        isDone = true
                        onMarkDone()
                    }
                },
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isDone) ElectricEmerald.copy(alpha = 0.2f) else SoftGlassOverlay,
                        CircleShape
                    )
                    .border(
                        1.dp,
                        if (isDone) ElectricEmerald else TextSecondary,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = "Confirm",
                    tint = if (isDone) ElectricEmerald else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rec.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) TextSecondary else TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Est. savings: ${rec.co2SavingsKg} kg", fontSize = 9.sp, color = NeonCyan)
                    }

                    Box(
                        modifier = Modifier
                            .background(AccentGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(rec.difficulty, fontSize = 9.sp, color = AccentGold, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .background(HighCarbonRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(rec.priority, fontSize = 9.sp, color = HighCarbonRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Subcomponent: Log emission history list card
@Composable
fun EmissionHistoryItem(
    entry: EmissionEntry,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, StrokeHighlight, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category mini icon decoration
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = when (entry.category.lowercase()) {
                            "transport" -> HighCarbonRed.copy(alpha = 0.15f)
                            "energy" -> NeonCyan.copy(alpha = 0.15f)
                            else -> ElectricEmerald.copy(alpha = 0.15f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (entry.category.lowercase()) {
                        "transport" -> Icons.Filled.DirectionsCar
                        "energy" -> Icons.Filled.FlashOn
                        else -> Icons.Filled.Fastfood
                    },
                    contentDescription = entry.category,
                    tint = when (entry.category.lowercase()) {
                        "transport" -> HighCarbonRed
                        "energy" -> NeonCyan
                        else -> ElectricEmerald
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                Text(
                    text = "${entry.category.replaceFirstChar { it.uppercase() }} footprint • Recorded ${SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(entry.timestamp))}",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("+${entry.amountKg} kg CO₂", fontWeight = FontWeight.Black, color = HighCarbonRed, fontSize = 13.sp)
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Delete, "Delete event", tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// 10. Floating Assistant Orb Header Composable
@Composable
fun FloatingOrbHeader(
    orbState: String,
    tooltipText: String,
    onOrbClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val radiusPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radius"
    )

    // Ring expansion animations for Listening state
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1"
    )
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing, delayMillis = 500),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2"
    )

    // Thinking: Floating particles angle rotation
    val particleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    // Responding Wave PulseOffset
    val wavePulseState by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = Math.PI.toFloat() * 2,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    // Orb visual colors depending on active state
    val orbColors = when (orbState) {
        "listening" -> listOf(Color(0xFF0A84FF), Color(0xFF38BDF8), Color(0xFF0A84FF))
        "thinking" -> listOf(Color(0xFFFFB300), Color(0xFFFF9100), Color(0xFFFFB300))
        "speaking", "responding" -> listOf(Color(0xFFD500F9), Color(0xFF38BDF8), Color(0xFFD500F9))
        "success" -> listOf(Color(0xFF00D084), Color(0xFF10B981), Color(0xFF00D084))
        "warning" -> listOf(Color(0xFFEF4444), Color(0xFFFF9100), Color(0xFFEF4444))
        else -> listOf(Color(0xFF00D084), Color(0xFF38BDF8), Color(0xFF00D084))
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .clickable(onClick = onOrbClick)
            .drawBehind {
                val r = size.width / 2

                // 1. Draw Expanding sound rings if listening
                if (orbState == "listening") {
                    drawCircle(
                        color = Color(0xFF0A84FF).copy(alpha = (1f - (ring1Scale - 1f) / 1.2f).coerceIn(0f, 0.4f)),
                        radius = r * ring1Scale,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = (1f - (ring2Scale - 1f) / 1.2f).coerceIn(0f, 0.4f)),
                        radius = r * ring2Scale,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                // 2. Draw central breathing volumetric gradient sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = orbColors.map { it.copy(alpha = 0.35f * radiusPulse) },
                        radius = size.width * 0.7f
                    ),
                    radius = r * radiusPulse
                )
                drawCircle(
                    brush = Brush.linearGradient(orbColors),
                    radius = r * 0.65f
                )

                // 3. Draw thinking particles circling the orb
                if (orbState == "thinking") {
                    val pRadius = r * 1.15f
                    val radians = Math.toRadians(particleAngle.toDouble())
                    val pX = r + pRadius * kotlin.math.cos(radians).toFloat()
                    val pY = r + pRadius * kotlin.math.sin(radians).toFloat()

                    drawCircle(
                        color = Color(0xFFFF9100),
                        radius = 3.dp.toPx(),
                        center = Offset(pX, pY)
                    )

                    val radians2 = Math.toRadians((particleAngle + 180f).toDouble())
                    val pX2 = r + pRadius * kotlin.math.cos(radians2).toFloat()
                    val pY2 = r + pRadius * kotlin.math.sin(radians2).toFloat()

                    drawCircle(
                        color = Color(0xFF38BDF8),
                        radius = 2.5.dp.toPx(),
                        center = Offset(pX2, pY2)
                    )
                }

                // 4. Draw responding wave pulses across the sphere center
                if (orbState == "speaking" || orbState == "responding") {
                    val wavePath = Path()
                    val segments = 24
                    wavePath.moveTo(0f, r)
                    for (i in 0..segments) {
                        val x = (size.width / segments) * i
                        val sinIn = (i.toFloat() / segments) * Math.PI.toFloat() * 2 + wavePulseState
                        val y = r + kotlin.math.sin(sinIn).toFloat() * 6.dp.toPx()
                        wavePath.lineTo(x, y)
                    }
                    drawPath(
                        path = wavePath,
                        color = Color.White.copy(alpha = 0.8f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (orbState) {
                "listening" -> Icons.Filled.GraphicEq
                "thinking" -> Icons.Filled.HourglassEmpty
                "speaking", "responding" -> Icons.Filled.RecordVoiceOver
                "success" -> Icons.Filled.CheckCircle
                "warning" -> Icons.Filled.Warning
                else -> Icons.Filled.BlurOn
            },
            contentDescription = "Orb Assistant Active State",
            tint = Color.Black,
            modifier = Modifier.size(13.dp)
        )
    }
}

// 2. Beautiful soundwave speech recording active animation component
@Composable
fun SoundWaveGraphic(orbState: String) {
    val barCount = 12
    val infiniteTransition = rememberInfiniteTransition(label = "sounding")

    val scaleAnims = (0 until barCount).map { i ->
        infiniteTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = if (orbState == "listening") 350 + (i * 40) else 450 + (i * 50),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "soundheight_$i"
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(DarkCard.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .border(1.dp, StrokeHighlight, RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        scaleAnims.forEachIndexed { idx, animValue ->
            val barColor = if (orbState == "listening") NeonCyan else Color(0xFFD500F9)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(animValue.value * 0.8f)
                    .background(barColor, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun WebCallOverlay(viewModel: EcoViewModel) {
    val callState by viewModel.webCallState.collectAsStateWithLifecycle()
    val duration by viewModel.webCallDuration.collectAsStateWithLifecycle()
    val userText by viewModel.webCallUserTranscript.collectAsStateWithLifecycle()
    val agentText by viewModel.webCallAgentTranscript.collectAsStateWithLifecycle()
    val isMuted by viewModel.isWebCallMuted.collectAsStateWithLifecycle()
    val orbState by viewModel.orbState.collectAsStateWithLifecycle()

    val mins = duration / 60
    val secs = duration % 60
    val timeString = String.format("%02d:%02d", mins, secs)

    // Animated glow scale for connecting or active status
    val infiniteTransition = rememberInfiniteTransition(label = "callGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg.copy(alpha = 0.97f))
            .clickable(enabled = false) {}, // consume taps to prevent underneath interaction
        contentAlignment = Alignment.Center
    ) {
        // Gradient backdrop pattern glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val brush = Brush.radialGradient(
                        colors = listOf(
                            when (callState) {
                                "connecting", "ringing" -> NeonCyan.copy(alpha = 0.15f)
                                "active" -> ElectricEmerald.copy(alpha = 0.15f)
                                "sandbox", "sandbox_thinking" -> ElectricEmerald.copy(alpha = 0.15f)
                                else -> HighCarbonRed.copy(alpha = 0.15f)
                            },
                            Color.Transparent
                        ),
                        radius = size.width * 0.9f
                    )
                    drawRect(brush)
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Elegant top branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Language,
                        contentDescription = "WebRTC Link",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "SECURE WEB PORTAL VOICE STREAMING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "EcoMind Live AI Coach",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = when (callState) {
                        "connecting" -> "Establishing WebSocket secure tunnel..."
                        "ringing" -> "Ringing EcoMind agent..."
                        "active" -> "Active Web Call • $timeString"
                        "sandbox" -> "Offline Sandbox Mode • $timeString"
                        "sandbox_thinking" -> "Tuning sustainability heuristics... • $timeString"
                        "error" -> "Vocal Link Interrupted"
                        else -> "Call Ending..."
                    },
                    fontSize = 13.sp,
                    color = if (callState == "active" || callState == "sandbox" || callState == "sandbox_thinking") ElectricEmerald else if (callState == "error") HighCarbonRed else TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Central Pulsing Avatar Ring
            Box(
                modifier = Modifier
                    .size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulse waves
                val outerColors = when (callState) {
                    "active", "sandbox", "sandbox_thinking" -> if (orbState == "speaking") {
                        listOf(Color(0xFFD500F9), Color(0xFFF50057))
                    } else if (orbState == "listening") {
                        listOf(NeonCyan, Color(0xFF2979FF))
                    } else {
                        listOf(ElectricEmerald, NeonCyan)
                    }
                    else -> listOf(NeonCyan, StrokeHighlightCyan)
                }

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = outerColors.map { it.copy(alpha = 0.25f) },
                                    radius = size.width
                                ),
                                radius = (size.width / 2) * pulseScale
                            )
                        }
                )

                // Main circular capsule container containing the beautiful visual representation
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = when (callState) {
                                    "active", "sandbox", "sandbox_thinking" -> if (orbState == "speaking") {
                                        listOf(Color(0xFFD500F9), Color(0xFFF50057))
                                    } else if (orbState == "listening") {
                                        listOf(NeonCyan, Color(0xFF2979FF))
                                    } else {
                                        listOf(ElectricEmerald, NeonCyan)
                                    }
                                    else -> listOf(DarkCard, SoftGlassOverlay)
                                }
                            ),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = if (callState == "active" || callState == "sandbox" || callState == "sandbox_thinking") ElectricEmerald else strokeHighlightForCard(callState),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (callState) {
                            "active", "sandbox", "sandbox_thinking" -> if (orbState == "speaking") Icons.Filled.RecordVoiceOver else Icons.Filled.Face
                            else -> Icons.Filled.BlurOn
                        },
                        contentDescription = "Voice Coach Agent Representative",
                        tint = if (callState == "active" || callState == "sandbox" || callState == "sandbox_thinking") Color.Black else ElectricEmerald,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            // Real-time voice captions scrolling box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (callState == "error" || callState == "sandbox") 165.dp else 130.dp)
                    .border(1.dp, if (callState == "error") HighCarbonRed else StrokeHighlight, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (callState == "error") "Connection Issue Log" else "Real-time Captions Transcript",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (callState == "error") HighCarbonRed else NeonCyan
                    )

                    if (callState == "error") {
                        Text(
                            text = "Error detail: ${agentText.replace("Voicelink Carrier Disturbed:", "")}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.startSandboxCall() },
                                modifier = Modifier.weight(1.3f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricEmerald),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text("Activate Local AI Sandbox", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.startWebCall() },
                                modifier = Modifier.weight(0.8f).height(38.dp),
                                border = BorderStroke(1.dp, TextSecondary),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text("Retry Port", fontSize = 11.sp, color = TextPrimary)
                            }
                        }
                    } else {
                        // Agent text representation
                        Text(
                            text = "Coach: $agentText",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                        
                        // User Text transcription
                        if ((callState == "active" || callState == "sandbox") && userText.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(if (isMuted) HighCarbonRed else ElectricEmerald, CircleShape))
                                Text(
                                    text = "You: $userText",
                                    fontSize = 12.sp,
                                    color = if (isMuted) TextSecondary else ElectricEmerald,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Central query chips for simulated local sandbox
            if (callState == "sandbox") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "TAP QUERY TO TALK WITH LOCAL COACH:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val queries = listOf(
                            "What is my EV scooter carbon tier impact?",
                            "Give me 3 zero-emission lifestyle tips",
                            "Explain vehicle manufacturing carbon metrics"
                        )
                        queries.forEach { query ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(SoftGlassOverlay, RoundedCornerShape(12.dp))
                                    .border(1.dp, StrokeHighlightCyan, RoundedCornerShape(12.dp))
                                    .clickable { viewModel.sendSandboxQuery(query) }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = query,
                                    fontSize = 10.sp,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Controller Actions Grid at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute controls
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { viewModel.toggleWebCallMute() },
                        modifier = Modifier
                            .size(54.dp)
                            .background(if (isMuted) HighCarbonRed.copy(alpha = 0.2f) else SoftGlassOverlay, CircleShape)
                            .border(1.dp, if (isMuted) HighCarbonRed else TextSecondary, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                            contentDescription = "Mute Microphone",
                            tint = if (isMuted) HighCarbonRed else TextPrimary
                        )
                    }
                    Text(text = if (isMuted) "Unmute" else "Mute", fontSize = 11.sp, color = TextSecondary)
                }

                // Large end call buttons red
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { viewModel.endWebCall() },
                        modifier = Modifier
                            .size(68.dp)
                            .background(HighCarbonRed, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CallEnd,
                            contentDescription = "End secure Web call",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(text = "End", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                }

                // Call metrics information Web Link Info trigger
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    var showInfoDialog by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier
                            .size(54.dp)
                            .background(SoftGlassOverlay, CircleShape)
                            .border(1.dp, TextSecondary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Link,
                            contentDescription = "Show Connection Info",
                            tint = TextPrimary
                        )
                    }
                    Text(text = "Link Info", fontSize = 11.sp, color = TextSecondary)

                    if (showInfoDialog) {
                        AlertDialog(
                            onDismissRequest = { showInfoDialog = false },
                            title = { Text("Secure Web Calling API Link Info") },
                            text = {
                                Text("This call stream is established directly with ElevenLabs' Conversational AI edge audio gateway over high-fidelity WebSockets for dynamic real-time voice streaming synthesis.")
                            },
                            confirmButton = {
                                Button(
                                    onClick = { showInfoDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricEmerald)
                                ) {
                                    Text("Dismiss", color = Color.Black)
                                }
                            },
                            containerColor = DarkCard,
                            titleContentColor = TextPrimary,
                            textContentColor = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

private fun strokeHighlightForCard(callState: String): Color {
    return if (callState == "error") HighCarbonRed else StrokeHighlight
}

fun parseChatMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    // Preprocess lines to make bullet points look incredibly structured and neat
    val processedLines = text.lines().map { line ->
        val trimmed = line.trimStart()
        if (trimmed.startsWith("* ") || trimmed.startsWith("- ")) {
            val bulletContent = trimmed.substring(2)
            // Indent bullet lines beautifully inside our custom layout matching screenshots
            "   •  $bulletContent"
        } else {
            line
        }
    }
    val processedText = processedLines.joinToString("\n")

    return androidx.compose.ui.text.buildAnnotatedString {
        var cursor = 0
        val regex = Regex("(\\*\\*.*?\\*\\*|`.*?`)", RegexOption.DOT_MATCHES_ALL)
        val matches = regex.findAll(processedText)
        
        for (match in matches) {
            val matchRange = match.range
            if (matchRange.first > cursor) {
                append(processedText.substring(cursor, matchRange.first))
            }
            
            val matchText = match.value
            if (matchText.startsWith("**") && matchText.endsWith("**")) {
                val cleanText = matchText.substring(2, matchText.length - 2)
                withStyle(
                    style = androidx.compose.ui.text.SpanStyle(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.White
                    )
                ) {
                    append(cleanText)
                }
            } else if (matchText.startsWith("`") && matchText.endsWith("`")) {
                val cleanText = matchText.substring(1, matchText.length - 1)
                withStyle(
                    style = androidx.compose.ui.text.SpanStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = NeonCyan,
                        background = Color.Black.copy(alpha = 0.3f)
                    )
                ) {
                    append(cleanText)
                }
            }
            cursor = matchRange.last + 1
        }
        
        if (cursor < processedText.length) {
            append(processedText.substring(cursor))
        }
    }
}

// 12. AUTONOMOUS AGENT ACTIVE TELEMETRY FEED & CONSOLE WRAPPER
@Composable
fun PulsingLed(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "LedPulse")
    val ledAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Box(
        modifier = modifier
            .size(8.dp)
            .graphicsLayer { alpha = ledAlpha }
            .background(
                color = ElectricEmerald,
                shape = CircleShape
            )
            .border(1.dp, ElectricEmerald, CircleShape)
    )
}

@Composable
fun AgentActivityConsoleCard(viewModel: com.example.ui.viewmodel.EcoViewModel) {
    val activities by viewModel.agentActivities.collectAsStateWithLifecycle()
    var isExpanded by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf("ALL") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StrokeHighlightCyan.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .testTag("agent_activity_console_card"),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Agent Brain",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Autonomous AI Agent Log Stream",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }
                
                // Pulsing Green LED
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PulsingLed()
                    Text(
                        text = "ACTIVE LIVE CONTEXT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tracking real-time background active operations (sync handlers, telemetry polling, Gemini heuristic tuning):",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // OLED Monospace Console Output Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B13), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF1B2E49), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (activities.isEmpty()) {
                        Text(
                            text = "[SYSTEM] Initializing autonomous loop streams...",
                            color = NeonCyan,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    } else {
                        // Show up to top 3 actions in vertical sequence
                        activities.take(3).forEach { log ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "[${log.timestamp}]",
                                    color = TextSecondary,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "${log.type}:",
                                    color = when (log.type) {
                                        "SYNC" -> NeonCyan
                                        "AI_PLAN" -> AccentGold
                                        "CALCULATION" -> ElectricEmerald
                                        "TELEMETRY" -> Color(0xFFD500F9)
                                        "ACTION" -> Color(0xFF2979FF)
                                        else -> TextPrimary
                                    },
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = log.message,
                                    color = if (log.isRealTime) Color.White else TextPrimary,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    modifier = Modifier.weight(1f),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Console buttons/actions on footing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Telemetry handshakes: OK",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                
                TextButton(
                    onClick = { isExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)
                ) {
                    Icon(
                        imageVector = Icons.Filled.BlurOn,
                        contentDescription = "Expand logs",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Open Telemetry Terminal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Expanded full details dialog console with search filtering
    if (isExpanded) {
        AlertDialog(
            onDismissRequest = { isExpanded = false },
            confirmButton = {
                TextButton(onClick = { isExpanded = false }) {
                    Text("Close Terminal", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Diagnostic admin manual ping trigger
                        viewModel.addAgentActivity(
                            "SYNC",
                            "Manual Webhook Test: admin diagnostic ping packet dispatched.",
                            "Sync",
                            isRealTime = true
                        )
                    }
                ) {
                    Text("Ping Webhook Link", color = ElectricEmerald)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.AutoAwesome, "Brain Console", tint = NeonCyan)
                    Text("Telemetry Log Console", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Quick tag filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("ALL", "SYNC", "AI_PLAN", "CALCULATION", "TELEMETRY").forEach { category ->
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .background(
                                        if (filterType == category) NeonCyan.copy(alpha = 0.2f) else SoftGlassOverlay,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (filterType == category) NeonCyan else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { filterType = category }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 9.sp,
                                    color = if (filterType == category) NeonCyan else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Scrolling panel
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(Color(0xFF050911), RoundedCornerShape(10.dp))
                            .border(1.5.dp, Color(0xFF1B2E49), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filtered = if (filterType == "ALL") {
                            activities
                        } else {
                            activities.filter { it.type == filterType }
                        }

                        if (filtered.isEmpty()) {
                            item {
                                Text(
                                    text = "> No traces recorded under category $filterType",
                                    color = TextSecondary,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            }
                        } else {
                            items(filtered) { log ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "[${log.timestamp}]",
                                        color = TextSecondary,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "${log.type}:",
                                        color = when (log.type) {
                                            "SYNC" -> NeonCyan
                                            "AI_PLAN" -> AccentGold
                                            "CALCULATION" -> ElectricEmerald
                                            "TELEMETRY" -> Color(0xFFD500F9)
                                            "ACTION" -> Color(0xFF2979FF)
                                            else -> TextPrimary
                                        },
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = log.message,
                                        color = if (log.isRealTime) Color.White else TextPrimary,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        modifier = Modifier.weight(1f),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(20.dp),
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = true)
        )
    }
}

