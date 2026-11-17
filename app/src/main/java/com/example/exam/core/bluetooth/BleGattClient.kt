package com.example.exam.core.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.exam.core.bluetooth.signal.KalmanFilter
import com.example.exam.core.bluetooth.signal.RssiWindowBuffer
import com.example.exam.core.common.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class BleGattClient(
    private val btAdapter: BluetoothAdapter?,
    private val scope: CoroutineScope,
    private val context: Context
) {

    private val tag = "BleGattClient"

    private val connectionTimeoutMs = 30_000L
    private val disconnectTimeoutMs = 3_000L
    private var rssiRequestTimeoutJob: Job? = null
    private val _connectionState =
        MutableStateFlow<BleState>(BleState.Idle)

    val connectionState: StateFlow<BleState> =
        _connectionState.asStateFlow()

    private val _activeDeviceRssi =
        MutableStateFlow<Double?>(null)

    val activeDeviceRssi: StateFlow<Double?> =
        _activeDeviceRssi.asStateFlow()

    private var activeGatt: BluetoothGatt? = null
    private var activeAddress: String? = null

    private var connectionTimeoutJob: Job? = null
    private var disconnectTimeoutJob: Job? = null
    private var rssiPollJob: Job? = null

    private var disconnectRequested = false

    private val rssiRequestPending = AtomicBoolean(false)

    private val kalmanFilter = KalmanFilter()
    private val windowBuffer = RssiWindowBuffer()

    companion object {
        private val CLIENT_CONFIGURATION_UUID: UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
    private fun logGattEvent(
        event: String,
        details: String = ""
    ) {
        Log.w(
            tag,
            """
        ========== GATT EVENT ==========
        Event: $event
        Active Address: $activeAddress
        Active GATT: ${activeGatt != null}
        Disconnect Requested: $disconnectRequested
        Connection State: ${_connectionState.value}
        $details
        ================================
        """.trimIndent()
        )
    }
    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            Log.w(
                tag,
                """
                ========== GATT CALLBACK ==========
                Address: ${gatt.device.address}
                Status: $status
                New State: $newState
                Is Active GATT: ${gatt === activeGatt}
                Disconnect Requested: $disconnectRequested
                Active Address: $activeAddress
                Current State: ${_connectionState.value}
                ===================================
                """.trimIndent()
            )

            if (gatt !== activeGatt) {
                Log.w(tag, "Ignoring callback from stale GATT: ${gatt.device.address}")
                try {
                    gatt.close()
                } catch (e: Exception) {
                    Log.e(tag, "Failed to close stale GATT", e)
                }
                return
            }

            val address = activeAddress ?: gatt.device.address

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(tag, "GATT connection failed. Status: $status")

                stopRssiPolling()
                cancelConnectionTimeout()

                _connectionState.value = BleState.Error(
                    BleError.ConnectionFailed(
                        "GATT connection failed with status: $status"
                    )
                )

                closeGatt(gatt)
                return
            }

            when (newState) {

                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(tag, "Connected to $address")

                    disconnectRequested = false
                    cancelConnectionTimeout()

                    _connectionState.value =
                        BleState.DiscoveringServices(address)

                    startRssiPolling(gatt)

                    val discoveryStarted = try {
                        gatt.discoverServices()
                    } catch (e: SecurityException) {
                        Log.e(tag, "Service discovery permission denied", e)
                        false
                    }

                    if (!discoveryStarted) {
                        _connectionState.value = BleState.Error(
                            BleError.OperationFailed(
                                "Unable to start service discovery"
                            )
                        )
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.e(
                        tag,
                        """
                        ========== DEVICE DISCONNECTED ==========
                        Address: $address
                        Status: $status
                        Local Disconnect Requested: $disconnectRequested
                        =========================================
                        """.trimIndent()
                    )

                    stopRssiPolling()
                    cancelConnectionTimeout()
                    disconnectTimeoutJob?.cancel()
                    disconnectTimeoutJob = null

                    _connectionState.value = BleState.Disconnected(address)
                    closeGatt(gatt)
                }
            }
        }

        override fun onServicesDiscovered(
            gatt: BluetoothGatt,
            status: Int
        ) {
            if (gatt !== activeGatt) return

            val address = activeAddress ?: "Unknown"

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(tag, "Services discovered for $address")

                gatt.services.forEach { service ->
                    Log.d(tag, "Service: ${service.uuid}")

                    service.characteristics.forEach { characteristic ->
                        Log.d(
                            tag,
                            "Characteristic: ${characteristic.uuid}"
                        )
                    }
                }

                _connectionState.value = BleState.Connected(address)

            } else {
                Log.e(tag, "Service discovery failed for $address. Status: $status")

                _connectionState.value = BleState.Error(
                    BleError.OperationFailed(
                        "Service discovery failed with status: $status"
                    )
                )
            }
        }

        override fun onReadRemoteRssi(
            gatt: BluetoothGatt,
            rssi: Int,
            status: Int
        ) {
            if (gatt !== activeGatt) return

            rssiRequestTimeoutJob?.cancel()
            rssiRequestTimeoutJob = null

            rssiRequestPending.set(false)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                val filtered = kalmanFilter.filter(rssi.toDouble())
                val smoothed = windowBuffer.addAndAverage(filtered)

                _activeDeviceRssi.value = smoothed

                Log.d(tag, "Live RSSI: $smoothed dBm")
            } else {
                Log.w(tag, "RSSI read failed for ${activeAddress ?: "Unknown"}. Status: $status")
            }
        }

        @Deprecated("Deprecated in Android 13")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (gatt !== activeGatt) return

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(
                    tag,
                    "Characteristic read: ${characteristic.uuid}, " +
                            "value: ${characteristic.value?.contentToString()}"
                )
            } else {
                Log.e(tag, "Characteristic read failed: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (gatt !== activeGatt) return

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(
                    tag,
                    "Characteristic read: ${characteristic.uuid}, " +
                            "value: ${value.contentToString()}"
                )
            } else {
                Log.e(tag, "Characteristic read failed: $status")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (gatt !== activeGatt) return

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(
                    tag,
                    "Characteristic write successful: ${characteristic.uuid}"
                )
            } else {
                Log.e(tag, "Characteristic write failed: $status")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            if (gatt !== activeGatt) return

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(
                    tag,
                    "Descriptor write successful: ${descriptor.uuid}"
                )
            } else {
                Log.e(tag, "Descriptor write failed: $status")
            }
        }
    }

    // ----------------------------------------------------
    // CONNECT
    // ----------------------------------------------------

    @SuppressLint("MissingPermission")
    fun connect(deviceAddress: String) {

        logGattEvent(
            event = "CONNECT REQUEST",
            details = "Requested Address: $deviceAddress"
        )

        val currentState = _connectionState.value
        val alreadyHandlingSameDevice =
            activeAddress == deviceAddress &&
                    activeGatt != null &&
                    (
                            currentState is BleState.Connecting ||
                                    currentState is BleState.DiscoveringServices ||
                                    currentState is BleState.Connected
                            )

        if (alreadyHandlingSameDevice) {
            logGattEvent(
                event = "DUPLICATE CONNECT IGNORED",
                details = "Already connecting or connected to $deviceAddress"
            )
            return
        }

        if (btAdapter == null) {
            _connectionState.value =
                BleState.Error(BleError.BluetoothNotSupported)
            return
        }

        if (!btAdapter.isEnabled) {
            _connectionState.value =
                BleState.Error(BleError.BluetoothDisabled)
            return
        }

        if (!BlePermissionHelper.hasRequiredPermissions(context)) {
            _connectionState.value = BleState.Error(
                BleError.PermissionDenied(
                    BlePermissionHelper.getMissingPermissions(context)
                )
            )
            return
        }

        closeExistingGatt(
            reason = "Starting a new connection to $deviceAddress"
        )

        activeAddress = deviceAddress
        disconnectRequested = false

        kalmanFilter.reset()
        windowBuffer.clear()
        _activeDeviceRssi.value = null

        _connectionState.value =
            BleState.Connecting(deviceAddress)

        val device = try {
            btAdapter.getRemoteDevice(deviceAddress)
        } catch (e: IllegalArgumentException) {
            null
        } catch (e: SecurityException) {
            _connectionState.value =
                BleState.Error(BleError.PermissionDenied())
            return
        }

        if (device == null) {
            activeAddress = null

            _connectionState.value =
                BleState.Error(BleError.DeviceNotFound(deviceAddress))
            return
        }

        try {
            activeGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(
                    context,
                    false,
                    gattCallback,
                    BluetoothDevice.TRANSPORT_LE
                )
            } else {
                @Suppress("DEPRECATION")
                device.connectGatt(
                    context,
                    false,
                    gattCallback
                )
            }

            if (activeGatt == null) {
                Log.e(tag, "connectGatt returned null for $deviceAddress")
                _connectionState.value = BleState.Error(
                    BleError.ConnectionFailed(
                        "Unable to initialize GATT connection"
                    )
                )
                activeAddress = null
                return
            }

            logGattEvent(
                event = "GATT CONNECTION INITIALIZED",
                details = "Requested Address: $deviceAddress"
            )
            startConnectionTimeout(deviceAddress)

        } catch (e: SecurityException) {
            activeAddress = null

            _connectionState.value =
                BleState.Error(BleError.PermissionDenied())

        } catch (e: Exception) {
            activeAddress = null

            Log.e(tag, "Connection error", e)

            _connectionState.value = BleState.Error(
                BleError.ConnectionFailed(
                    e.localizedMessage ?: "Unknown connection error"
                )
            )
        }
    }

    // ----------------------------------------------------
    // CONNECTION TIMEOUT
    // ----------------------------------------------------

    private fun startConnectionTimeout(address: String) {
        cancelConnectionTimeout()

        connectionTimeoutJob = scope.launch {
            delay(connectionTimeoutMs)

            val state = _connectionState.value

            if (
                activeAddress == address &&
                (state is BleState.Connecting ||
                        state is BleState.DiscoveringServices)
            ) {
                Log.e(
                    tag,
                    "CONNECTION TIMEOUT | address=$address | " +
                            "state=$state | timeout=${connectionTimeoutMs}ms"
                )

                _connectionState.value = BleState.Error(
                    BleError.ConnectionFailed(
                        "Connection timed out after ${connectionTimeoutMs / 1000} seconds"
                    )
                )

                closeExistingGatt(reason = "Connection timeout for $address")
            }
        }
    }

    private fun cancelConnectionTimeout() {
        connectionTimeoutJob?.cancel()
        connectionTimeoutJob = null
    }

    // ----------------------------------------------------
    // RSSI POLLING
    // ----------------------------------------------------

    @SuppressLint("MissingPermission")
    private fun startRssiPolling(gatt: BluetoothGatt) {

        stopRssiPolling()

        rssiPollJob = scope.launch(Dispatchers.IO) {

            while (isActive && activeGatt === gatt) {

                if (!BlePermissionHelper.hasRequiredPermissions(context)) {
                    Log.w(tag, "BLE permission unavailable during RSSI polling")
                    break
                }

                if (rssiRequestPending.compareAndSet(false, true)) {
                    try {
                        val requestStarted = gatt.readRemoteRssi()

                        if (requestStarted) {
                            rssiRequestTimeoutJob?.cancel()

                            rssiRequestTimeoutJob = scope.launch {
                                delay(5000)

                                if (rssiRequestPending.compareAndSet(true, false)) {
                                    Log.w(tag, "RSSI request timed out")
                                }
                            }
                        } else {
                            rssiRequestPending.set(false)
                        }

                    } catch (e: Exception) {
                        rssiRequestPending.set(false)
                        Log.e(tag, "RSSI request failed", e)
                    }
                }
                delay(Constants.BLE_GATT_RSSI_POLL_INTERVAL_MS)
            }

            rssiRequestPending.set(false)
        }
    }

    private fun stopRssiPolling() {
        rssiPollJob?.cancel()
        rssiPollJob = null

        rssiRequestPending.set(false)
        _activeDeviceRssi.value = null
        rssiRequestTimeoutJob?.cancel()
        rssiRequestTimeoutJob = null
    }

    // ----------------------------------------------------
    // DISCONNECT
    // ----------------------------------------------------

    @SuppressLint("MissingPermission")
    fun disconnect() {

        logGattEvent(
            event = "DISCONNECT REQUESTED",
            details = "BleGattClient.disconnect() was called"
        )

        Log.e(
            tag,
            "DISCONNECT CALL STACK",
            Throwable("Who called BleGattClient.disconnect()?")
        )

        val gatt = activeGatt

        stopRssiPolling()
        cancelConnectionTimeout()

        if (gatt == null) {
            Log.w(tag, "Disconnect requested, but there is no active GATT")
            activeAddress = null
            disconnectRequested = false
            _connectionState.value = BleState.Disconnected()
            return
        }

        disconnectRequested = true

        try {
            if (!BlePermissionHelper.hasRequiredPermissions(context)) {
                Log.w(tag, "Cannot request GATT disconnect: permissions unavailable")
                closeExistingGatt(reason = "Disconnect requested without required permissions")
                _connectionState.value = BleState.Disconnected(activeAddress)
                return
            }

            val address = activeAddress
            Log.w(tag, "Calling BluetoothGatt.disconnect() for $address")
            gatt.disconnect()

            disconnectTimeoutJob?.cancel()
            disconnectTimeoutJob = scope.launch {
                delay(disconnectTimeoutMs)

                if (activeGatt === gatt) {
                    Log.w(
                        tag,
                        "DISCONNECT TIMEOUT | address=$address | " +
                                "No disconnected callback received within ${disconnectTimeoutMs}ms"
                    )

                    closeGatt(gatt)
                    _connectionState.value = BleState.Disconnected(address)
                }
            }

        } catch (e: SecurityException) {
            Log.e(tag, "Permission error during disconnect", e)
            closeExistingGatt(reason = "SecurityException during disconnect")
            _connectionState.value = BleState.Disconnected(activeAddress)

        } catch (e: Exception) {
            Log.e(tag, "Disconnect failed", e)
            closeExistingGatt(reason = "Exception during disconnect")
            _connectionState.value = BleState.Disconnected(activeAddress)
        }
    }

    // ----------------------------------------------------
    // GATT CLEANUP
    // ----------------------------------------------------

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun closeGatt(gatt: BluetoothGatt) {

        val address = try {
            gatt.device.address
        } catch (_: Exception) {
            activeAddress ?: "Unknown"
        }

        Log.w(
            tag,
            "CLOSING GATT | address=$address | isActive=${gatt === activeGatt}"
        )

        try {
            gatt.close()
        } catch (e: Exception) {
            Log.e(tag, "Error closing GATT for $address", e)
        }

        if (activeGatt === gatt) {
            activeGatt = null
            activeAddress = null

            stopRssiPolling()
            cancelConnectionTimeout()
            disconnectTimeoutJob?.cancel()
            disconnectTimeoutJob = null
        }
    }
    private fun closeExistingGatt(reason: String) {
        Log.d(TAG, "Closing existing GATT. Reason: $reason")

        val gatt = activeGatt ?: return

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Cannot close GATT: BLUETOOTH_CONNECT permission denied")
            return
        }

        try {
            gatt.close()
            Log.d(TAG, "GATT closed successfully")
            activeGatt = null
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while closing GATT", e)
        }
    }
    // ----------------------------------------------------
    // CHARACTERISTIC READ
    // ----------------------------------------------------

    @SuppressLint("MissingPermission")
    fun readCharacteristic(
        serviceUuid: UUID,
        characteristicUuid: UUID
    ) {

        val gatt = activeGatt ?: return

        if (!BlePermissionHelper.hasRequiredPermissions(context)) {
            _connectionState.value =
                BleState.Error(BleError.PermissionDenied())
            return
        }

        try {
            val characteristic = gatt
                .getService(serviceUuid)
                ?.getCharacteristic(characteristicUuid)

            if (characteristic == null) {
                Log.e(tag, "Characteristic not found: $characteristicUuid")
                return
            }

            val started = gatt.readCharacteristic(characteristic)

            if (!started) {
                Log.e(tag, "Unable to start characteristic read")
            }

        } catch (e: SecurityException) {
            _connectionState.value =
                BleState.Error(BleError.PermissionDenied())
        } catch (e: Exception) {
            Log.e(tag, "Characteristic read error", e)
        }
    }

    // ----------------------------------------------------
    // CHARACTERISTIC WRITE
    // ----------------------------------------------------

    @SuppressLint("MissingPermission")
    fun writeCharacteristic(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        data: ByteArray
    ) {

        val gatt = activeGatt ?: return

        if (!BlePermissionHelper.hasRequiredPermissions(context)) {
            _connectionState.value =
                BleState.Error(BleError.PermissionDenied())
            return
        }

        try {
            val characteristic = gatt
                .getService(serviceUuid)
                ?.getCharacteristic(characteristicUuid)

            if (characteristic == null) {
                Log.e(tag, "Characteristic not found: $characteristicUuid")
                return
            }

            characteristic.value = data

            val started = gatt.writeCharacteristic(characteristic)

            if (!started) {
                Log.e(tag, "Unable to start characteristic write")
            }

        } catch (e: SecurityException) {
            _connectionState.value =
                BleState.Error(BleError.PermissionDenied())
        } catch (e: Exception) {
            Log.e(tag, "Characteristic write error", e)
        }
    }

    // ----------------------------------------------------
    // ENABLE / DISABLE NOTIFICATIONS
    // ----------------------------------------------------

    @SuppressLint("MissingPermission")
    fun enableNotifications(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        enable: Boolean
    ) {

        val gatt = activeGatt ?: return

        if (!BlePermissionHelper.hasRequiredPermissions(context)) {
            _connectionState.value =
                BleState.Error(BleError.PermissionDenied())
            return
        }

        try {
            val characteristic = gatt
                .getService(serviceUuid)
                ?.getCharacteristic(characteristicUuid)

            if (characteristic == null) {
                Log.e(tag, "Characteristic not found: $characteristicUuid")
                return
            }

            val descriptor = characteristic.getDescriptor(
                CLIENT_CONFIGURATION_UUID
            )

            if (descriptor == null) {
                Log.e(tag, "Client configuration descriptor not found")
                return
            }

            val notificationEnabled =
                gatt.setCharacteristicNotification(
                    characteristic,
                    enable
                )

            if (!notificationEnabled) {
                Log.e(tag, "Unable to configure local notifications")
                return
            }

            descriptor.value = if (enable) {
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            } else {
                BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            }

            val started = gatt.writeDescriptor(descriptor)

            if (!started) {
                Log.e(tag, "Unable to write notification descriptor")
            }

        } catch (e: SecurityException) {
            _connectionState.value =
                BleState.Error(BleError.PermissionDenied())
        } catch (e: Exception) {
            Log.e(tag, "Notification configuration error", e)
        }
    }
}
