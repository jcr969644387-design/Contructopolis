package com.educalab.civilestructuras.ui.screens.builder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educalab.civilestructuras.AppContainer
import com.educalab.civilestructuras.domain.model.*
import com.educalab.civilestructuras.ui.components.DemandStateChip
import com.educalab.civilestructuras.ui.components.StarRow
import com.educalab.civilestructuras.ui.theme.ConstructoColors
import com.educalab.civilestructuras.viewmodel.BuilderTool
import com.educalab.civilestructuras.viewmodel.BuilderViewModel
import com.educalab.civilestructuras.viewmodel.GenericViewModelFactory
import kotlin.math.roundToInt

@Composable
fun BuilderScreen(container: AppContainer, challengeId: String, onBack: () -> Unit) {
    val viewModel: BuilderViewModel = viewModel(
        factory = GenericViewModelFactory({ BuilderViewModel(it, challengeId) }, container),
        key = "builder_$challengeId"
    )
    val state by viewModel.uiState.collectAsState()

    if (state.loading || state.challenge == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val challenge = state.challenge!!
    var showBriefing by rememberSaveable(challengeId) { mutableStateOf(true) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().navigationBarsPadding()) {
        BuilderTopBar(
            title = challenge.title,
            budget = challenge.maxBudget,
            onBack = onBack,
            onShowBriefing = { showBriefing = true }
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(ConstructoColors.BlueprintNavy)
        ) {
            BuilderGridCanvas(
                design = state.design,
                gridWidth = challenge.gridWidth,
                gridHeight = challenge.gridHeight,
                demandByMember = state.lastOutcome?.result?.memberResults?.associateBy { it.memberId }.orEmpty(),
                pendingStartNodeId = state.pendingMemberStartNodeId,
                onCellTap = viewModel::onGridTap
            )
            state.roleMismatchMessage?.let { message ->
                RoleMismatchBanner(message = message, modifier = Modifier.align(Alignment.TopCenter).padding(12.dp))
                LaunchedEffect(message) {
                    kotlinx.coroutines.delay(2200)
                    viewModel.dismissRoleMismatch()
                }
            }
        }
        BuilderToolbar(
            selectedTool = state.selectedTool,
            selectedMaterial = state.selectedMaterial,
            selectedRole = state.selectedRole,
            allowedMaterials = challenge.allowedMaterials,
            onSelectTool = viewModel::selectTool,
            onSelectMaterial = viewModel::selectMaterial,
            onSelectRole = viewModel::selectRole,
            onSave = viewModel::save,
            onSimulate = viewModel::simulate,
            onClearAll = { showClearAllConfirm = true }
        )
        state.lastOutcome?.let { outcome ->
            SimulationResultPanel(result = outcome.result, challenge = challenge)
        }
    }

    if (showBriefing) {
        ChallengeBriefingDialog(challenge = challenge, onDismiss = { showBriefing = false })
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            icon = { Icon(Icons.Filled.DeleteSweep, contentDescription = null, tint = ConstructoColors.DangerRed) },
            title = { Text("¿Borrar toda la construcción?") },
            text = { Text("Se eliminarán todos los nodos, piezas y cargas que agregaste. Los apoyos y cargas propios del reto no se pierden.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAll(); showClearAllConfirm = false }) { Text("Borrar todo") }
            },
            dismissButton = { TextButton(onClick = { showClearAllConfirm = false }) { Text("Cancelar") } }
        )
    }

    if (state.savedNotice) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1200)
            viewModel.dismissSavedNotice()
        }
    }
    if (state.newBadges.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = viewModel::dismissNewBadges,
            confirmButton = { TextButton(onClick = viewModel::dismissNewBadges) { Text("¡Genial!") } },
            icon = { Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = ConstructoColors.CraneOrange) },
            title = { Text("¡Nueva insignia desbloqueada!") },
            text = { Text(state.newBadges.joinToString(", ") { it.name.replace('_', ' ') }) }
        )
    }
}

