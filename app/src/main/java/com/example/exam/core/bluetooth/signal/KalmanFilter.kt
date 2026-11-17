package com.example.exam.core.bluetooth.signal

/**
 * 1D Kalman Filter for smoothing noisy RSSI values received from BLE advertisements and GATT.
 *
 * @param processNoise Process noise covariance (Q)
 * @param measurementNoise Measurement noise covariance (R)
 */
class KalmanFilter(
    private val processNoise: Double = 0.05,
    private val measurementNoise: Double = 1.5
) {
    private var estimate: Double? = null
    private var errorCovariance: Double = 1.0

    /**
     * Filters the raw RSSI measurement.
     * @param measurement Raw RSSI value in dBm
     * @return Smoothed RSSI estimate
     */
    fun filter(measurement: Double): Double {
        val currentEstimate = estimate
        if (currentEstimate == null) {
            estimate = measurement
            errorCovariance = 1.0
            return measurement
        }

        // 1. Predict
        val predictedEstimate = currentEstimate
        val predictedError = errorCovariance + processNoise

        // 2. Update (Kalman Gain)
        val kalmanGain = predictedError / (predictedError + measurementNoise)
        val newEstimate = predictedEstimate + kalmanGain * (measurement - predictedEstimate)
        val newError = (1.0 - kalmanGain) * predictedError

        estimate = newEstimate
        errorCovariance = newError

        return newEstimate
    }

    /**
     * Resets the filter state.
     */
    fun reset() {
        estimate = null
        errorCovariance = 1.0
    }
}

