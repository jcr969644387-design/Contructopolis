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
    val selectedMaterial: MaterialType = MaterialType.MADERA,
    val selectedRole: MemberRole = MemberRole.VIGA,
    val pendingMemberStartNodeId: String? = null,
    val lastOutcome: SimulationOutcome? = null,
    val savedNotice: Boolean = false,
    val newBadges: Set<BadgeId> = emptySet()
)

class BuilderViewModel(private val container: AppContainer, private val challengeId: String) : ViewModel() {

    private val _uiState = MutableStateFlow(BuilderUiState())
    val uiState: StateFlow<BuilderUiState> = _uiState.asStateFlow()

    private var nodeCounter = 0
    private var memberCounter = 0
    private var loadCounter = 0

    init {
        viewModelScope.launch {
            val challenge = container.challengeRepository.getChallengeModel(challengeId)
            container.challengeRepository.markStarted(challengeId)
            if (challenge != null) {
                val design = container.designRepository.getOrCreateInitialDesign(challenge)
                nodeCounter = design.nodes.size
                memberCounter = design.members.size
                loadCounter = design.loads.size
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
                val newNode = StructureNodeModel(id = "n${nodeCounter++}", position = NodePosition(x, y))
                updateDesign(design.copy(nodes = design.nodes + newNode))
            }
            BuilderTool.MIEMBRO -> {
                val tappedNode = design.nodes.firstOrNull { it.position.x == x && it.position.y == y } ?: return
                val pendingId = state.pendingMemberStartNodeId
                if (pendingId == null) {
                    _uiState.value = state.copy(pendingMemberStartNodeId = tappedNode.id)
                } else if (pendingId != tappedNode.id) {
                    val newMember = StructureMemberModel(
                        id = "m${memberCounter++}", nodeAId = pendingId, nodeBId = tappedNode.id,
                        material = state.selectedMaterial, role = state.selectedRole
                    )
                    updateDesign(design.copy(members = design.members + newMember))
                    _uiState.value = _uiState.value.copy(pendingMemberStartNodeId = null)
                }
            }
            BuilderTool.CARGA -> {
                val tappedNode = design.nodes.firstOrNull { it.position.x == x && it.position.y == y } ?: return
                val newLoad = LoadModel(id = "l${loadCounter++}", nodeId = tappedNode.id, magnitude = DEFAULT_LOAD_MAGNITUDE)
                updateDesign(design.copy(loads = design.loads + newLoad))
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
                }
            }
        }
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
            _uiState.value = _uiState.value.copy(lastOutcome = outcome, newBadges = outcome.newlyUnlockedBadges)
        }
    }

    private fun updateDesign(newDesign: StructureDesign) {
        _uiState.value = _uiState.value.copy(design = newDesign, lastOutcome = null)
    }

    companion object {
        private const val DEFAULT_LOAD_MAGNITUDE = 20
    }
}