@Composable
private fun BuilderTopBar(title: String, budget: Int, onBack: () -> Unit, onShowBriefing: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ConstructoColors.SteelBlue)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White) }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Presupuesto: $budget monedas", style = MaterialTheme.typography.labelMedium, color = ConstructoColors.WarningYellow)
        }
        IconButton(onClick = onShowBriefing) { Icon(Icons.Filled.Info, contentDescription = "Ver misión", tint = Color.White) }
    }
}

@Composable
private fun ChallengeBriefingDialog(challenge: StructureChallengeModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Info, contentDescription = null, tint = ConstructoColors.SteelBlue) },
        title = { Text(challenge.title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(challenge.briefing, style = MaterialTheme.typography.bodyMedium)
                if (challenge.goals.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Objetivos:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    challenge.goals.forEach { goal ->
                        Text("• ${goalDescription(goal)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Presupuesto: ${challenge.maxBudget} monedas", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("¡Entendido, a construir!") } }
    )
}

private fun goalDescription(goal: ChallengeGoal): String = when (goal.type) {
    ChallengeGoalType.ALTURA_MINIMA -> "Alcanza una altura mínima de ${goal.value}m."
    ChallengeGoalType.PRESUPUESTO_MAXIMO -> "No superes ${goal.value} monedas de presupuesto."
    ChallengeGoalType.RESISTIR_CARGA_LATERAL -> "Tu estructura debe resistir una carga lateral de ${goal.value}."
    ChallengeGoalType.TRIANGULACION_MINIMA -> "Usa al menos ${goal.value} diagonales de triangulación."
    ChallengeGoalType.PESO_MAXIMO -> "No superes un peso total de ${goal.value}."
    ChallengeGoalType.ESTABILIDAD_MINIMA -> "Logra una estabilidad mínima del ${goal.value}%."
}

@Composable
private fun BuilderGridCanvas(
    design: StructureDesign,
    gridWidth: Int,
    gridHeight: Int,
    demandByMember: Map<String, MemberResultModel>,
    pendingStartNodeId: String?,
    onCellTap: (Int, Int) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize().padding(12.dp)) {
        val cellSize = minOf(maxWidth / gridWidth, maxHeight / gridHeight)
        val gridPxWidth = cellSize * gridWidth
        val gridPxHeight = cellSize * gridHeight

        Canvas(
            modifier = Modifier
                .width(gridPxWidth)
                .height(gridPxHeight)
                .align(Alignment.BottomCenter)
                .pointerInput(gridWidth, gridHeight) {
                    detectTapGestures { offset ->
                        val cellPx = size.width / gridWidth
                        val gx = (offset.x / cellPx).toInt().coerceIn(0, gridWidth - 1)
                        val gy = gridHeight - 1 - (offset.y / cellPx).toInt().coerceIn(0, gridHeight - 1)
                        onCellTap(gx, gy)
                    }
                }
        ) {
            val cellPx = size.width / gridWidth
            fun toOffset(pos: NodePosition): Offset = Offset(pos.x * cellPx + cellPx / 2, size.height - (pos.y * cellPx + cellPx / 2))

            // Cuadrícula de plano
            for (i in 0..gridWidth) drawLine(Color.White.copy(alpha = 0.08f), Offset(i * cellPx, 0f), Offset(i * cellPx, size.height))
            for (j in 0..gridHeight) drawLine(Color.White.copy(alpha = 0.08f), Offset(0f, j * cellPx), Offset(size.width, j * cellPx))

            // Miembros
            design.members.forEach { member ->
                val a = design.nodeById(member.nodeAId) ?: return@forEach
                val b = design.nodeById(member.nodeBId) ?: return@forEach
                val demand = demandByMember[member.id]
                val color = demand?.let { colorForDemand(it.state) } ?: colorForMaterial(member.material)
                val strokeWidth = when (member.role) {
                    MemberRole.COLUMNA -> cellPx * 0.16f
                    MemberRole.VIGA -> cellPx * 0.12f
                    MemberRole.DIAGONAL -> cellPx * 0.09f
                }
                drawLine(color, toOffset(a.position), toOffset(b.position), strokeWidth = strokeWidth)
            }

            // Cargas (flecha hacia abajo, o hacia el lado si es lateral)
            design.loads.forEach { load ->
                val node = design.nodeById(load.nodeId) ?: return@forEach
                val center = toOffset(node.position)
                val len = cellPx * 0.55f
                val end = if (load.isLateral) Offset(center.x + len, center.y) else Offset(center.x, center.y + len)
                drawLine(ConstructoColors.WarningYellow, center, end, strokeWidth = cellPx * 0.06f)
                drawCircle(ConstructoColors.WarningYellow, radius = cellPx * 0.08f, center = end)
            }

            // Nodos
            design.nodes.forEach { node ->
                val center = toOffset(node.position)
                val isPending = node.id == pendingStartNodeId
                val nodeColor = when {
                    isPending -> ConstructoColors.WarningYellow
                    node.support != SupportType.NINGUNO -> ConstructoColors.ConcreteGray
                    else -> ConstructoColors.OffWhite
                }
                drawCircle(nodeColor, radius = cellPx * 0.16f, center = center)
                drawCircle(ConstructoColors.InkDark, radius = cellPx * 0.16f, center = center, style = Stroke(width = 2f))
                if (node.support != SupportType.NINGUNO) {
                    // Símbolo de apoyo: triángulo simple bajo el nodo
                    val h = cellPx * 0.18f
                    drawLine(ConstructoColors.SteelBlue, Offset(center.x - h, center.y + h), Offset(center.x + h, center.y + h), strokeWidth = 4f)
                }
            }
        }
    }
}

