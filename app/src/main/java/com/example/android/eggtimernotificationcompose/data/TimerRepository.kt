package com.example.android.eggtimernotificationcompose.data

import com.example.android.eggtimernotificationcompose.model.TimerEntity

class TimerRepository(
    private val dao: TimerDao
) {

    suspend fun getAll(): List<TimerEntity> {
        return dao.getAll()
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