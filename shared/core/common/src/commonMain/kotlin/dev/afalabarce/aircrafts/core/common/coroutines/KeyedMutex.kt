package dev.afalabarce.aircrafts.core.common.coroutines

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Provides per-key mutual exclusion for suspending operations in KMP `commonMain`.
 *
 * Threading & Cancellation design:
 * - Operations with **distinct** keys acquire separate [Mutex] instances and execute concurrently.
 * - Operations with the **same** key serialize on a shared [Mutex], preventing duplicate network
 *   or database work (single-flight) while keeping waiting coroutines cooperatively cancellable.
 * - Reference-counted entries are cleaned up inside `withContext(NonCancellable)` in `finally` so
 *   cancelled waiters never leak map entries or leave stale locks behind.
 */
class KeyedMutex<K> {
    private class Entry(
        val mutex: Mutex = Mutex(),
        var activeCount: Int = 0
    )

    private val guard = Mutex()
    private val entries = HashMap<K, Entry>()

    suspend fun <T> withLock(key: K, action: suspend () -> T): T {
        val entry = guard.withLock {
            entries.getOrPut(key) { Entry() }.also { it.activeCount++ }
        }
        return try {
            entry.mutex.withLock {
                action()
            }
        } finally {
            withContext(NonCancellable) {
                guard.withLock {
                    entry.activeCount--
                    if (entry.activeCount <= 0) {
                        entries.remove(key)
                    }
                }
            }
        }
    }
}