@Composable
private fun RoleMismatchBanner(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ConstructoColors.DangerRed.copy(alpha = 0.92f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = Color.White)
    }
}

private fun colorForDemand(state: MemberDemandState): Color = when (state) {
    MemberDemandState.SIN_CARGA -> ConstructoColors.DemandNone
    MemberDemandState.BAJA -> ConstructoColors.DemandLow
    MemberDemandState.MEDIA -> ConstructoColors.DemandMedium
    MemberDemandState.ALTA -> ConstructoColors.DemandHigh
    MemberDemandState.FALLO -> ConstructoColors.DemandFail
}

/** Antes de simular, cada pieza se pinta según su material para que madera/acero/concreto se distingan a simple vista. */
private fun colorForMaterial(material: MaterialType): Color = when (material) {
    MaterialType.MADERA -> ConstructoColors.WoodBrown
    MaterialType.ACERO -> ConstructoColors.SteelBlueLight
    MaterialType.CONCRETO -> ConstructoColors.ConcreteGray
}

@Composable
private fun BuilderToolbar(
    selectedTool: BuilderTool,
    selectedMaterial: MaterialType,
    selectedRole: MemberRole,
    allowedMaterials: List<MaterialType>,
    onSelectTool: (BuilderTool) -> Unit,
    onSelectMaterial: (MaterialType) -> Unit,
    onSelectRole: (MemberRole) -> Unit,
    onSave: () -> Unit,
    onSimulate: () -> Unit,
    onClearAll: () -> Unit
) {
    Column(Modifier.background(MaterialTheme.colorScheme.surface).padding(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ToolChip("Nodo", Icons.Filled.RadioButtonChecked, selectedTool == BuilderTool.NODO) { onSelectTool(BuilderTool.NODO) }
            ToolChip("Pieza", Icons.Filled.Timeline, selectedTool == BuilderTool.MIEMBRO) { onSelectTool(BuilderTool.MIEMBRO) }
            ToolChip("Carga", Icons.Filled.ArrowDownward, selectedTool == BuilderTool.CARGA) { onSelectTool(BuilderTool.CARGA) }
            ToolChip("Borrar", Icons.Filled.Delete, selectedTool == BuilderTool.BORRAR) { onSelectTool(BuilderTool.BORRAR) }
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AssistChip(
                onClick = onClearAll,
                leadingIcon = { Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp)) },
                label = { Text("Borrar todo") }
            )
        }
        if (selectedTool == BuilderTool.MIEMBRO) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                allowedMaterials.forEach { mat ->
                    FilterChip(selected = mat == selectedMaterial, onClick = { onSelectMaterial(mat) }, label = { Text(mat.name) })
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MemberRole.values().forEach { role ->
                    FilterChip(selected = role == selectedRole, onClick = { onSelectRole(role) }, label = { Text(role.name) })
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onSave, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Save, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Guardar")
            }
            Button(onClick = onSimulate, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Probar")
            }
        }
    }
}

