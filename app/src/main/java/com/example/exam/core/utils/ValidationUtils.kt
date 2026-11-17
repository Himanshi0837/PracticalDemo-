package com.example.exam.core.utils

import java.util.regex.Pattern

object ValidationUtils {

    private val MAC_ADDRESS_PATTERN =
        Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")

    private val EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")

    fun isValidMacAddress(mac: String?): Boolean {
        if (mac.isNullOrBlank()) return false
        return MAC_ADDRESS_PATTERN.matcher(mac).matches()
    }

    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        return EMAIL_PATTERN.matcher(email).matches()
    }

    fun isNonEmpty(text: String?): Boolean {
        return !text.isNullOrBlank()
    }
}

