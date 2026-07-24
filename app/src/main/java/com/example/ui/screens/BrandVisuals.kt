package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.ElectricCar
import androidx.compose.material.icons.rounded.EnergySavingsLeaf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

// 1. Logo System: AI Brain + Leaf Fusion (Left side neural nodes, Right side organic ecosystem leaf)
@Composable
fun BrainLeafFusionLogo(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "brain_leaf_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.graphicsLayer {
        scaleX = pulse
        scaleY = pulse
    }) {
        val width = size.width
        val height = size.height

        // Background soft cyan and emerald dynamic radial glows
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonCyan.copy(alpha = 0.2f), Color.Transparent),
                center = Offset(width * 0.35f, height * 0.5f),
                radius = width * 0.45f
            ),
            radius = width * 0.45f,
            center = Offset(width * 0.35f, height * 0.5f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ElectricEmerald.copy(alpha = 0.2f), Color.Transparent),
                center = Offset(width * 0.65f, height * 0.5f),
                radius = width * 0.45f
            ),
            radius = width * 0.45f,
            center = Offset(width * 0.65f, height * 0.5f)
        )

        // Draw Left Side: Neural Brain Nodes (DeepMind and OpenAI inspired cybernetics)
        val leftNodes = listOf(
            Offset(width * 0.38f, height * 0.25f),
            Offset(width * 0.22f, height * 0.35f),
            Offset(width * 0.15f, height * 0.50f),
            Offset(width * 0.24f, height * 0.65f),
            Offset(width * 0.38f, height * 0.75f),
            Offset(width * 0.34f, height * 0.48f),
            Offset(width * 0.42f, height * 0.60f)
        )

        leftNodes.forEachIndexed { i, node ->
            leftNodes.forEachIndexed { j, otherNode ->
                if (i != j) {
                    val dx = node.x - otherNode.x
                    val dy = node.y - otherNode.y
                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                    if (dist < width * 0.35f) {
                        drawLine(
                            color = NeonCyan.copy(alpha = 0.4f),
                            start = node,
                            end = otherNode,
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }
            }
        }

        leftNodes.forEach { node ->
            drawCircle(
                color = AccentBlue.copy(alpha = 0.3f),
                radius = 7.dp.toPx(),
                center = node
            )
            drawCircle(
                color = NeonCyan,
                radius = 3.dp.toPx(),
                center = node
            )
        }

        // Draw Right Side: Organic Eco Leaf
        val leafPath = Path().apply {
            moveTo(width * 0.5f, height * 0.20f)
            // Outer curved leaf edge
            cubicTo(
                width * 0.85f, height * 0.28f,
                width * 0.90f, height * 0.65f,
                width * 0.5f, height * 0.82f
            )
            // Bottom to middle return curve
            cubicTo(
                width * 0.44f, height * 0.70f,
                width * 0.48f, height * 0.45f,
                width * 0.5f, height * 0.20f
            )
        }

        drawPath(
            path = leafPath,
            brush = Brush.linearGradient(
                colors = listOf(ElectricEmerald, Color(0xFF00B066)),
                start = Offset(width * 0.5f, height * 0.2f),
                end = Offset(width * 0.85f, height * 0.8f)
            )
        )

        // Leaf internal vein details (tech aesthetics)
        val veins = listOf(
            Pair(Offset(width * 0.5f, height * 0.35f), Offset(width * 0.68f, height * 0.32f)),
            Pair(Offset(width * 0.5f, height * 0.48f), Offset(width * 0.76f, height * 0.43f)),
            Pair(Offset(width * 0.5f, height * 0.60f), Offset(width * 0.74f, height * 0.56f)),
            Pair(Offset(width * 0.5f, height * 0.70f), Offset(width * 0.62f, height * 0.68f))
        )

        veins.forEach { vein ->
            drawLine(
                color = DarkBg.copy(alpha = 0.45f),
                start = vein.first,
                end = vein.second,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Central divider tech spine
        drawLine(
            color = TextPrimary.copy(alpha = 0.6f),
            start = Offset(width * 0.5f, height * 0.18f),
            end = Offset(width * 0.5f, height * 0.84f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

// 2. Eco Orbit Symbol (Circular orbits supporting continuous optimization)
@Composable
fun EcoOrbitSymbol(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbit_spinning")
    val angleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .graphicsLayer { rotationZ = angleRotation }
            .drawBehind {
                val r = size.width / 2

                // Orbit circles
                drawCircle(
                    color = StrokeHighlightCyan.copy(alpha = 0.3f),
                    radius = r * 0.95f,
                    style = Stroke(width = 1.dp.toPx())
                )

                drawCircle(
                    color = StrokeHighlight.copy(alpha = 0.4f),
                    radius = r * 0.75f,
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )

                // Small neural nodes orbiting
                drawCircle(
                    color = NeonCyan,
                    radius = 4.dp.toPx(),
                    center = Offset(r + r * 0.95f * kotlin.math.cos(0.0).toFloat(), r + r * 0.95f * kotlin.math.sin(0.0).toFloat())
                )

                drawCircle(
                    color = ElectricEmerald,
                    radius = 3.dp.toPx(),
                    center = Offset(r + r * 0.75f * kotlin.math.cos(Math.PI).toFloat(), r + r * 0.75f * kotlin.math.sin(Math.PI).toFloat())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.EnergySavingsLeaf,
            contentDescription = "Core planet optimization",
            tint = ElectricEmerald,
            modifier = Modifier.size(24.dp)
        )
    }
}

// 3. Realistic Onboarding Scene definition
data class OnboardContent(
    val id: Int,
    val title: String,
    val tagline: String,
    val description: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val overlayColor: Color
)

// 4. Custom Onboarding Illustration Carousel with Semi-3D Glass UI elements
@Composable
fun OnboardingIllustrationCarousel() {
    val slides = remember {
        listOf(
            OnboardContent(
                id = 1,
                title = "AI Sustainability Coach",
                tagline = "Empowered by Intelligent Calm Technology",
                description = "Your floating AI Companion schedules micro-habits, monitors real-time changes & coaches you on energy balance through conversational wisdom.",
                icon = Icons.Filled.AutoAwesome,
                primaryColor = ElectricEmerald,
                overlayColor = NeonCyan
            ),
            OnboardContent(
                id = 2,
                title = "Deep Carbon Analytics",
                tagline = "Predictive Neural Network Footprints",
                description = "Track your food choices, transit, & utility emissions. Our machine learning models detect leakages and model smart mitigation pipelines.",
                icon = Icons.Filled.Language,
                primaryColor = NeonCyan,
                overlayColor = AccentBlue
            ),
            OnboardContent(
                id = 3,
                title = "Smart AI Recommendations",
                tagline = "Automated webhook optimizations with n8n",
                description = "Receive adaptive alerts triggered by environmental events and automatically schedule carbon offsets on your linked workspace hubs.",
                icon = Icons.Filled.OfflineBolt,
                primaryColor = AccentBlue,
                overlayColor = ElectricEmerald
            ),
            OnboardContent(
                id = 4,
                title = "Natural Voice Interaction",
                tagline = "Speak naturally with your Ambient Companion",
                description = "Engage in voice-to-voice eco check-ins using our animated responsive AI Orb system. Idle, thinking, & success states respond dynamically.",
                icon = Icons.Filled.KeyboardVoice,
                primaryColor = ElectricEmerald,
                overlayColor = AccentGold
            ),
            OnboardContent(
                id = 5,
                title = "Eco Badges & Habits",
                tagline = "Gamified environment for Planet Heroes",
                description = "Build streaks, capture achievements, level-up across 5 metallic badges, and unlock customizable reward certificates of sustainability.",
                icon = Icons.Filled.EmojiEvents,
                primaryColor = AccentGold,
                overlayColor = ElectricEmerald
            )
        )
    }

    var currentIdx by remember { mutableStateOf(0) }

    // Auto-swipe timer every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentIdx = (currentIdx + 1) % slides.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(
            targetState = slides[currentIdx],
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) + slideInHorizontally(animationSpec = tween(500)) { width -> width / 2 } togetherWith
                        fadeOut(animationSpec = tween(500)) + slideOutHorizontally(animationSpec = tween(500)) { width -> -width / 2 }
            },
            label = "onboard_slide"
        ) { slide ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(1.dp, slide.primaryColor.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SoftGlassOverlay)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Floating Semi-3D Cinematic Illustration Holder (Drawn dynamically via Canvas)
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(DarkBg.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                            .border(1.dp, slide.overlayColor.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                            .drawBehind {
                                // Dynamic soft light cone simulation
                                val gradient = Brush.radialGradient(
                                    colors = listOf(slide.primaryColor.copy(alpha = 0.25f), Color.Transparent),
                                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                                    radius = size.width * 0.6f
                                )
                                drawCircle(
                                    brush = gradient,
                                    radius = size.width * 0.6f,
                                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Animated breathing elements in the illustration
                        val infiniteTransition = rememberInfiniteTransition(label = "illustration")
                        val breatheAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 0.95f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1400, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "breathe"
                        )

                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .graphicsLayer { alpha = breatheAlpha },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = slide.icon,
                                contentDescription = null,
                                tint = slide.primaryColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Text Content with premium typography hierarchy
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = slide.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = slide.tagline,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = slide.primaryColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = slide.description,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 14.sp,
                            maxLines = 4
                        )
                    }
                }
            }
        }

        // Customized active indicators
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            slides.forEachIndexed { idx, _ ->
                val isActive = currentIdx == idx
                val barWidth by animateDpAsState(
                    targetValue = if (isActive) 20.dp else 6.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "width"
                )
                val dotColor by animateColorAsState(
                    targetValue = if (isActive) ElectricEmerald else TextSecondary.copy(alpha = 0.3f),
                    label = "color"
                )

                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(barWidth)
                        .background(dotColor, CircleShape)
                        .clickable { currentIdx = idx }
                )
            }
        }
    }
}

