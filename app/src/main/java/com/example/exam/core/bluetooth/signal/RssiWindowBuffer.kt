package com.example.exam.core.bluetooth.signal

import com.example.exam.core.common.Constants
import java.util.ArrayDeque

/**
 * Window Buffer (Size = 10) FIFO queue to compute a stable moving average.
 * Maintains up to 10 latest filtered values and computes the arithmetic mean.
 */
class RssiWindowBuffer(private val capacity: Int = Constants.BLE_RSSI_WINDOW_CAPACITY) {
    private val buffer = ArrayDeque<Double>(capacity)

    @Synchronized
    fun addAndAverage(value: Double): Double {
        if (buffer.size >= capacity) {
            buffer.removeFirst()
        }
        buffer.addLast(value)
        return average()
    }

    @Synchronized
    fun average(): Double {
        if (buffer.isEmpty()) return 0.0
        val sum = buffer.sum()
        return sum / buffer.size
    }

    @Synchronized
    fun getSnapshot(): List<Double> = buffer.toList()

    @Synchronized
    fun clear() {
        buffer.clear()
    }
}

