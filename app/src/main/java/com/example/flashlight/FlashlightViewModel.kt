package com.example.flashlight

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.morse.MorseCode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class FlashlightMode {
    OFF,
    PERMANENT_ON,
    SOS,
    TRANSMITTING
}

enum class PulseType {
    DIT,  // Short flash
    DAH,  // Long flash
    GAP   // Light off
}

data class TransmitProgress(
    val fullText: String = "",
    val charIndex: Int = -1,
    val currentChar: Char? = null,
    val currentMorse: String = "",
    val activeSymbolIndex: Int = -1,
    val currentPulse: PulseType = PulseType.GAP,
    val progressFraction: Float = 0f
)

data class FlashlightUiState(
    val mode: FlashlightMode = FlashlightMode.OFF,
    val isLightEmitting: Boolean = false,
    val textToTransmit: String = "",
    val morsePreview: String = "",
    val transmitProgress: TransmitProgress = TransmitProgress(),
    val sosCycleCount: Int = 0,
    val sosActiveLetter: Char? = null,
    val hasCameraPermission: Boolean = false,
    val hasFlashHardware: Boolean = true
)

class FlashlightViewModel(application: Application) : AndroidViewModel(application) {

    private val flashlightManager = FlashlightManager(application.applicationContext)

    private val _uiState = MutableStateFlow(
        FlashlightUiState(
            hasCameraPermission = flashlightManager.hasCameraPermission.value,
            hasFlashHardware = flashlightManager.hasFlashUnit.value
        )
    )
    val uiState: StateFlow<FlashlightUiState> = _uiState.asStateFlow()

    private var activeJob: Job? = null

