package com.example.exam.core.common

object Constants {
    // Network Constants
    const val DEFAULT_BASE_URL = "https://jsonplaceholder.typicode.com/"
    const val NETWORK_TIMEOUT_SECONDS = 30L

    // BLE Constants
    const val BLE_SCAN_TIMEOUT_MS = 10000L
    const val BLE_OUT_OF_RANGE_CHECK_INTERVAL_MS = 5000L
    const val BLE_GATT_RSSI_POLL_INTERVAL_MS = 500L
    const val BLE_DEFAULT_TX_POWER = -59.0
    const val BLE_PATH_LOSS_EXPONENT = 2.2
    const val BLE_RSSI_WINDOW_CAPACITY = 10
}

