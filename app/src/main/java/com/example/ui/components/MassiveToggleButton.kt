package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashlight.FlashlightMode
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun MassiveToggleButton(
    mode: FlashlightMode,
    isLightEmitting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPermanentOn = mode == FlashlightMode.PERMANENT_ON

    // Scale animation on toggle
    val buttonScale by animateFloatAsState(
        targetValue = if (isPermanentOn) 1.03f else 1.0f,
        animationSpec = tween(durationMillis = 220),
        label = "buttonScale"
    )

    // Infinite breathing ring animation when ON
    val infiniteTransition = rememberInfiniteTransition(label = "haloTransition")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloPulse"
    )
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )

    val coreBgColor by animateColorAsState(
        targetValue = if (isPermanentOn) AmberPrimary else Color(0xFF161C26),
        animationSpec = tween(durationMillis = 300),
        label = "coreBgColor"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isPermanentOn) Color(0xFF1C1300) else TextMuted,
        animationSpec = tween(durationMillis = 300),
        label = "iconColor"
    )

    val ringBorderColor by animateColorAsState(
        targetValue = if (isPermanentOn) AmberBright else DarkSurfaceBorder,
        animationSpec = tween(durationMillis = 300),
        label = "ringBorderColor"
    )

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing light rays canvas when torch is permanently on
        if (isPermanentOn) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.width / 2f

                // Outer ambient glow ring
                drawCircle(
                    color = AmberPrimary.copy(alpha = haloAlpha),
                    radius = baseRadius * haloPulse,
                    center = center,
                    style = Stroke(width = 8.dp.toPx())
                )

                // Secondary soft glow ring
                drawCircle(
                    color = AmberGlow.copy(alpha = haloAlpha * 0.7f),
                    radius = (baseRadius * 0.92f) * haloPulse,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }

        // Tactile outer bezel container
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(buttonScale)
                .shadow(
                    elevation = if (isPermanentOn) 28.dp else 8.dp,
                    shape = CircleShape,
                    ambientColor = if (isPermanentOn) AmberPrimary else Color.Black,
                    spotColor = if (isPermanentOn) AmberGlow else Color.Black
                )
                .clip(CircleShape)
                .background(
                    if (isPermanentOn) {
                        Brush.radialGradient(
                            colors = listOf(
                                AmberBright,
                                AmberPrimary,
                                AmberDark
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF222B38),
                                Color(0xFF161C26),
                                Color(0xFF0D121A)
                            )
                        )
                    }
                )
                .border(
                    width = if (isPermanentOn) 3.5.dp else 2.dp,
                    brush = if (isPermanentOn) {
                        Brush.linearGradient(listOf(AmberBright, AmberDark))
                    } else {
                        Brush.linearGradient(listOf(DarkSurfaceBorder.copy(alpha = 0.9f), Color(0xFF12161F)))
                    },
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 100.dp, color = AmberPrimary),
                    role = Role.Button,
                    onClick = onClick
                )
                .testTag("massive_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            // Center inner circle for tactile recessed button look
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPermanentOn) {
                            Brush.linearGradient(
                                listOf(
                                    AmberPrimary,
                                    AmberDark
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1C2330),
                                    Color(0xFF111721)
                                )
                            )
                        }
                    )
                    .border(
                        1.dp,
                        if (isPermanentOn) AmberBright.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.05f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Flashlight Toggle Switch",
                        tint = iconColor,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isPermanentOn) "ON" else "OFF",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        color = iconColor
                    )
                }
            }
        }
    }
}