// 5. Eco Score Badges Composable with Metallic Eco tech appearance
@Composable
fun EcoScoreBadgeCard(
    levelName: String, // Starter, Conscious, Impactful, Eco Hero, Planet Guardian
    isRequiredXp: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val metallicColors = when (levelName) {
        "Starter" -> listOf(Color(0xFF8E9EAB), Color(0xFFEEF2F3), Color(0xFF8E9EAB)) // Silver steel
        "Conscious" -> listOf(Color(0xFFFF8C00), Color(0xFFFFD700), Color(0xFFFF8C00)) // Bronze glow
        "Impactful" -> listOf(Color(0xFF38BDF8), Color(0xFF0D9488), Color(0xFF38BDF8)) // Metallic Neural Cyan
        "Eco Hero" -> listOf(Color(0xFF00D084), Color(0xFF10B981), Color(0xFF34D399)) // Emerald Chrome
        else -> listOf(Color(0xFFF43F5E), Color(0xFFFB7185), Color(0xFFF43F5E)) // Titanium Rose / Planet Guardian
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isActive) ElectricEmerald.copy(alpha = 0.8f) else StrokeHighlight.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) DarkCard.copy(alpha = 0.9f) else DarkCard.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metallic Badge visual avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.sweepGradient(metallicColors),
                        shape = CircleShape
                    )
                    .padding(2.dp)
                    .drawBehind {
                        drawCircle(color = DarkBg, radius = (size.width / 2) - 4f)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (levelName) {
                        "Starter" -> Icons.Filled.PlayArrow
                        "Conscious" -> Icons.Filled.DirectionsBike
                        "Impactful" -> Icons.Filled.ElectricCar
                        "Eco Hero" -> Icons.Filled.LockOpen
                        else -> Icons.Filled.Public
                    },
                    contentDescription = null,
                    tint = if (isActive) metallicColors[1] else TextSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = levelName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isActive) TextPrimary else TextSecondary
                    )
                    if (isActive) {
                        Text(
                            text = "ACTIVE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            modifier = Modifier
                                .background(ElectricEmerald, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = "Unlocked at $isRequiredXp XP score",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

// 6. Streak Hybrid Hybrid System (Animated leaf + flame hybrid visualization icon)
@Composable
fun StreakFlameAndLeaf(
    days: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Row(
        modifier = modifier
            .background(DarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, AccentGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer {
                    scaleX = scalePulse
                    scaleY = scalePulse
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // Draw a beautiful orange/amber flame with a green leaf center overlay
                val flamePath = Path().apply {
                    moveTo(w * 0.5f, h * 0.05f)
                    cubicTo(w * 0.25f, h * 0.3f, w * 0.1f, h * 0.6f, w * 0.15f, h * 0.8f)
                    cubicTo(w * 0.2f, h * 0.95f, w * 0.8f, h * 0.95f, w * 0.85f, h * 0.8f)
                    cubicTo(w * 0.9f, h * 0.6f, w * 0.75f, h * 0.3f, w * 0.5f, h * 0.05f)
                }
                drawPath(
                    path = flamePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF9100), Color(0xFFFF3D00))
                    )
                )

                // Leaf overlay
                val leafPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.4f)
                    cubicTo(w * 0.65f, h * 0.48f, w * 0.70f, h * 0.68f, w * 0.5f, h * 0.8f)
                    cubicTo(w * 0.3f, h * 0.68f, w * 0.35f, h * 0.48f, w * 0.5f, h * 0.4f)
                }
                drawPath(
                    path = leafPath,
                    color = ElectricEmerald
                )
            }
        }

        Text(
            text = "$days Days Habit",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = AccentGold
        )
    }
}
