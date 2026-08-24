package com.educalab.civilestructuras.data.local.dao

import androidx.room.*
import com.educalab.civilestructuras.data.local.entity.MaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Query("SELECT * FROM material ORDER BY cost ASC")
    fun observeAll(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM material ORDER BY cost ASC")
    suspend fun getAll(): List<MaterialEntity>

    @Query("SELECT COUNT(*) FROM material")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(materials: List<MaterialEntity>)
}
