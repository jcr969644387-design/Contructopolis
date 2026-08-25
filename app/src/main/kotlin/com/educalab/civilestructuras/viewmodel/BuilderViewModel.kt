package com.educalab.civilestructuras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.civilestructuras.AppContainer
import com.educalab.civilestructuras.data.repository.SimulationOutcome
import com.educalab.civilestructuras.domain.logic.BadgeId
import com.educalab.civilestructuras.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class BuilderTool { NODO, MIEMBRO, CARGA, BORRAR }

data class BuilderUiState(
    val loading: Boolean = true,
    val challenge: StructureChallengeModel? = null,
    val design: StructureDesign = StructureDesign("", emptyList(), emptyList(), emptyList()),
    val selectedTool: BuilderTool = BuilderTool.NODO,
    val selectedMaterial: MaterialType? = null,
    val selectedRole: MemberRole = MemberRole.VIGA,
    val pendingMemberStartNodeId: String? = null,
    val lastOutcome: SimulationOutcome? = null,
    val savedNotice: Boolean = false,
    val newBadges: Set<BadgeId> = emptySet(),
    val roleMismatchMessage: String? = null,
    val nextChallengeId: String? = null
)

class BuilderViewModel(private val container: AppContainer, private val challengeId: String) : ViewModel() {

    private val _uiState = MutableStateFlow(BuilderUiState())
    val uiState: StateFlow<BuilderUiState> = _uiState.asStateFlow()

    private var nodeCounter = 0
    private var memberCounter = 0
    private var loadCounter = 0
    private var pristineDesign = StructureDesign("", emptyList(), emptyList(), emptyList())

