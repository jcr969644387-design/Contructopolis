package com.educalab.civilestructuras.data.repository

import com.educalab.civilestructuras.data.local.dao.BlueprintDao
import com.educalab.civilestructuras.data.local.entity.BlueprintRewardEntity
import kotlinx.coroutines.flow.Flow

class BlueprintRepository(private val blueprintDao: BlueprintDao) {
    fun observeAll(): Flow<List<BlueprintRewardEntity>> = blueprintDao.observeAll()
}
