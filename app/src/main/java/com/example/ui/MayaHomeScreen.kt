package com.example.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AiEngineType
import com.example.speech.SpeechState
import com.example.ui.components.ConversationView
import com.example.ui.components.LargeMicControl
import com.example.ui.components.QuickSuggestionsRow
import com.example.ui.components.SettingsSheet
import com.example.ui.theme.MayaCyan
import com.example.ui.theme.MayaTeal
import com.example.ui.theme.MayaViolet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MayaHomeScreen(
    viewModel: MayaViewModel,
    onRequestRecordPermission: () -> Unit,
    hasRecordPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val activeLanguage by viewModel.activeLanguage.collectAsStateWithLifecycle()
    val speechState by viewModel.speechState.collectAsStateWithLifecycle()
    val partialSpeechText by viewModel.partialSpeechText.collectAsStateWithLifecycle()
    val rmsDb by viewModel.rmsDb.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val currentSpeakingMessageId by viewModel.currentSpeakingMessageId.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var isTextInputVisible by remember { mutableStateOf(false) }
    var typedText by remember { mutableStateOf("") }

    // Scroll to bottom whenever new message or thinking state changes
    LaunchedEffect(messages.size, isAiThinking, partialSpeechText) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    // Display error messages in snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Glowing Maya Avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(MayaCyan, MayaViolet)))
                                .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Maya AI",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Maya AI",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Online / Offline indicator badge
                                val engineLabel = when (settings.engineType) {
                                    AiEngineType.OFFLINE_RULE -> "Offline"
                                    AiEngineType.GEMINI_CLOUD -> "Gemini"
                                    AiEngineType.AUTO -> "Smart Auto"
                                    AiEngineType.CUSTOM_API -> "Custom"
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = engineLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Text(
                                text = "Voice Assistant • ${activeLanguage.displayName}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Quick Language Switcher Button (opens settings or toggles)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { showSettingsSheet = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("language_badge_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = activeLanguage.flagEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = activeLanguage.nativeName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // TTS Stop Button (visible when actively speaking)
                    if (isSpeaking) {
                        IconButton(
                            onClick = { viewModel.stopSpeaking() },
                            modifier = Modifier.testTag("stop_speaking_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = "Mute Voice",
                                tint = MayaCyan
                            )
                        }
                    }

                    // Settings Button
                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Conversation Display Area (scrollable)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                ConversationView(
                    messages = messages,
                    listState = listState,
                    isProcessing = isAiThinking,
                    partialSpeechText = partialSpeechText,
                    currentSpeakingMessageId = currentSpeakingMessageId,
                    activeLanguage = activeLanguage,
                    onSpeakMessage = { msg -> viewModel.speakMessage(msg) },
                    onToggleFavorite = { msg -> viewModel.toggleFavorite(msg) },
                    onSuggestionClick = { prompt ->
                        viewModel.sendMessage(prompt)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Quick Suggestions Row (visible when not typing and conversation has items)
            if (messages.isNotEmpty() && !isTextInputVisible) {
                QuickSuggestionsRow(
                    activeLanguage = activeLanguage,
                    onSuggestionClick = { prompt -> viewModel.sendMessage(prompt) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            // 3. Optional Text Input Bar
            AnimatedVisibility(
                visible = isTextInputVisible,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = typedText,
                            onValueChange = { typedText = it },
                            placeholder = {
                                Text(
                                    text = if (activeLanguage.code == "ur") "مایا سے بات کریں..." else "Type message to Maya...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("text_input_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            maxLines = 3
                        )

                        IconButton(
                            onClick = {
                                if (typedText.isNotBlank()) {
                                    val sendQuery = typedText
                                    typedText = ""
                                    viewModel.sendMessage(sendQuery)
                                }
                            },
                            modifier = Modifier.testTag("send_text_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { isTextInputVisible = false }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Keyboard",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 4. Voice Controls Dock (with Large Microphone button)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left action: Keyboard / Text Toggle
                    IconButton(
                        onClick = { isTextInputVisible = !isTextInputVisible },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("toggle_keyboard_button")
                    ) {
                        Icon(
                            imageVector = if (isTextInputVisible) Icons.Default.KeyboardHide else Icons.Default.Keyboard,
                            contentDescription = "Toggle Keyboard Input",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Center: Large Animated Microphone Button
                    LargeMicControl(
                        speechState = speechState,
                        isSpeaking = isSpeaking,
                        rmsDb = rmsDb,
                        onClick = {
                            if (!hasRecordPermission) {
                                onRequestRecordPermission()
                            } else {
                                viewModel.toggleListening()
                            }
                        }
                    )

                    // Right action: Stop TTS or Quick Language Dialog
                    if (isSpeaking) {
                        IconButton(
                            onClick = { viewModel.stopSpeaking() },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MayaCyan.copy(alpha = 0.2f))
                                .testTag("mic_dock_stop_speaking_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.StopCircle,
                                contentDescription = "Stop Voice",
                                tint = MayaCyan
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { showSettingsSheet = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("dock_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Settings Bottom Sheet
    if (showSettingsSheet) {
        SettingsSheet(
            settings = settings,
            onDismiss = { showSettingsSheet = false },
            onLanguageSelected = { lang ->
                viewModel.setLanguage(lang)
            },
            onEngineSelected = { engine ->
                viewModel.setEngine(engine)
            },
            onSpeechRateChanged = { rate ->
                viewModel.setSpeechRate(rate)
            },
            onSpeechPitchChanged = { pitch ->
                viewModel.setSpeechPitch(pitch)
            },
            onAutoSpeakToggled = { auto ->
                viewModel.setAutoSpeak(auto)
            },
            onApiKeyChanged = { key ->
                viewModel.setCustomApiKey(key)
            },
            onApiUrlChanged = { url ->
                viewModel.setCustomApiUrl(url)
            },
            onTestVoice = {
                viewModel.testVoice()
            },
            onClearHistory = {
                viewModel.clearAllMessages()
            },
            onExportHistory = {
                val exported = viewModel.exportConversation()
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, exported)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Export Maya AI Conversation")
                context.startActivity(shareIntent)
            }
        )
    }
}
