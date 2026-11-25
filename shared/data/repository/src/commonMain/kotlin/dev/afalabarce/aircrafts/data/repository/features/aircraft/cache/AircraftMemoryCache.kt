package dev.afalabarce.aircrafts.data.repository.features.aircraft.cache

import dev.afalabarce.aircrafts.domain.models.AircraftCategory
import dev.afalabarce.aircrafts.domain.models.AircraftModelDetail
import dev.afalabarce.aircrafts.domain.models.AircraftSearchResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

data class SearchCacheKey(
    val query: String,
    val category: AircraftCategory?,
    val cursor: String?,
    val limit: Int
)

private data class CacheEntry<T>(
    val value: T,
    val expiresAtMs: Long
)

/**
 * Thread-safe in-memory TTL cache for aircraft search results and technical detail sheets.
 *
 * Concurrency & Threading decisions:
 * - Uses [Mutex] instead of JVM `synchronized` because this code lives in KMP `commonMain`
 *   (Android + iOS) and `Mutex.withLock` suspends without blocking carrier threads.
 * - Bounds map growth via expired-entry pruning and FIFO eviction when [maxEntries] is reached.
 *
 * @param ttlMs Time-to-live in milliseconds (default 3 minutes).
 * @param maxEntries Maximum entries per cache partition before pruning/eviction.
 * @param clockMs Monotonic/epoch time provider for deterministic testing.
 */
class AircraftMemoryCache(
    private val ttlMs: Long = DEFAULT_TTL_MS,
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
    private val clockMs: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {
    private val mutex = Mutex()
    private val searchEntries = LinkedHashMap<SearchCacheKey, CacheEntry<AircraftSearchResult>>()
    private val detailEntries = LinkedHashMap<String, CacheEntry<AircraftModelDetail>>()

    suspend fun getSearch(key: SearchCacheKey): AircraftSearchResult? = mutex.withLock {
        getValidEntry(searchEntries, key)
    }

    suspend fun putSearch(key: SearchCacheKey, value: AircraftSearchResult) = mutex.withLock {
        putBoundedEntry(searchEntries, key, value)
    }

    suspend fun getDetail(icaoTypeCode: String): AircraftModelDetail? = mutex.withLock {
        getValidEntry(detailEntries, icaoTypeCode.uppercase())
    }

    suspend fun putDetail(icaoTypeCode: String, value: AircraftModelDetail) = mutex.withLock {
        putBoundedEntry(detailEntries, icaoTypeCode.uppercase(), value)
    }

    suspend fun clear() = mutex.withLock {
        searchEntries.clear()
        detailEntries.clear()
    }

    private fun <K, V> getValidEntry(
        entries: MutableMap<K, CacheEntry<V>>,
        key: K
    ): V? {
        val entry = entries[key] ?: return null
        if (clockMs() >= entry.expiresAtMs) {
            entries.remove(key)
            return null
        }
        return entry.value
    }

    private fun <K, V> putBoundedEntry(
        entries: MutableMap<K, CacheEntry<V>>,
        key: K,
        value: V
    ) {
        val now = clockMs()
        if (entries.size >= maxEntries && key !in entries) {
            val iterator = entries.entries.iterator()
            while (iterator.hasNext()) {
                if (now >= iterator.next().value.expiresAtMs) iterator.remove()
            }
            if (entries.size >= maxEntries) {
                entries.keys.firstOrNull()?.let(entries::remove)
            }
        }
        entries[key] = CacheEntry(value = value, expiresAtMs = now + ttlMs)
    }

    companion object {
        const val DEFAULT_TTL_MS: Long = 3 * 60 * 1000L // 3 minutes
        const val DEFAULT_MAX_ENTRIES: Int = 100
    }
}
