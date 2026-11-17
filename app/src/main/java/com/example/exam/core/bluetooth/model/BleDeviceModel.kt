package com.example.exam.core.bluetooth.model

import android.bluetooth.le.ScanResult
import com.example.exam.core.bluetooth.signal.DistanceEstimator
import com.example.exam.core.bluetooth.signal.ProximityLevel

/**
 * Clean domain representation of a discovered BLE peripheral.
 * UI never directly interacts with raw Android ScanResult.
 */
data class BleDeviceModel(
    val id: String, // Hardware MAC address
    val name: String,
    val rawRssi: Int,
    val filteredRssi: Double,
    val distanceMeters: Double,
    val proximity: ProximityLevel,
    val category: BleDeviceCategory = detectCategory(name),
    val lastSeen: Long = System.currentTimeMillis(),
    val isConnected: Boolean = false,
    val scanResult: ScanResult? = null
) {
    companion object {
        fun detectCategory(name: String): BleDeviceCategory {
            val lower = name.lowercase()
            return when {
                lower.contains("airpod") || lower.contains("earbud") || lower.contains("buds") ||
                        lower.contains("headphone") || lower.contains("audio") -> BleDeviceCategory.EARBUDS
                lower.contains("watch") -> BleDeviceCategory.WATCH
                lower.contains("band") || lower.contains("fit") || lower.contains("track") -> BleDeviceCategory.FITNESS_BAND
                lower.contains("beacon") -> BleDeviceCategory.BEACON
                lower.contains("phone") || lower.contains("pixel") || lower.contains("galaxy") ||
                        lower.contains("iphone") -> BleDeviceCategory.PHONE
                else -> BleDeviceCategory.GENERIC
            }
        }

        fun create(
            id: String,
            name: String,
            rawRssi: Int,
            smoothedRssi: Double,
            scanResult: ScanResult? = null,
            isConnected: Boolean = false,
            lastSeen: Long = System.currentTimeMillis()
        ): BleDeviceModel {
            val distance = DistanceEstimator.calculateDistance(smoothedRssi)
            val proximity = DistanceEstimator.getProximityLevel(distance)
            return BleDeviceModel(
                id = id,
                name = name.ifBlank { "Unknown Device" },
                rawRssi = rawRssi,
                filteredRssi = smoothedRssi,
                distanceMeters = distance,
                proximity = proximity,
                category = detectCategory(name),
                lastSeen = lastSeen,
                isConnected = isConnected,
                scanResult = scanResult
            )
        }
    }
}

