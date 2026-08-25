package com.educalab.civilestructuras.ui.screens.blueprints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educalab.civilestructuras.AppContainer
import com.educalab.civilestructuras.data.local.entity.BadgeEntity
import com.educalab.civilestructuras.data.local.entity.BlueprintRewardEntity
import com.educalab.civilestructuras.ui.theme.ConstructoColors
import com.educalab.civilestructuras.util.IconRegistry
import com.educalab.civilestructuras.viewmodel.BlueprintsViewModel
import com.educalab.civilestructuras.viewmodel.GenericViewModelFactory

@Composable
fun BlueprintsScreen(container: AppContainer) {
    val viewModel: BlueprintsViewModel = viewModel(factory = GenericViewModelFactory({ BlueprintsViewModel(it) }, container))
    val state by viewModel.uiState.collectAsState()
    var tab by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Text(
            "Planos y Logros",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(20.dp)
        )
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Insignias") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Planos") })
        }
        if (tab == 0) {
            val unlockedCount = state.badges.count { it.unlocked }
            Text(
                "$unlockedCount / ${state.badges.size} insignias desbloqueadas",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.badges, key = { it.badge.id }) { item -> BadgeTile(item.badge, item.unlocked) }
            }
        } else {
            val unlockedBlueprints = state.blueprints.count { it.unlockedAt != null }
            Text(
                "$unlockedBlueprints / ${state.blueprints.size} planos desbloqueados",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.blueprints, key = { it.challengeId }) { item -> BlueprintTile(item) }
            }
        }
    }
}

@Composable
private fun BadgeTile(badge: BadgeEntity, unlocked: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) ConstructoColors.CraneOrange.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(14.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (unlocked) ConstructoColors.CraneOrange.copy(alpha = 0.22f) else ConstructoColors.ConcreteGray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (unlocked) {
                    Icon(
                        painter = painterResource(id = IconRegistry.resolve(badge.iconRes)),
                        contentDescription = badge.title,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = ConstructoColors.ConcreteGray, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(badge.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(
                if (unlocked) badge.description else "Sigue jugando para descubrirla",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun BlueprintTile(blueprint: BlueprintRewardEntity) {
    val unlocked = blueprint.unlockedAt != null
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) ConstructoColors.SteelBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(14.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (unlocked) {
                Icon(
                    painter = painterResource(id = IconRegistry.resolve(blueprint.iconRes)),
                    contentDescription = blueprint.title,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = ConstructoColors.ConcreteGray, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (unlocked) blueprint.title else "Plano bloqueado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (unlocked) {
                Text(blueprint.description, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}
