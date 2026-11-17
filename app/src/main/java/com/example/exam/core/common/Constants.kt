package com.example.exam.core.common

object Constants {
    // Network & TMDB Constants
    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
    const val TMDB_IMAGE_BASE_W500 = "https://image.tmdb.org/t/p/w500"
    const val TMDB_IMAGE_BASE_ORIGINAL = "https://image.tmdb.org/t/p/original"
    const val TMDB_API_KEY = "0dbfa8407d871c1fd3472b59c99ae533"
    const val TMDB_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIwZGJmYTg0MDdkODcxYzFmZDM0NzJiNTljOTlhZTUzMyIsIm5iZiI6MTc5NDg5NzU5MS42NzIsInN1YiI6IjZhZmJmNmI3MDFhYTE4ZjIzZWI2ODY3NCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.DLqsL154HHBaeZR8F140yBIz3dwcLU86TlqyHpaMpIc"

    // BLE Constants
    const val BLE_SCAN_TIMEOUT_MS = 10000L
    const val BLE_OUT_OF_RANGE_CHECK_INTERVAL_MS = 5000L
    const val BLE_GATT_RSSI_POLL_INTERVAL_MS = 500L
    const val BLE_DEFAULT_TX_POWER = -59.0
    const val BLE_PATH_LOSS_EXPONENT = 2.2
    const val BLE_RSSI_WINDOW_CAPACITY = 10
}

