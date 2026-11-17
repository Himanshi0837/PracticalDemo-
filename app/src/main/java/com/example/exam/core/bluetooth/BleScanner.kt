
package com.example.exam.core.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.example.exam.core.bluetooth.model.BleDeviceModel
import com.example.exam.core.bluetooth.signal.KalmanFilter
import com.example.exam.core.bluetooth.signal.RssiWindowBuffer
import com.example.exam.core.common.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles Bluetooth Low Energy device discovery,
 * RSSI smoothing, and out-of-range device pruning.
 */
class BleScanner(
    private val btAdapter: BluetoothAdapter?,
    private val scope: CoroutineScope,
    private val context: Context
) {

    private val tag = "BleScanner"

    private val scanLock = Any()
    private val deviceLock = Any()

    private val _devicesList =
        MutableStateFlow<List<BleDeviceModel>>(emptyList())

    val devices: StateFlow<List<BleDeviceModel>> =
        _devicesList.asStateFlow()

    private val _isScanning = MutableStateFlow(false)

    val isScanning: StateFlow<Boolean> =
        _isScanning.asStateFlow()

    private val _error = MutableStateFlow<BleError?>(null)

    val error: StateFlow<BleError?> =
        _error.asStateFlow()

    private val _activeTrackedDeviceRssi = MutableStateFlow<Double?>(null)
    val activeTrackedDeviceRssi: StateFlow<Double?> = _activeTrackedDeviceRssi.asStateFlow()

    @Volatile
    var activeTrackedAddress: String? = null
        private set

    fun setTrackedAddress(address: String?) {
        activeTrackedAddress = address
        if (address == null) {
            _activeTrackedDeviceRssi.value = null
        } else {
            val existing = _devicesList.value.firstOrNull { it.id.equals(address, ignoreCase = true) }
            if (existing != null) {
                _activeTrackedDeviceRssi.value = existing.filteredRssi
            }
        }
    }

    private var btScanner: BluetoothLeScanner? = null

    private var outOfRangeJob: Job? = null

    private val deviceFilters =
        ConcurrentHashMap<String, KalmanFilter>()

    private val deviceBuffers =
        ConcurrentHashMap<String, RssiWindowBuffer>()

    val isBluetoothSupported: Boolean
        get() = btAdapter != null

    val isBluetoothEnabled: Boolean
        get() = btAdapter?.isEnabled == true

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanFilters: List<ScanFilter> =
        emptyList()

    // ---------------------------------------------------------
    // BLE SCAN CALLBACK
    // ---------------------------------------------------------

    private val scanCallback = object : ScanCallback() {

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

            Log.e(
                tag,
                "BLE scan failed. Error code: $errorCode"
            )

            synchronized(scanLock) {
                _isScanning.value = false
            }

            _error.value = BleError.ScanFailed(errorCode)
        }

        @SuppressLint("MissingPermission")
        override fun onScanResult(
            callbackType: Int,
            result: ScanResult
        ) {
            super.onScanResult(callbackType, result)

            processScanResult(result)
        }

        override fun onBatchScanResults(
            results: MutableList<ScanResult>?
        ) {
            super.onBatchScanResults(results)

            results?.forEach { result ->
                processScanResult(result)
            }
        }
    }

    // ---------------------------------------------------------
    // INITIALIZATION
    // ---------------------------------------------------------

    init {
        startOutOfRangeMonitor()
    }

    // ---------------------------------------------------------
    // PROCESS SCAN RESULT
    // ---------------------------------------------------------

    @SuppressLint("MissingPermission")
    private fun processScanResult(result: ScanResult) {

        scope.launch {

            try {

                val device = result.device
                val address = device.address

                if (address.isBlank()) return@launch

                val currentTime = System.currentTimeMillis()

                val rawName = try {
                    device.name
                        ?: result.scanRecord?.deviceName
                } catch (e: SecurityException) {
                    Log.w(
                        tag,
                        "Unable to read device name: ${e.message}"
                    )
                    null
                }

                val name = if (!rawName.isNullOrBlank()) {
                    rawName
                } else {
                    address
                }

                synchronized(deviceLock) {

                    // Get or create filters for this device.
                    val kalmanFilter =
                        deviceFilters.getOrPut(address) {
                            KalmanFilter()
                        }

                    val rssiBuffer =
                        deviceBuffers.getOrPut(address) {
                            RssiWindowBuffer()
                        }

                    // RSSI smoothing.
                    val filteredRssi =
                        kalmanFilter.filter(
                            result.rssi.toDouble()
                        )

                    val stableRssi =
                        rssiBuffer.addAndAverage(filteredRssi)

                    // Create updated BLE device.
                    val updatedDevice =
                        BleDeviceModel.create(
                            id = address,
                            name = name,
                            rawRssi = result.rssi,
                            smoothedRssi = stableRssi,
                            scanResult = result,
                            lastSeen = currentTime
                        )

                    val currentList =
                        _devicesList.value.toMutableList()

                    val existingIndex =
                        currentList.indexOfFirst {
                            it.id == address
                        }

                    if (existingIndex >= 0) {

                        currentList[existingIndex] =
                            updatedDevice

                    } else {

                        currentList.add(updatedDevice)
                    }

                    // Closest devices first.
                    currentList.sortBy {
                        it.distanceMeters
                    }

                    _devicesList.value = currentList

                    if (address.equals(activeTrackedAddress, ignoreCase = true)) {
                        _activeTrackedDeviceRssi.value = stableRssi
                    }
                }

            } catch (e: SecurityException) {

                Log.e(
                    tag,
                    "BLE permission error while processing result",
                    e
                )

                _error.value = BleError.PermissionDenied()

            } catch (e: Exception) {

                Log.e(
                    tag,
                    "Error processing BLE scan result",
                    e
                )
            }
        }
    }

    // ---------------------------------------------------------
    // OUT OF RANGE DEVICE MONITOR
    // ---------------------------------------------------------

    private fun startOutOfRangeMonitor() {

        if (outOfRangeJob?.isActive == true) {
            return
        }

        outOfRangeJob = scope.launch {

            while (isActive) {

                delay(
                    Constants.BLE_OUT_OF_RANGE_CHECK_INTERVAL_MS
                )

                val currentTime =
                    System.currentTimeMillis()

                synchronized(deviceLock) {

                    val currentList =
                        _devicesList.value

                    val freshList =
                        currentList.filter { device ->
                            device.id.equals(activeTrackedAddress, ignoreCase = true) ||
                                (currentTime - device.lastSeen <= Constants.BLE_SCAN_TIMEOUT_MS)
                        }

                    if (freshList.size != currentList.size) {

                        _devicesList.value = freshList

                        val activeAddresses =
                            freshList.map { it.id }.toSet()

                        deviceFilters.keys.removeAll {
                            it !in activeAddresses
                        }

                        deviceBuffers.keys.removeAll {
                            it !in activeAddresses
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------
    // START SCANNING
    // ---------------------------------------------------------

    @SuppressLint("MissingPermission")
    fun startScan() {

        if (!isBluetoothSupported) {

            _error.value =
                BleError.BluetoothNotSupported

            return
        }

        if (!isBluetoothEnabled) {

            _error.value =
                BleError.BluetoothDisabled

            return
        }

        if (!BlePermissionHelper.hasRequiredPermissions(context)) {

            _error.value =
                BleError.PermissionDenied(
                    BlePermissionHelper.getMissingPermissions(context)
                )

            return
        }

        synchronized(scanLock) {

            if (_isScanning.value) {
                Log.d(tag, "BLE scan already running")
                return
            }

            try {

                _error.value = null

                // Restart monitor if it was previously cleaned up.
                startOutOfRangeMonitor()

                val scanner =
                    btAdapter?.bluetoothLeScanner

                if (scanner == null) {

                    _isScanning.value = false

                    _error.value =
                        BleError.OperationFailed(
                            "Bluetooth scanner is unavailable"
                        )

                    return
                }

                btScanner = scanner

                scanner.startScan(
                    scanFilters,
                    scanSettings,
                    scanCallback
                )

                // The scan request was accepted.
                _isScanning.value = true

                Log.d(
                    tag,
                    "BLE scanning started successfully"
                )

            } catch (e: SecurityException) {

                _isScanning.value = false

                _error.value =
                    BleError.PermissionDenied()

                Log.e(
                    tag,
                    "Permission error while starting scan",
                    e
                )

            } catch (e: Exception) {

                _isScanning.value = false

                _error.value =
                    BleError.OperationFailed(
                        e.localizedMessage
                            ?: "Failed to start BLE scan"
                    )

                Log.e(
                    tag,
                    "Exception starting BLE scan",
                    e
                )
            }
        }
    }

    // ---------------------------------------------------------
    // STOP SCANNING
    // ---------------------------------------------------------

    @SuppressLint("MissingPermission")
    fun stopScan() {

        synchronized(scanLock) {

            try {

                if (
                    btScanner != null &&
                    isBluetoothEnabled &&
                    BlePermissionHelper.hasRequiredPermissions(context)
                ) {

                    btScanner?.stopScan(scanCallback)
                }

            } catch (e: SecurityException) {

                Log.e(
                    tag,
                    "Permission error while stopping scan",
                    e
                )

            } catch (e: Exception) {

                Log.e(
                    tag,
                    "Error stopping BLE scan",
                    e
                )

            } finally {

                _isScanning.value = false

                Log.d(
                    tag,
                    "BLE scanning stopped"
                )
            }
        }
    }

    // ---------------------------------------------------------
    // CLEAR DEVICES
    // ---------------------------------------------------------

    fun clearDevices() {

        synchronized(deviceLock) {

            _devicesList.value = emptyList()

            deviceFilters.clear()

            deviceBuffers.clear()
        }
    }

    // ---------------------------------------------------------
    // CLEANUP
    // ---------------------------------------------------------

    fun cleanup() {

        Log.d(tag, "Cleaning up BLE scanner")

        stopScan()

        outOfRangeJob?.cancel()
        outOfRangeJob = null

        clearDevices()
    }
}
