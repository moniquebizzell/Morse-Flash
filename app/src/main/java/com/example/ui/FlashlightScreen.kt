package com.example.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashlight.FlashlightMode
import com.example.flashlight.FlashlightViewModel
import com.example.ui.components.MassiveToggleButton
import com.example.ui.components.MorseTransmitter
import com.example.ui.components.SosButton
import com.example.ui.components.StatusHeader
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.SosRedGlow

@Composable
fun FlashlightScreen(
    viewModel: FlashlightViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    // Auto-request permission on launch if not granted
    LaunchedEffect(Unit) {
        if (!uiState.hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val requestPermissionAction: () -> Unit = {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Ambient simulated flashlight glow behind everything
            AnimatedVisibility(
                visible = uiState.isLightEmitting,
                enter = fadeIn(animationSpec = tween(100)),
                exit = fadeOut(animationSpec = tween(100))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = when (uiState.mode) {
                                    FlashlightMode.SOS -> listOf(
                                        SosRedGlow.copy(alpha = 0.35f),
                                        SosRedGlow.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                    FlashlightMode.TRANSMITTING -> listOf(
                                        com.example.ui.theme.MorseCyan.copy(alpha = 0.32f),
                                        com.example.ui.theme.MorseCyan.copy(alpha = 0.10f),
                                        Color.Transparent
                                    )
                                    else -> listOf(
                                        AmberGlow.copy(alpha = 0.35f),
                                        AmberGlow.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                }
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                // Top status bar and permission check
                StatusHeader(
                    uiState = uiState,
                    onRequestPermission = requestPermissionAction
                )

                // Scrollable content area containing the Massive Toggle Button and SOS Button
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Feature 1: Massive Toggle Button
                    MassiveToggleButton(
                        mode = uiState.mode,
                        isLightEmitting = uiState.isLightEmitting,
                        onClick = {
                            if (!uiState.hasCameraPermission) {
                                requestPermissionAction()
                            }
                            viewModel.togglePermanent()
                        }
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Feature 2: Red 'SOS' Button
                    SosButton(
                        mode = uiState.mode,
                        isLightEmitting = uiState.isLightEmitting,
                        sosActiveLetter = uiState.sosActiveLetter,
                        sosCycleCount = uiState.sosCycleCount,
                        onClick = {
                            if (!uiState.hasCameraPermission) {
                                requestPermissionAction()
                            }
                            viewModel.toggleSos()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Feature 3: Morse Code Transmitter at the bottom
                MorseTransmitter(
                    text = uiState.textToTransmit,
                    morsePreview = uiState.morsePreview,
                    mode = uiState.mode,
                    isLightEmitting = uiState.isLightEmitting,
                    progress = uiState.transmitProgress,
                    onTextChange = viewModel::onTextChange,
                    onTransmit = {
                        if (!uiState.hasCameraPermission) {
                            requestPermissionAction()
                        }
                        viewModel.transmitCurrentText()
                    },
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        }
    }
}
