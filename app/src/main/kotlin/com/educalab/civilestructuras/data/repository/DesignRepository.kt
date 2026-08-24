package com.educalab.civilestructuras.data.repository

import com.educalab.civilestructuras.data.local.dao.DesignDao
import com.educalab.civilestructuras.data.local.dao.DesignWithDetails
import com.educalab.civilestructuras.data.local.entity.LoadEntity
import com.educalab.civilestructuras.data.local.entity.StructureDesignEntity
import com.educalab.civilestructuras.data.local.entity.StructureMemberEntity
import com.educalab.civilestructuras.data.local.entity.StructureNodeEntity
import com.educalab.civilestructuras.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DesignRepository(private val designDao: DesignDao) {

    /** Observa el diseño guardado de un reto, ya convertido a modelo de dominio (null si aún no existe). */
    fun observeDesign(challengeId: String): Flow<StructureDesign?> =
        designDao.observeForChallenge(challengeId).map { it?.toDomainModel(challengeId) }

    /** Crea (si no existe) el diseño inicial de un reto a partir de sus apoyos pre-colocados. */
    suspend fun getOrCreateInitialDesign(challenge: StructureChallengeModel): StructureDesign {
        val existingDetails = designDao.getWithDetailsOnce(challenge.id)
        if (existingDetails != null) {
            return existingDetails.toDomainModel(challenge.id)
        }
        val now = System.currentTimeMillis()
        val designId = designDao.upsertDesign(StructureDesignEntity(challengeId = challenge.id, createdAt = now, updatedAt = now))
        val nodes = challenge.fixedSupports.mapIndexed { index, preset ->
            StructureNodeEntity(designId = designId, nodeKey = "S$index", x = preset.position.x, y = preset.position.y, supportType = preset.support.name)
        }
        val loads = challenge.presetLoads.mapIndexed { index, preset ->
            // Las cargas pre-colocadas necesitan un nodo anfitrión; se coloca en la misma posición si coincide con un apoyo,
            // o se añade como nodo libre adicional que el niño deberá conectar.
            "PL$index" to preset
        }
        val extraNodes = mutableListOf<StructureNodeEntity>()
        val loadEntities = mutableListOf<LoadEntity>()
        loads.forEach { (key, preset) ->
            val hostNode = nodes.firstOrNull { it.x == preset.position.x && it.y == preset.position.y }
            val nodeKey = hostNode?.nodeKey ?: run {
                val newKey = "L${extraNodes.size}"
                extraNodes += StructureNodeEntity(designId = designId, nodeKey = newKey, x = preset.position.x, y = preset.position.y, supportType = SupportType.NINGUNO.name)
                newKey
            }
            loadEntities += LoadEntity(designId = designId, loadKey = key, nodeKey = nodeKey, magnitude = preset.magnitude, isLateral = preset.isLateral)
        }
        designDao.replaceDesignContent(designId, nodes + extraNodes, emptyList(), loadEntities)
        return StructureDesign(
            challengeId = challenge.id,
            nodes = (nodes + extraNodes).map { StructureNodeModel(it.nodeKey, NodePosition(it.x, it.y), SupportType.valueOf(it.supportType)) },
            members = emptyList(),
            loads = loadEntities.map { LoadModel(it.loadKey, it.nodeKey, it.magnitude, it.isLateral) }
        )
    }

    /** Reemplaza atómicamente el contenido del diseño (llamada por "Guardar"). */
    suspend fun saveDesign(design: StructureDesign) {
        val designRow = designDao.getForChallenge(design.challengeId)
            ?: StructureDesignEntity(challengeId = design.challengeId, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        val designId = designDao.upsertDesign(designRow.copy(updatedAt = System.currentTimeMillis()))
        designDao.replaceDesignContent(
            designId = designId,
            nodes = design.nodes.map { StructureNodeEntity(designId = designId, nodeKey = it.id, x = it.position.x, y = it.position.y, supportType = it.support.name) },
            members = design.members.map { StructureMemberEntity(designId = designId, memberKey = it.id, nodeAKey = it.nodeAId, nodeBKey = it.nodeBId, material = it.material.name, role = it.role.name) },
            loads = design.loads.map { LoadEntity(designId = designId, loadKey = it.id, nodeKey = it.nodeId, magnitude = it.magnitude, isLateral = it.isLateral) }
        )
    }

    /** Id numérico interno (Room) del diseño guardado de un reto, o null si aún no existe. */
    suspend fun getDesignEntityId(challengeId: String): Long? = designDao.getForChallenge(challengeId)?.id

    /** Materiales distintos usados en el diseño guardado de un reto (para insignias por material). */
    suspend fun getDistinctMaterials(designId: Long): List<MaterialType> =
        designDao.distinctMaterialsForDesign(designId).map { MaterialType.valueOf(it) }
}

fun DesignWithDetails.toDomainModel(challengeId: String): StructureDesign = StructureDesign(
    challengeId = challengeId,
    nodes = nodes.map { StructureNodeModel(it.nodeKey, NodePosition(it.x, it.y), SupportType.valueOf(it.supportType)) },
    members = members.map { StructureMemberModel(it.memberKey, it.nodeAKey, it.nodeBKey, MaterialType.valueOf(it.material), MemberRole.valueOf(it.role)) },
    loads = loads.map { LoadModel(it.loadKey, it.nodeKey, it.magnitude, it.isLateral) }
)
