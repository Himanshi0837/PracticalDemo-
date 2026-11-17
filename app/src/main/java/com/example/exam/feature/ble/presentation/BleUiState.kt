package com.example.exam.feature.ble.presentation

import com.example.exam.core.bluetooth.BleError
import com.example.exam.core.bluetooth.BleState
import com.example.exam.core.bluetooth.model.BleDeviceModel

data class BleUiState(
    val isScanning: Boolean = false,
    val isBluetoothEnabled: Boolean = true,
    val isBluetoothSupported: Boolean = true,
    val devices: List<BleDeviceModel> = emptyList(),
    val connectionState: BleState = BleState.Idle,
    val trackedDeviceRssi: Double? = null,
    val error: BleError? = null,
    val userMessage: String? = null
)

