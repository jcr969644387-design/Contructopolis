package com.educalab.civilestructuras.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.educalab.civilestructuras.data.local.converters.BooleanConverters
import com.educalab.civilestructuras.data.local.dao.*
import com.educalab.civilestructuras.data.local.entity.*

@Database(
    entities = [
        UserProfileEntity::class,
        MaterialEntity::class,
        StructureChallengeEntity::class,
        ChallengeGoalEntity::class,
        PresetSupportEntity::class,
        PresetLoadEntity::class,
        StructureDesignEntity::class,
        StructureNodeEntity::class,
        StructureMemberEntity::class,
        LoadEntity::class,
        SimulationRunEntity::class,
        MemberResultEntity::class,
        ProgressEntity::class,
        BlueprintRewardEntity::class,
        BadgeEntity::class,
        UserBadgeEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(BooleanConverters::class)
abstract class ConstructopolisDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun materialDao(): MaterialDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun designDao(): DesignDao
    abstract fun simulationDao(): SimulationDao
    abstract fun progressDao(): ProgressDao
    abstract fun blueprintDao(): BlueprintDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        const val DATABASE_NAME = "constructopolis.db"

        @Volatile private var INSTANCE: ConstructopolisDatabase? = null

        fun getInstance(context: android.content.Context): ConstructopolisDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    ConstructopolisDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
