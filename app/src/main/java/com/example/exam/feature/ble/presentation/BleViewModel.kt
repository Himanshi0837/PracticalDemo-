
package com.example.exam.feature.ble.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exam.core.bluetooth.BleManager
import com.example.exam.core.bluetooth.BleState
import com.example.exam.core.bluetooth.model.BleDeviceModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private data class BleDeviceSnapshot(
    val devices: List<BleDeviceModel>,
    val isScanning: Boolean,
    val connectionState: BleState,
    val activeRssi: Double?
)

@HiltViewModel
class BleViewModel @Inject constructor(
    private val bleManager: BleManager
) : ViewModel() {

    companion object {
        private const val TAG = "BleViewModel"
    }

    private val userMessage = MutableStateFlow<String?>(null)

    private val deviceSnapshot = combine(
        bleManager.devices,
        bleManager.isScanning,
        bleManager.connectionState,
        bleManager.activeDeviceRssi
    ) { devices, isScanning, connectionState, activeRssi ->

        BleDeviceSnapshot(
            devices = devices,
            isScanning = isScanning,
            connectionState = connectionState,
            activeRssi = activeRssi
        )
    }

    val uiState: StateFlow<BleUiState> = combine(
        deviceSnapshot,
        bleManager.scanError,
        userMessage
    ) { snapshot, scanError, message ->

        val connectionError =
            (snapshot.connectionState as? BleState.Error)?.error

        BleUiState(
            isScanning = snapshot.isScanning,
            isBluetoothEnabled = bleManager.isBluetoothEnabled,
            isBluetoothSupported = bleManager.isBluetoothSupported,
            devices = snapshot.devices,
            connectionState = snapshot.connectionState,
            trackedDeviceRssi = snapshot.activeRssi,
            error = connectionError ?: scanError,
            userMessage = message
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BleUiState()
    )

    fun startScan() {
        userMessage.value = null

        if (!bleManager.isBluetoothSupported) {
            userMessage.value =
                "Bluetooth is not supported on this device."
            return
        }

        if (!bleManager.isBluetoothEnabled) {
            userMessage.value =
                "Bluetooth is disabled. Please enable Bluetooth."
            return
        }

        Log.d(TAG, "Starting BLE scan")
        bleManager.startScan()
    }

    fun stopScan() {
        Log.d(TAG, "Stopping BLE scan")
        bleManager.stopScan()
    }

    fun toggleScan() {
        if (uiState.value.isScanning) {
            stopScan()
        } else {
            startScan()
        }
    }

    fun refresh() {
        clearDevices()
        if (uiState.value.isScanning) {
            stopScan()
        }
        startScan()
    }

    fun clearDevices() {
        bleManager.clearDevices()
    }

    fun connectDevice(address: String) {
        userMessage.value = null

        if (!bleManager.isBluetoothSupported) {
            userMessage.value =
                "Bluetooth is not supported on this device."
            return
        }

        if (!bleManager.isBluetoothEnabled) {
            userMessage.value =
                "Bluetooth is disabled. Please enable Bluetooth."
            return
        }

        Log.d(TAG, "Connecting and tracking device: $address")
        // Keep scan active for continuous advertisement RSSI feed, and connect GATT
        bleManager.connectAndTrack(address)
    }

    fun disconnectDevice() {
        userMessage.value = null

        Log.d(TAG, "User requested device disconnection")
        bleManager.disconnect()
    }

    fun stopTracking() {
        Log.d(TAG, "Stopping device tracking and disconnecting")
        bleManager.stopTracking()
    }

    fun clearMessage() {
        userMessage.value = null
    }

    override fun onCleared() {
        Log.w(TAG, "BleViewModel cleared - cleaning up BLE manager")

        bleManager.cleanup()

        super.onCleared()
    }
}
