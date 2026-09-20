package com.example.flashlight

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FlashlightManager(private val context: Context) {

    private val cameraManager: CameraManager? =
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private var flashCameraId: String? = null

    private val _isHardwareTorchOn = MutableStateFlow(false)
    val isHardwareTorchOn: StateFlow<Boolean> = _isHardwareTorchOn.asStateFlow()

    private val _hasFlashUnit = MutableStateFlow(false)
    val hasFlashUnit: StateFlow<Boolean> = _hasFlashUnit.asStateFlow()

    private val _hasCameraPermission = MutableStateFlow(false)
    val hasCameraPermission: StateFlow<Boolean> = _hasCameraPermission.asStateFlow()

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == flashCameraId) {
                _isHardwareTorchOn.value = enabled
            }
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            if (cameraId == flashCameraId) {
                _isHardwareTorchOn.value = false
            }
        }
    }

    init {
        detectFlashHardware()
        checkPermission()
        try {
            if (flashCameraId != null) {
                cameraManager?.registerTorchCallback(torchCallback, null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error registering torch callback", e)
        }
    }

    fun checkPermission(): Boolean {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        _hasCameraPermission.value = granted
        return granted
    }

    private fun detectFlashHardware() {
        try {
            val hasSystemFeature = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
            var chosenCameraId: String? = null

            cameraManager?.cameraIdList?.forEach { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)

                if (hasFlash) {
                    if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                        chosenCameraId = id
                        return@forEach
                    } else if (chosenCameraId == null) {
                        chosenCameraId = id
                    }
                }
            }

            flashCameraId = chosenCameraId
            _hasFlashUnit.value = hasSystemFeature || chosenCameraId != null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to detect camera flash hardware", e)
            flashCameraId = null
            _hasFlashUnit.value = false
        }
    }

    /**
     * Sets the physical torch on or off.
     */
    fun setTorch(enabled: Boolean): Boolean {
        val id = flashCameraId
        if (id != null && cameraManager != null && _hasFlashUnit.value) {
            try {
                cameraManager.setTorchMode(id, enabled)
                _isHardwareTorchOn.value = enabled
                return true
            } catch (e: Throwable) {
                Log.w(TAG, "Exception setting torch mode to $enabled on camera $id: ${e.message}")
            }
        }
        // Fallback for emulator / simulated environment
        _isHardwareTorchOn.value = enabled
        return false
    }

    /**
     * Triggers a subtle tactile haptic pulse.
     */
    fun vibrateTactile(durationMs: Long = 25L) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            // Non-critical, ignore
        }
    }

    fun release() {
        try {
            setTorch(false)
            cameraManager?.unregisterTorchCallback(torchCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing flashlight manager", e)
        }
    }

    companion object {
        private const val TAG = "FlashlightManager"
    }
}
