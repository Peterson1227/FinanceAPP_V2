package com.financeapp.nativeapp.presentation.security

import org.junit.Assert.*
import org.junit.Test

class UnlockSessionTest {
    @Test fun startsLockedAndRejectsUnsolicitedSuccess() {
        val session = UnlockSession()
        session.succeed()
        assertFalse(session.unlocked)
    }
    @Test fun successfulPendingAuthenticationUnlocks() {
        val session = UnlockSession()
        assertTrue(session.begin())
        session.succeed()
        assertTrue(session.unlocked)
        assertFalse(session.pending)
    }
    @Test fun cancellationAndErrorsKeepLocked() {
        val session = UnlockSession()
        session.begin()
        session.finish()
        session.succeed()
        assertFalse(session.unlocked)
        assertTrue(session.begin())
    }
    @Test fun failedRecognitionDoesNotGrantAccessAndCanRetry() {
        val session = UnlockSession()
        session.begin()
        // A nonterminal recognition failure makes no successful transition.
        assertFalse(session.unlocked)
        assertTrue(session.pending)
        assertFalse(session.begin())
        session.succeed()
        assertTrue(session.unlocked)
    }
    @Test fun backgroundLocksAndRejectsLateSuccess() {
        val session = UnlockSession()
        session.begin()
        session.lock()
        session.succeed()
        assertFalse(session.unlocked)
        session.begin()
        session.succeed()
        session.lock()
        assertFalse(session.unlocked)
    }
    @Test fun recreationDoesNotRestoreUnlockedState() {
        val previous = UnlockSession()
        previous.begin()
        previous.succeed()
        assertFalse(UnlockSession().unlocked)
    }
}
