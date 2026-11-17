package com.example.exam.core.bluetooth.signal

enum class ProximityLevel(val label: String) {
    VERY_CLOSE("Very Close"),
    GETTING_CLOSER("Getting Closer"),
    FAR_AWAY("Far Away");

    val guidanceMessage: String
        get() = when (this) {
            VERY_CLOSE -> "Very close! You are right beside the device."
            GETTING_CLOSER -> "Getting closer! Keep moving in this direction."
            FAR_AWAY -> "Too far away. Follow the indicator."
        }
}

