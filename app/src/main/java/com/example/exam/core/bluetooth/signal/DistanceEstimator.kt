package com.example.exam.core.bluetooth.signal

import com.example.exam.core.common.Constants
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Calculates estimated distance and proximity levels based on filtered RSSI values
 * using the Log-Distance Path Loss Model: d = 10 ^ ((TxPower - RSSI) / (10 * n))
 */
object DistanceEstimator {

    fun calculateDistance(
        rssi: Double,
        txPower: Double = Constants.BLE_DEFAULT_TX_POWER,
        pathLoss: Double = Constants.BLE_PATH_LOSS_EXPONENT
    ): Double {
        if (rssi >= 0) return 0.1
        val ratio = (txPower - rssi) / (10.0 * pathLoss)
        val distance = 10.0.pow(ratio)
        val clamped = distance.coerceIn(0.1, 30.0)
        return (clamped * 10.0).roundToInt() / 10.0
    }

    fun getProximityLevel(distanceMeters: Double): ProximityLevel {
        return when {
            distanceMeters < 2.0 -> ProximityLevel.VERY_CLOSE
            distanceMeters <= 5.0 -> ProximityLevel.GETTING_CLOSER
            else -> ProximityLevel.FAR_AWAY
        }
    }

    fun calculateGaugeFraction(distanceMeters: Double): Float {
        val fraction = ((distanceMeters - 0.5) / (8.0 - 0.5)).toFloat()
        return fraction.coerceIn(0f, 1f)
    }
}

