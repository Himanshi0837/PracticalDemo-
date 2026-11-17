package com.example.exam.core.bluetooth

import com.example.exam.core.bluetooth.model.BleDeviceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

/**
 * Central BLE coordinator that wraps scanning and GATT operations.
 * Exposes clean domain StateFlows so ViewModels never directly interact
 * with Android BluetoothAdapter, BluetoothLeScanner, or BluetoothGatt.
 */
class BleManager(
    private val scanner: BleScanner,
    private val gattClient: BleGattClient,
    private val scope: CoroutineScope
) {
    val isBluetoothSupported: Boolean
        get() = scanner.isBluetoothSupported

    val isBluetoothEnabled: Boolean
        get() = scanner.isBluetoothEnabled

    val devices: StateFlow<List<BleDeviceModel>> = scanner.devices
    val isScanning: StateFlow<Boolean> = scanner.isScanning
    val scanError: StateFlow<BleError?> = scanner.error

    val connectionState: StateFlow<BleState> = gattClient.connectionState

    /**
     * Seamless dual-feed RSSI:
     * When GATT is connected, GATT polling RSSI is used.
     * When GATT drops or between polling ticks, the live advertisement packet RSSI
     * from BleScanner seamlessly keeps the signal, distance, and speedometer needle updating!
     */
    val activeDeviceRssi: StateFlow<Double?> = combine(
        gattClient.activeDeviceRssi,
        scanner.activeTrackedDeviceRssi
    ) { gattRssi, scanRssi ->
        gattRssi ?: scanRssi
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

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

    /**
     * Connect to GATT and simultaneously keep scan active for live advertisement tracking.
     * This ensures the speedometer gauge needle never freezes, even if the peripheral
     * drops its idle GATT connection.
     */
    fun connectAndTrack(deviceAddress: String) {
        scanner.setTrackedAddress(deviceAddress)
        if (!scanner.isScanning.value) {
            scanner.startScan()
        }
        gattClient.connect(deviceAddress)
    }

    /**
     * Stop tracking and disconnect GATT when leaving the detail screen.
     */
    fun stopTracking() {
        scanner.setTrackedAddress(null)
        gattClient.disconnect()
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