    init {
        viewModelScope.launch {
            val challenge = container.challengeRepository.getChallengeModel(challengeId)
            container.challengeRepository.markStarted(challengeId)
            if (challenge != null) {
                val design = container.designRepository.getOrCreateInitialDesign(challenge)
                nodeCounter = design.nodes.size
                memberCounter = design.members.size
                loadCounter = design.loads.size
                pristineDesign = pristineDesignFor(challenge)
                _uiState.value = _uiState.value.copy(loading = false, challenge = challenge, design = design)
            } else {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }

    fun selectTool(tool: BuilderTool) {
        _uiState.value = _uiState.value.copy(selectedTool = tool, pendingMemberStartNodeId = null)
    }

    fun selectMaterial(material: MaterialType) {
        val allowed = _uiState.value.challenge?.allowedMaterials ?: return
        if (material !in allowed) return
        _uiState.value = _uiState.value.copy(selectedMaterial = material)
    }

    fun selectRole(role: MemberRole) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
    }

    /** El niño toca una celda de la cuadrícula: el efecto depende de la herramienta activa. */
    fun onGridTap(x: Int, y: Int) {
        val state = _uiState.value
        val design = state.design
        when (state.selectedTool) {
            BuilderTool.NODO -> {
                if (design.nodes.any { it.position.x == x && it.position.y == y }) return
                val pos = NodePosition(x, y)
                if (nodeWouldSitOnExistingMember(design, pos)) {
                    _uiState.value = _uiState.value.copy(
                        roleMismatchMessage = "No puedes colocar un nodo sobre una pieza ya construida. Borra esa pieza primero o elige otra posición."
                    )
                    container.feedbackPlayer.warn()
                    return
                }
                val newNode = StructureNodeModel(id = "n${nodeCounter++}", position = pos)
                updateDesign(design.copy(nodes = design.nodes + newNode))
                container.feedbackPlayer.tap()
            }
            BuilderTool.MIEMBRO -> {
                val tappedNode = design.nodes.firstOrNull { it.position.x == x && it.position.y == y } ?: return
                val pendingId = state.pendingMemberStartNodeId
                if (pendingId == null) {
                    _uiState.value = state.copy(pendingMemberStartNodeId = tappedNode.id)
                    container.feedbackPlayer.tap()
                } else if (pendingId != tappedNode.id) {
                    val startNode = design.nodeById(pendingId)
                    val material = state.selectedMaterial
                    val rejectionMessage = when {
                        startNode == null -> orientationHint(state.selectedRole)
                        material == null -> "Elige un material (madera, acero o concreto) antes de construir una pieza."
                        !isValidOrientation(startNode.position, tappedNode.position, state.selectedRole) -> orientationHint(state.selectedRole)
                        hasIntermediateNode(design, startNode.position, tappedNode.position, setOf(pendingId, tappedNode.id)) ->
                            "Hay un nodo en medio: conecta primero con ese nodo, no puedes saltarlo."
                        else -> null
                    }
                    if (rejectionMessage != null) {
                        _uiState.value = _uiState.value.copy(pendingMemberStartNodeId = null, roleMismatchMessage = rejectionMessage)
                        container.feedbackPlayer.warn()
                    } else if (startNode != null && material != null) {
                        val newMember = StructureMemberModel(
                            id = "m${memberCounter++}", nodeAId = pendingId, nodeBId = tappedNode.id,
                            material = material, role = state.selectedRole
                        )
                        updateDesign(design.copy(members = design.members + newMember))
                        _uiState.value = _uiState.value.copy(pendingMemberStartNodeId = null)
                        container.feedbackPlayer.confirm()
                    }
                }
            }
            BuilderTool.CARGA -> {
                val tappedNode = design.nodes.firstOrNull { it.position.x == x && it.position.y == y } ?: return
                val newLoad = LoadModel(id = "l${loadCounter++}", nodeId = tappedNode.id, magnitude = DEFAULT_LOAD_MAGNITUDE)
                updateDesign(design.copy(loads = design.loads + newLoad))
                container.feedbackPlayer.tap()
            }
            BuilderTool.BORRAR -> {
                val nodeAtCell = design.nodes.firstOrNull { it.position.x == x && it.position.y == y }
                if (nodeAtCell != null && nodeAtCell.support == SupportType.NINGUNO) {
                    updateDesign(
                        design.copy(
                            nodes = design.nodes.filterNot { it.id == nodeAtCell.id },
                            members = design.members.filterNot { it.nodeAId == nodeAtCell.id || it.nodeBId == nodeAtCell.id },
                            loads = design.loads.filterNot { it.nodeId == nodeAtCell.id }
                        )
                    )
                    container.feedbackPlayer.tap()
                }
            }
        }
    }

    /** Borra todas las piezas, nodos libres y cargas añadidas, dejando solo lo que trae el reto de origen. */
    fun clearAll() {
        _uiState.value = _uiState.value.copy(
            design = pristineDesign,
            pendingMemberStartNodeId = null,
            lastOutcome = null
        )
        container.feedbackPlayer.warn()
    }

    fun dismissRoleMismatch() {
        _uiState.value = _uiState.value.copy(roleMismatchMessage = null)
    }

    fun removeMember(memberId: String) {
        val design = _uiState.value.design
        updateDesign(design.copy(members = design.members.filterNot { it.id == memberId }))
    }

    fun removeLoad(loadId: String) {
        val design = _uiState.value.design
        updateDesign(design.copy(loads = design.loads.filterNot { it.id == loadId }))
    }

    fun save() {
        viewModelScope.launch {
            container.designRepository.saveDesign(_uiState.value.design)
            _uiState.value = _uiState.value.copy(savedNotice = true)
            container.feedbackPlayer.confirm()
        }
    }

    fun dismissSavedNotice() {
        _uiState.value = _uiState.value.copy(savedNotice = false)
    }

    fun dismissNewBadges() {
        _uiState.value = _uiState.value.copy(newBadges = emptySet())
    }

    fun simulate() {
        val challenge = _uiState.value.challenge ?: return
        viewModelScope.launch {
            val outcome = container.simulationRepository.runSimulation(challenge, _uiState.value.design)
            val nextId = if (outcome.result.passed) container.challengeRepository.getNextChallengeId(challenge.id) else null
            _uiState.value = _uiState.value.copy(lastOutcome = outcome, newBadges = outcome.newlyUnlockedBadges, nextChallengeId = nextId)
            if (outcome.result.passed) {
                container.feedbackPlayer.success()
            } else {
                container.feedbackPlayer.failure()
            }
        }
    }

    private fun updateDesign(newDesign: StructureDesign) {
        _uiState.value = _uiState.value.copy(design = newDesign, lastOutcome = null, nextChallengeId = null)
    }

    companion object {
        private const val DEFAULT_LOAD_MAGNITUDE = 20

        /** Misma regla usada por [MemberRole] al dibujar: viga=horizontal, columna=vertical, diagonal=cruzada. */
        fun isValidOrientation(a: NodePosition, b: NodePosition, role: MemberRole): Boolean {
            val dx = b.x - a.x
            val dy = b.y - a.y
            return when (role) {
                MemberRole.VIGA -> dy == 0 && dx != 0
                MemberRole.COLUMNA -> dx == 0 && dy != 0
                MemberRole.DIAGONAL -> dx != 0 && dy != 0
            }
        }

        /** true si [p] cae exactamente sobre el segmento recto entre [a] y [b], sin contar los extremos. */
        fun isPointStrictlyOnSegment(a: NodePosition, b: NodePosition, p: NodePosition): Boolean {
            val dx = b.x - a.x
            val dy = b.y - a.y
            val lenSq = dx * dx + dy * dy
            if (lenSq == 0) return false
            val px = p.x - a.x
            val py = p.y - a.y
            val cross = dx * py - dy * px
            if (cross != 0) return false
            val dot = px * dx + py * dy
            return dot in 1 until lenSq
        }

        /**
         * true si algún otro nodo del diseño cae exactamente sobre el segmento recto entre
         * [a] y [b] (sin contar los propios extremos, excluidos por id en [excludeIds]). Evita
         * que una pieza "salte" por encima de un nodo intermedio en vez de conectarse a él.
         */
        fun hasIntermediateNode(design: StructureDesign, a: NodePosition, b: NodePosition, excludeIds: Set<String>): Boolean =
            design.nodes.any { node -> node.id !in excludeIds && isPointStrictlyOnSegment(a, b, node.position) }

        /**
         * true si [pos] cae exactamente sobre el tramo recto de alguna pieza ya construida. Evita
         * el caso inverso al anterior: agregar un nodo nuevo "escondido" bajo una pieza existente
         * en vez de partirla en dos tramos reales.
         */
        fun nodeWouldSitOnExistingMember(design: StructureDesign, pos: NodePosition): Boolean =
            design.members.any { m ->
                val a = design.nodeById(m.nodeAId)?.position
                val b = design.nodeById(m.nodeBId)?.position
                a != null && b != null && isPointStrictlyOnSegment(a, b, pos)
            }

        fun orientationHint(role: MemberRole): String = when (role) {
            MemberRole.VIGA -> "Una viga debe ser horizontal: conecta dos nodos a la misma altura."
            MemberRole.COLUMNA -> "Una columna debe ser vertical: conecta dos nodos en la misma columna."
            MemberRole.DIAGONAL -> "Una diagonal debe ir en ángulo: conecta nodos que no compartan fila ni columna."
        }

        /** Reconstruye, sin tocar la base de datos, el diseño de partida de un reto (apoyos fijos + cargas pre-colocadas). */
        fun pristineDesignFor(challenge: StructureChallengeModel): StructureDesign {
            val supportNodes = challenge.fixedSupports.mapIndexed { index, preset ->
                StructureNodeModel(id = "S$index", position = preset.position, support = preset.support)
            }
            val extraNodes = mutableListOf<StructureNodeModel>()
            val loads = challenge.presetLoads.mapIndexed { index, preset ->
                val hostId = (supportNodes + extraNodes).firstOrNull { it.position == preset.position }?.id
                    ?: "L${extraNodes.size}".also { key ->
                        extraNodes += StructureNodeModel(id = key, position = preset.position, support = SupportType.NINGUNO)
                    }
                LoadModel(id = "PL$index", nodeId = hostId, magnitude = preset.magnitude, isLateral = preset.isLateral)
            }
            return StructureDesign(
                challengeId = challenge.id,
                nodes = supportNodes + extraNodes,
                members = emptyList(),
                loads = loads
            )
        }
    }
}
