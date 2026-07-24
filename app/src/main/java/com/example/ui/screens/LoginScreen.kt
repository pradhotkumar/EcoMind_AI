package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.ui.viewmodel.EcoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: EcoViewModel) {
    val isLoginLoading by viewModel.isLoginLoading.collectAsStateWithLifecycle()
    val errorText by viewModel.loginError.collectAsStateWithLifecycle()
    val defaultFirebaseUrl by viewModel.firebaseDbUrl.collectAsStateWithLifecycle()
    val defaultN8nUrl by viewModel.n8nBackendUrl.collectAsStateWithLifecycle()

    var isSignUpTab by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firebaseUrl by remember { mutableStateOf(defaultFirebaseUrl) }
    var n8nWebhookUrl by remember { mutableStateOf(defaultN8nUrl) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var showGooglePicker by remember { mutableStateOf(false) }
    var useManualEmailInput by remember { mutableStateOf(false) }
    var manualGoogleEmail by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    // Smooth tab sliding layout animations
    val infiniteTransition = rememberInfiniteTransition(label = "eco_ambient_glow")
    val radialPulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radial_pulse"
    )

    val logoPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Futuristic Glowing Environmental Web/Aura Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Soft pulsing dual green & cyan glowing radial spotlights
                    val sweepBrush = Brush.radialGradient(
                        colors = listOf(
                            ElectricEmerald.copy(alpha = 0.12f),
                            NeonCyan.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.3f, size.height * 0.2f),
                        radius = size.width * radialPulseRadius
                    )
                    drawRect(sweepBrush)

                    val techBrush = Brush.radialGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.8f, size.height * 0.8f),
                        radius = size.width * 0.9f
                    )
                    drawRect(techBrush)

                    // Draw abstract grid stars / neural nodes
                    val listNodes = listOf(
                        Offset(size.width * 0.15f, size.height * 0.4f),
                        Offset(size.width * 0.85f, size.height * 0.25f),
                        Offset(size.width * 0.72f, size.height * 0.65f),
                        Offset(size.width * 0.26f, size.height * 0.82f)
                    )
                    listNodes.forEach { node ->
                        drawCircle(
                            color = ElectricEmerald.copy(alpha = 0.25f),
                            radius = 3f,
                            center = node
                        )
                        drawCircle(
                            color = ElectricEmerald.copy(alpha = 0.08f),
                            radius = 12f,
                            center = node
                        )
                    }
                }
        )

        // Scrollable glass container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Brand Premium AI Brain + Leaf Fusion Logo
            BrainLeafFusionLogo(
                modifier = Modifier
                    .size(120.dp)
                    .testTag("brand_premium_logo")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "EcoMind AI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Your AI Sustainability Companion • “Smarter choices for a greener planet.”",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricEmerald,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )

            // Dynamic 3D-styled Onboarding Illustration system showcasing core pillars
            OnboardingIllustrationCarousel()

            Spacer(modifier = Modifier.height(16.dp))

            // Authentic Toggle Tab Switcher
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, StrokeHighlight, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SoftGlassOverlay)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sign In Button Option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (!isSignUpTab) ElectricEmerald else Color.Transparent)
                            .clickable {
                                isSignUpTab = false
                                viewModel.performLogin("", "") // resets previous errors
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Log In",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isSignUpTab) Color.Black else TextSecondary
                        )
                    }

                    // Sign Up / Cloud Setup Button Option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSignUpTab) ElectricEmerald else Color.Transparent)
                            .clickable {
                                isSignUpTab = true
                                viewModel.performLogin("", "") // resets previous errors
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Register",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSignUpTab) Color.Black else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Substantial sliding/fading form container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StrokeHighlightCyan.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.85f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (!isSignUpTab) "AUTHENTICATE AGENT SESSION" else "CREATE SUSTAINABILITY ACCOUNT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )

                    // Email / User ID Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Eco Mind Email ID") },
                        modifier = Modifier.fillMaxWidth().testTag("email_input"),
                        leadingIcon = {
                            Icon(Icons.Filled.Email, "Email", tint = ElectricEmerald)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricEmerald,
                            unfocusedBorderColor = StrokeHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = ElectricEmerald,
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Secure Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Account Password") },
                        modifier = Modifier.fillMaxWidth().testTag("password_input"),
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, "Lock", tint = ElectricEmerald)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (isSignUpTab) ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (!isSignUpTab) viewModel.performLogin(email, password)
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricEmerald,
                            unfocusedBorderColor = StrokeHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = ElectricEmerald,
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )


                }
            }

            // Error log warning messages
            if (errorText != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HighCarbonRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, HighCarbonRed.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Warning, "Error", tint = HighCarbonRed)
                        Text(
                            text = errorText!!,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Spring interactive Call To Action primary button
            val scaleInteractionSource = remember { MutableInteractionSource() }
            val isPressed by scaleInteractionSource.collectIsPressedAsState()
            val buttonScale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "button_press_spring"
            )

            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (isSignUpTab) {
                        viewModel.performRegister(email, password, n8nWebhookUrl)
                    } else {
                        viewModel.performLogin(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_login")
                    .graphicsLayer {
                        scaleX = buttonScale
                        scaleY = buttonScale
                    },
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricEmerald,
                    contentColor = Color.Black
                ),
                enabled = !isLoginLoading
            ) {
                if (isLoginLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (!isSignUpTab) "SECURE ACCESS PORTAL" else "CREATE ACCOUNT",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (!isSignUpTab) Icons.Filled.ChevronRight else Icons.Filled.PersonAdd,
                            contentDescription = "Submit Access"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Premium Google Authentication Button with User Account Picker selection dialog
            Button(
                onClick = {
                    focusManager.clearFocus()
                    showGooglePicker = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("google_login_button"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                enabled = !isLoginLoading
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GoogleLogoIcon(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign in with Google",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                }
            }

            // Simulated Google Sign In Account Choice Dialog
            if (showGooglePicker) {
                AlertDialog(
                    onDismissRequest = { showGooglePicker = false },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = true),
                    containerColor = Color(0xFF1E293B), // Premium dark slate Google card look
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GoogleLogoIcon(modifier = Modifier.size(32.dp))
                            Text(
                                text = "Choose an account",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "to continue to EcoMind AI Agent",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!useManualEmailInput) {
                                // Account 1: User's actual environment email
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showGooglePicker = false
                                            viewModel.performGoogleLogin("pradhotkumar251@gmail.com")
                                        },
                                    colors = CardDefaults.cardColors(containerColor = SoftGlassOverlay),
                                    border = BorderStroke(1.dp, StrokeHighlightCyan.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(ElectricEmerald.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("P", fontWeight = FontWeight.Bold, color = ElectricEmerald)
                                        }
                                        Column {
                                            Text("Pradhot Kumar", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                            Text("pradhotkumar251@gmail.com", fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }
                                }

                                // Account 2: Simulated general account choice
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showGooglePicker = false
                                            viewModel.performGoogleLogin("eco.champion@gmail.com")
                                        },
                                    colors = CardDefaults.cardColors(containerColor = SoftGlassOverlay),
                                    border = BorderStroke(1.dp, StrokeHighlight.copy(alpha = 0.1f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(NeonCyan.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("E", fontWeight = FontWeight.Bold, color = NeonCyan)
                                        }
                                        Column {
                                            Text("Eco Champion", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                            Text("eco.champion@gmail.com", fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }
                                }

                                // Interactive Manual Email Option
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            useManualEmailInput = true
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                    border = BorderStroke(1.dp, StrokeHighlight.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "Other Account Option",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(20.dp).padding(start = 4.dp)
                                        )
                                        Text(
                                            text = "Use another Gmail / Email",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            } else {
                                // Let user type a custom Google identifier
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = manualGoogleEmail,
                                        onValueChange = { manualGoogleEmail = it },
                                        label = { Text("Google Account Email", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth().testTag("manual_google_email_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonCyan,
                                            unfocusedBorderColor = StrokeHighlight,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedContainerColor = DarkBg,
                                            unfocusedContainerColor = DarkBg
                                        ),
                                        singleLine = true
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { useManualEmailInput = false }) {
                                            Text("Back", color = TextSecondary)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                if (manualGoogleEmail.isNotBlank() && manualGoogleEmail.contains("@")) {
                                                    showGooglePicker = false
                                                    viewModel.performGoogleLogin(manualGoogleEmail)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                                        ) {
                                            Text("Sign In", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {}
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Guest local mode bypass bypasses database authentication credentials
            OutlinedButton(
                onClick = {
                    focusManager.clearFocus()
                    // Prompt/Log user as free-tier guest local sync option
                    viewModel.performLogin("guest_warrior@ecomind.ai", "guestPassword123!")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = BorderStroke(1.dp, TextSecondary.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Local Sandbox Mode (Skip Sync)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val strokeWidth = 3.dp.toPx()
        
        // Red Top Arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 180f,
            sweepAngle = 100f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Yellow Left Arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 100f,
            sweepAngle = 80f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Green Bottom Arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 0f,
            sweepAngle = 100f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Blue Right Arc + Horizontal cross stroke
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 260f,
            sweepAngle = 100f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
