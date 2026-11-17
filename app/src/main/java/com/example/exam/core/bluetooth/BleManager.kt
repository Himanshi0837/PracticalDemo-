package com.example.exam.core.bluetooth

import com.example.exam.core.bluetooth.model.BleDeviceModel
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

/**
 * Central BLE coordinator that wraps scanning and GATT operations.
 * Exposes clean domain StateFlows so ViewModels never directly interact
 * with Android BluetoothAdapter, BluetoothLeScanner, or BluetoothGatt.
 */
class BleManager(
    private val scanner: BleScanner,
    private val gattClient: BleGattClient
) {
    val isBluetoothSupported: Boolean
        get() = scanner.isBluetoothSupported

    val isBluetoothEnabled: Boolean
        get() = scanner.isBluetoothEnabled

    val devices: StateFlow<List<BleDeviceModel>> = scanner.devices
    val isScanning: StateFlow<Boolean> = scanner.isScanning
    val scanError: StateFlow<BleError?> = scanner.error

    val connectionState: StateFlow<BleState> = gattClient.connectionState
    val activeDeviceRssi: StateFlow<Double?> = gattClient.activeDeviceRssi

    fun startScan() {
        scanner.startScan()
    }

    fun stopScan() {
        scanner.stopScan()
    }

    fun clearDevices() {
        scanner.clearDevices()
    }

    fun connect(deviceAddress: String) {
        gattClient.connect(deviceAddress)
    }

    fun disconnect() {
        gattClient.disconnect()
    }

    fun readCharacteristic(serviceUuid: UUID, characteristicUuid: UUID) {
        gattClient.readCharacteristic(serviceUuid, characteristicUuid)
    }

    fun writeCharacteristic(serviceUuid: UUID, characteristicUuid: UUID, data: ByteArray) {
        gattClient.writeCharacteristic(serviceUuid, characteristicUuid, data)
    }

    fun enableNotifications(serviceUuid: UUID, characteristicUuid: UUID, enable: Boolean) {
        gattClient.enableNotifications(serviceUuid, characteristicUuid, enable)
    }

    fun cleanup() {
        scanner.cleanup()
        gattClient.disconnect()
    }
}

