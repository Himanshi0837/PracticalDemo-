package com.example.exam.core.bluetooth

sealed interface BleState {
    data object Idle : BleState
    data object Scanning : BleState
    data class Connecting(val deviceAddress: String) : BleState
    data class Connected(val deviceAddress: String) : BleState
    data class DiscoveringServices(val deviceAddress: String) : BleState
    data class Disconnected(val deviceAddress: String? = null) : BleState
    data class Error(val error: BleError) : BleState
}