@Composable
private fun ToolChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
        label = { Text(label) }
    )
}

@Composable
private fun SimulationResultPanel(result: SimulationResultModel, challenge: StructureChallengeModel) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(if (result.passed) ConstructoColors.SuccessGreen.copy(alpha = 0.12f) else ConstructoColors.DangerRed.copy(alpha = 0.10f))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (result.passed) Icons.Filled.CheckCircle else Icons.Filled.Info,
                contentDescription = null,
                tint = if (result.passed) ConstructoColors.SuccessGreen else ConstructoColors.SteelBlue
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (result.passed) "¡Reto superado!" else "Todavía no, ¡sigue intentando!",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            StarRow(stars = result.starsEarned)
        }
        Spacer(Modifier.height(8.dp))
        Text(feedbackText(result), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Altura: ${result.maxHeight}m", style = MaterialTheme.typography.labelMedium)
            Text("Costo: ${result.totalCost}/${challenge.maxBudget}", style = MaterialTheme.typography.labelMedium)
            Text("Estabilidad: ${result.stabilityScore}%", style = MaterialTheme.typography.labelMedium)
        }
        if (result.memberResults.any { it.state == MemberDemandState.FALLO || it.state == MemberDemandState.ALTA }) {
            Spacer(Modifier.height(10.dp))
            Text("Piezas a vigilar:", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                result.memberResults.filter { it.state == MemberDemandState.FALLO || it.state == MemberDemandState.ALTA }
                    .take(4)
                    .forEach { DemandStateChip(it.state) }
            }
        }
    }
}

private fun feedbackText(result: SimulationResultModel): String = when (result.feedbackKey) {
    "feedback_no_conectado" -> "Tu estructura no está conectada al suelo. Revisa que todas las piezas lleguen hasta un apoyo."
    "feedback_miembro_fallido" -> "Alguna pieza recibió más peso del que puede soportar y se rompió. Prueba con un material más resistente o reparte mejor la carga."
    "feedback_presupuesto_excedido" -> "Te pasaste del presupuesto disponible. Intenta usar materiales más económicos o menos piezas."
    "feedback_altura_insuficiente" -> "Tu estructura necesita ser más alta para cumplir este reto."
    "feedback_falta_triangulacion" -> "Te faltan diagonales. Agrega triángulos para resistir mejor los empujes laterales."
    "feedback_objetivo_pendiente" -> "Casi lo logras: revisa los objetivos del reto, todavía falta alguno."
    "feedback_excelente" -> "¡Excelente trabajo de ingeniería! Tu estructura es sólida y eficiente."
    "feedback_solido" -> "Buen diseño, sólido y estable. ¡Sigue así!"
    "feedback_aprobado" -> "Reto aprobado. Tu estructura cumple lo básico, aunque podrías reforzarla más."
    else -> "Sigue explorando distintas combinaciones de piezas y materiales."
}