    init {
        viewModelScope.launch {
            flashlightManager.hasCameraPermission.collect { granted ->
                _uiState.value = _uiState.value.copy(hasCameraPermission = granted)
            }
        }
        viewModelScope.launch {
            flashlightManager.hasFlashUnit.collect { hasFlash ->
                _uiState.value = _uiState.value.copy(hasFlashHardware = hasFlash)
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.value = _uiState.value.copy(hasCameraPermission = isGranted)
        flashlightManager.checkPermission()
    }

    fun onTextChange(newText: String) {
        val upper = newText.uppercase()
        val preview = MorseCode.toMorse(upper)
        _uiState.value = _uiState.value.copy(
            textToTransmit = newText,
            morsePreview = preview
        )
    }

    /**
     * Feature 1: Massive Toggle Button
     * Turns the LED on and off permanently.
     */
    fun togglePermanent() {
        flashlightManager.vibrateTactile(35L)
        if (_uiState.value.mode == FlashlightMode.PERMANENT_ON) {
            turnOff()
        } else {
            stopActiveJob()
            _uiState.value = _uiState.value.copy(
                mode = FlashlightMode.PERMANENT_ON,
                isLightEmitting = true
            )
            flashlightManager.setTorch(true)
        }
    }

    /**
     * Feature 2: Red 'SOS' Button
     * Loops the Morse code SOS pattern (3 short, 3 long, 3 short) continuously until stopped.
     */
    fun toggleSos() {
        flashlightManager.vibrateTactile(45L)
        if (_uiState.value.mode == FlashlightMode.SOS) {
            turnOff()
        } else {
            startSosLoop()
        }
    }

    private fun startSosLoop() {
        stopActiveJob()
        _uiState.value = _uiState.value.copy(
            mode = FlashlightMode.SOS,
            sosCycleCount = 0,
            sosActiveLetter = null
        )

        activeJob = viewModelScope.launch {
            var cycle = 0
            while (isActive && _uiState.value.mode == FlashlightMode.SOS) {
                cycle++
                _uiState.value = _uiState.value.copy(sosCycleCount = cycle)

                // S: 3 short flashes (...)
                _uiState.value = _uiState.value.copy(sosActiveLetter = 'S')
                pulseMorseLetter("...", isShortFast = true)
                if (!isActive) break
                delay(MorseCode.INTER_CHAR_GAP_MS)

                // O: 3 long flashes (---)
                _uiState.value = _uiState.value.copy(sosActiveLetter = 'O')
                pulseMorseLetter("---", isShortFast = false)
                if (!isActive) break
                delay(MorseCode.INTER_CHAR_GAP_MS)

                // S: 3 short flashes (...)
                _uiState.value = _uiState.value.copy(sosActiveLetter = 'S')
                pulseMorseLetter("...", isShortFast = true)
                if (!isActive) break

                // Pause before next loop cycle
                _uiState.value = _uiState.value.copy(
                    isLightEmitting = false,
                    sosActiveLetter = null
                )
                delay(MorseCode.SOS_LOOP_INTERVAL_MS)
            }
        }
    }

    /**
     * Feature 3: Text input transmit
     * Translates the text string into standard Morse code and pulses the camera LED flash.
     */
    fun transmitCurrentText() {
        val text = _uiState.value.textToTransmit.trim()
        if (text.isEmpty()) return

        flashlightManager.vibrateTactile(30L)
        if (_uiState.value.mode == FlashlightMode.TRANSMITTING) {
            turnOff()
            return
        }

        stopActiveJob()
        _uiState.value = _uiState.value.copy(
            mode = FlashlightMode.TRANSMITTING,
            transmitProgress = TransmitProgress(fullText = text)
        )

        activeJob = viewModelScope.launch {
            val totalChars = text.length
            for ((index, char) in text.withIndex()) {
                if (!isActive) break

                val progress = if (totalChars > 0) (index.toFloat() / totalChars) else 0f
                val morseStr = MorseCode.charToMorse(char)

                _uiState.value = _uiState.value.copy(
                    transmitProgress = TransmitProgress(
                        fullText = text,
                        charIndex = index,
                        currentChar = char,
                        currentMorse = morseStr ?: "",
                        activeSymbolIndex = -1,
                        currentPulse = PulseType.GAP,
                        progressFraction = progress
                    )
                )

                if (char == ' ') {
                    // Word space
                    delay(MorseCode.INTER_WORD_GAP_MS)
                } else if (!morseStr.isNullOrEmpty()) {
                    for ((symbolIdx, symbol) in morseStr.withIndex()) {
                        if (!isActive) break

                        val pulseType = if (symbol == '.') PulseType.DIT else PulseType.DAH
                        val onDuration = if (symbol == '.') MorseCode.DOT_TIME_MS else MorseCode.DASH_TIME_MS

                        // Flash ON
                        flashlightManager.setTorch(true)
                        _uiState.value = _uiState.value.copy(
                            isLightEmitting = true,
                            transmitProgress = _uiState.value.transmitProgress.copy(
                                activeSymbolIndex = symbolIdx,
                                currentPulse = pulseType
                            )
                        )
                        delay(onDuration)

                        // Flash OFF
                        flashlightManager.setTorch(false)
                        _uiState.value = _uiState.value.copy(
                            isLightEmitting = false,
                            transmitProgress = _uiState.value.transmitProgress.copy(
                                currentPulse = PulseType.GAP
                            )
                        )
                        // Intra-element gap
                        delay(MorseCode.INTRA_CHAR_GAP_MS)
                    }
                    // Inter-character gap before next character
                    delay(MorseCode.INTER_CHAR_GAP_MS)
                }
            }

            // Completed transmission
            turnOff()
        }
    }

    private suspend fun pulseMorseLetter(pattern: String, isShortFast: Boolean) {
        for (symbol in pattern) {
            if (!viewModelScope.isActive) break
            val onDuration = if (symbol == '.') MorseCode.DOT_TIME_MS else MorseCode.DASH_TIME_MS

            // Torch ON
            flashlightManager.setTorch(true)
            _uiState.value = _uiState.value.copy(isLightEmitting = true)
            delay(onDuration)

            // Torch OFF
            flashlightManager.setTorch(false)
            _uiState.value = _uiState.value.copy(isLightEmitting = false)
            delay(MorseCode.INTRA_CHAR_GAP_MS)
        }
    }

    fun turnOff() {
        stopActiveJob()
        flashlightManager.setTorch(false)
        _uiState.value = _uiState.value.copy(
            mode = FlashlightMode.OFF,
            isLightEmitting = false,
            sosActiveLetter = null,
            transmitProgress = TransmitProgress(fullText = _uiState.value.textToTransmit)
        )
    }

    private fun stopActiveJob() {
        activeJob?.cancel()
        activeJob = null
        flashlightManager.setTorch(false)
    }

    override fun onCleared() {
        super.onCleared()
        stopActiveJob()
        flashlightManager.release()
    }
}
