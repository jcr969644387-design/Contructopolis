package com.educalab.civilestructuras.data.local.dao

import androidx.room.*
import com.educalab.civilestructuras.data.local.entity.*
import kotlinx.coroutines.flow.Flow

/** Diseño completo con sus nodos, miembros y cargas (lo que edita el Constructor). */
data class DesignWithDetails(
    @Embedded val design: StructureDesignEntity,
    @Relation(parentColumn = "id", entityColumn = "designId") val nodes: List<StructureNodeEntity>,
    @Relation(parentColumn = "id", entityColumn = "designId") val members: List<StructureMemberEntity>,
    @Relation(parentColumn = "id", entityColumn = "designId") val loads: List<LoadEntity>
)

@Dao
interface DesignDao {
    @Query("SELECT * FROM structure_design WHERE challengeId = :challengeId LIMIT 1")
    suspend fun getForChallenge(challengeId: String): StructureDesignEntity?

    @Transaction
    @Query("SELECT * FROM structure_design WHERE challengeId = :challengeId LIMIT 1")
    suspend fun getWithDetailsOnce(challengeId: String): DesignWithDetails?

    @Transaction
    @Query("SELECT * FROM structure_design WHERE challengeId = :challengeId LIMIT 1")
    fun observeForChallenge(challengeId: String): Flow<DesignWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDesign(design: StructureDesignEntity): Long

    @Query("DELETE FROM structure_design WHERE id = :designId")
    suspend fun deleteDesign(designId: Long)

    @Query("DELETE FROM structure_node WHERE designId = :designId")
    suspend fun clearNodes(designId: Long)

    @Query("DELETE FROM structure_member WHERE designId = :designId")
    suspend fun clearMembers(designId: Long)

    @Query("DELETE FROM load WHERE designId = :designId")
    suspend fun clearLoads(designId: Long)

    @Insert
    suspend fun insertNodes(nodes: List<StructureNodeEntity>)

    @Insert
    suspend fun insertMembers(members: List<StructureMemberEntity>)

    @Insert
    suspend fun insertLoads(loads: List<LoadEntity>)

    @Query("SELECT DISTINCT material FROM structure_member WHERE designId = :designId")
    suspend fun distinctMaterialsForDesign(designId: Long): List<String>

    /**
     * Reemplaza atómicamente el contenido completo de un diseño (guardado
     * desde el Constructor). @Transaction asegura que nunca queda a medias.
     */
    @Transaction
    suspend fun replaceDesignContent(
        designId: Long,
        nodes: List<StructureNodeEntity>,
        members: List<StructureMemberEntity>,
        loads: List<LoadEntity>
    ) {
        clearNodes(designId); clearMembers(designId); clearLoads(designId)
        if (nodes.isNotEmpty()) insertNodes(nodes)
        if (members.isNotEmpty()) insertMembers(members)
        if (loads.isNotEmpty()) insertLoads(loads)
    }
}
