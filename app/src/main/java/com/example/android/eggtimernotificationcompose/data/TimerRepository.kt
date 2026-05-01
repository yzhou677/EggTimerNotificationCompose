package com.example.android.eggtimernotificationcompose.data

import com.example.android.eggtimernotificationcompose.model.TimerEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TimerRepository(
    private val dao: TimerDao
) {

    private val persistenceMutex = Mutex()

    /**
     * Serializes timer row writes across ViewModel and BroadcastReceivers so
     * save/update/cancel cannot reorder and resurrect stale SCHEDULED rows.
     */
    suspend fun <T> withPersistenceLock(block: suspend () -> T): T =
        persistenceMutex.withLock { block() }

    suspend fun getAll(): List<TimerEntity> {
        return dao.getAll()
    }

    suspend fun getById(id: String): TimerEntity? {
        return dao.getById(id)
    }

    suspend fun update(timer: TimerEntity) {
        dao.insert(timer)
    }

    suspend fun save(timer: TimerEntity) {
        dao.insert(timer)
    }

    suspend fun delete(id: String) {
        dao.delete(id)
    }

    fun observeTimers() = dao.observeTimers()
}