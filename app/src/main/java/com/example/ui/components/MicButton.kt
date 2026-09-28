package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speech.SpeechState
import com.example.ui.theme.MayaCyan
import com.example.ui.theme.MayaViolet

@Composable
fun LargeMicControl(
    speechState: SpeechState,
    isSpeaking: Boolean,
    rmsDb: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening = speechState == SpeechState.LISTENING
    val isProcessing = speechState == SpeechState.PROCESSING

    // Infinite pulsing aura when listening or speaking
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.28f else if (isSpeaking) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = if (isListening) 0.05f else if (isSpeaking) 0.15f else 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Smooth reactive scale based on live audio volume
    val normalizedVolume = (rmsDb / 10f).coerceIn(0f, 1f)
    val reactiveAudioScale by animateFloatAsState(
        targetValue = if (isListening) 1.0f + (normalizedVolume * 0.18f) else 1.0f,
        animationSpec = tween(120),
        label = "reactive_audio"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Audio Waveform Visualizer Bars (shown when listening)
        if (isListening) {
            AudioWaveformVisualizer(rmsDb = rmsDb)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(110.dp)
        ) {
            // Outer Pulsing Glow Aura 2
            if (isListening || isSpeaking) {
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .scale(pulseScale * 1.12f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    (if (isListening) MayaCyan else MayaViolet).copy(alpha = pulseAlpha * 0.8f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Outer Pulsing Glow Aura 1
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    (if (isListening) MayaCyan else MayaViolet).copy(alpha = pulseAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Core Mic Button
            val buttonSize = 82.dp
            val gradientBrush = when {
                isListening -> Brush.linearGradient(
                    colors = listOf(Color(0xFF00E5FF), Color(0xFF0091EA))
                )
                isSpeaking -> Brush.linearGradient(
                    colors = listOf(Color(0xFF9D4EDD), Color(0xFF5A189A))
                )
                isProcessing -> Brush.linearGradient(
                    colors = listOf(Color(0xFF64748B), Color(0xFF334155))
                )
                else -> Brush.linearGradient(
                    colors = listOf(MayaCyan, MayaViolet)
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(buttonSize)
                    .scale(reactiveAudioScale)
                    .clip(CircleShape)
                    .background(gradientBrush)
                    .border(
                        width = 2.dp,
                        color = if (isListening) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.25f),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White),
                        onClick = onClick
                    )
                    .testTag("mic_button")
            ) {
                when {
                    isProcessing -> {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    isListening -> {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Listening",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    isSpeaking -> {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Maya is speaking",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Tap to Speak",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Status Subtitle
        val statusText = when {
            isListening -> "Listening... Tap to send"
            isProcessing -> "Maya is thinking..."
            isSpeaking -> "Maya is speaking... Tap to stop"
            else -> "Tap microphone to speak"
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = when {
                isListening -> MayaCyan
                isSpeaking -> MayaViolet
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
fun AudioWaveformVisualizer(rmsDb: Float) {
    val norm = (rmsDb / 10f).coerceIn(0f, 1f)

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(26.dp)
    ) {
        val bars = 7
        for (i in 0 until bars) {
            val factor = when (i) {
                0, 6 -> 0.4f
                1, 5 -> 0.65f
                2, 4 -> 0.85f
                else -> 1.0f
            }

            val barHeight by animateFloatAsState(
                targetValue = (6.dp.value + (norm * 20.dp.value * factor)).coerceIn(6f, 26f),
                animationSpec = tween(90),
                label = "bar_$i"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(MayaCyan, MayaViolet)
                        )
                    )
            )
        }
    }
}
