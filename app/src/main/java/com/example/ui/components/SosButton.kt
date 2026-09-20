package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashlight.FlashlightMode
import com.example.ui.theme.SosRed
import com.example.ui.theme.SosRedDark
import com.example.ui.theme.SosRedGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun SosButton(
    mode: FlashlightMode,
    isLightEmitting: Boolean,
    sosActiveLetter: Char?,
    sosCycleCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSosActive = mode == FlashlightMode.SOS

    val infiniteTransition = rememberInfiniteTransition(label = "sosPulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val containerBg by animateColorAsState(
        targetValue = if (isSosActive) SosRedDark else Color(0xFF1E1418),
        animationSpec = tween(300),
        label = "sosBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSosActive) SosRedGlow else SosRed.copy(alpha = 0.5f),
        animationSpec = tween(300),
        label = "sosBorder"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(if (isSosActive) pulseScale else 1.0f)
                .shadow(
                    elevation = if (isSosActive) 16.dp else 4.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = SosRed,
                    spotColor = SosRedGlow
                )
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (isSosActive) {
                        Brush.linearGradient(
                            listOf(
                                SosRed,
                                SosRedDark,
                                Color(0xFF8B0000)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF26181C),
                                Color(0xFF181014)
                            )
                        )
                    }
                )
                .border(
                    width = if (isSosActive) 2.5.dp else 1.5.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = SosRedGlow),
                    role = Role.Button,
                    onClick = onClick
                )
                .testTag("sos_button")
                .padding(vertical = 16.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left SOS Icon & Typography
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSosActive) Color.White.copy(alpha = 0.2f)
                                else SosRed.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (isSosActive) Color.White.copy(alpha = 0.4f)
                                else SosRed.copy(alpha = 0.4f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSosActive) Icons.Default.Stop else Icons.Default.Warning,
                            contentDescription = if (isSosActive) "Stop SOS" else "Activate SOS",
                            tint = if (isSosActive) Color.White else SosRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SOS",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 3.sp,
                                    fontFamily = FontFamily.SansSerif
                                ),
                                color = if (isSosActive) Color.White else SosRed
                            )
                            if (isSosActive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color.White.copy(alpha = glowAlpha)
                                )
                            }
                        }

                        Text(
                            text = if (isSosActive) "Continuous Distress Loop" else "3 Short, 3 Long, 3 Short",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSosActive) Color.White.copy(alpha = 0.85f) else TextMuted
                        )
                    }
                }

                // Right Morse pattern visual representation
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // S: 3 short
                    LetterBadge(
                        letter = 'S',
                        code = "•••",
                        isActive = isSosActive && sosActiveLetter == 'S' && isLightEmitting,
                        isEmergencyActive = isSosActive
                    )
                    // O: 3 long
                    LetterBadge(
                        letter = 'O',
                        code = "———",
                        isActive = isSosActive && sosActiveLetter == 'O' && isLightEmitting,
                        isEmergencyActive = isSosActive
                    )
                    // S: 3 short
                    LetterBadge(
                        letter = 'S',
                        code = "•••",
                        isActive = isSosActive && sosActiveLetter == 'S' && isLightEmitting,
                        isEmergencyActive = isSosActive
                    )
                }
            }
        }
    }
}

@Composable
private fun LetterBadge(
    letter: Char,
    code: String,
    isActive: Boolean,
    isEmergencyActive: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isActive) Color.White
                else if (isEmergencyActive) Color.White.copy(alpha = 0.15f)
                else Color(0xFF2B1920)
            )
            .border(
                1.dp,
                if (isActive) Color.White
                else if (isEmergencyActive) Color.White.copy(alpha = 0.3f)
                else SosRed.copy(alpha = 0.3f),
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = letter.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                ),
                color = if (isActive) Color.Black else if (isEmergencyActive) Color.White else TextWhite
            )
            Text(
                text = code,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isActive) Color.Black else if (isEmergencyActive) Color.White.copy(alpha = 0.8f) else TextMuted
            )
        }
    }
}
