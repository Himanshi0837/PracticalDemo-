package com.example.exam.core.bluetooth

sealed interface BleError {
    data object BluetoothNotSupported : BleError
    data object BluetoothDisabled : BleError
    data class PermissionDenied(val missingPermissions: List<String> = emptyList()) : BleError
    data class ScanFailed(val errorCode: Int, val message: String = "BLE scan failed with code $errorCode") : BleError
    data class ConnectionFailed(val reason: String = "Failed to establish GATT connection") : BleError
    data class DeviceNotFound(val address: String) : BleError
    data class OperationFailed(val message: String) : BleError
}

