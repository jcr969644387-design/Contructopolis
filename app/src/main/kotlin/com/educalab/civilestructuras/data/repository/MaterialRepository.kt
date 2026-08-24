package com.educalab.civilestructuras.data.repository

import com.educalab.civilestructuras.data.local.dao.MaterialDao
import com.educalab.civilestructuras.data.local.entity.MaterialEntity
import kotlinx.coroutines.flow.Flow

class MaterialRepository(private val materialDao: MaterialDao) {
    fun observeAll(): Flow<List<MaterialEntity>> = materialDao.observeAll()
    suspend fun getAll(): List<MaterialEntity> = materialDao.getAll()
}
