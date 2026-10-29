package com.financeapp.nativeapp.presentation.security

/** No saved state: Activity and process recreation always start locked. */
class UnlockSession {
    var unlocked = false
        private set
    var pending = false
        private set
    fun begin(): Boolean {
        if (pending || unlocked) return false
        pending = true
        return true
    }
    fun succeed() {
        if (pending) unlocked = true
        pending = false
    }
    fun finish() { pending = false }
    fun lock() {
        unlocked = false
        pending = false
    }
}
