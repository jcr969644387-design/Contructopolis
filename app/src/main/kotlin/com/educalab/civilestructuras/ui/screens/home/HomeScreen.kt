package com.educalab.civilestructuras.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educalab.civilestructuras.AppContainer
import com.educalab.civilestructuras.domain.logic.ModuleState
import com.educalab.civilestructuras.ui.components.*
import com.educalab.civilestructuras.ui.navigation.Routes
import com.educalab.civilestructuras.ui.theme.ConstructoColors
import com.educalab.civilestructuras.viewmodel.GenericViewModelFactory
import com.educalab.civilestructuras.viewmodel.HomeViewModel

private data class WorkshopModule(
    val chapter: Int?,
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)

private val MODULES = listOf(
    WorkshopModule(null, Routes.CONCEPTS, "Conceptos", "Ideas clave de ingeniería", Icons.Filled.Lightbulb, ConstructoColors.WarningYellow),
    WorkshopModule(null, Routes.MATERIALS, "Materiales", "Madera, acero y concreto", Icons.Filled.Category, ConstructoColors.WoodBrown),
    WorkshopModule(Routes.CHAPTER_VIGAS, Routes.chapter(Routes.CHAPTER_VIGAS), "Vigas", "Puentes y pasarelas", Icons.Filled.HorizontalRule, ConstructoColors.SteelBlue),
    WorkshopModule(Routes.CHAPTER_COLUMNAS, Routes.chapter(Routes.CHAPTER_COLUMNAS), "Columnas", "Pilares y soportes", Icons.Filled.ViewColumn, ConstructoColors.ConcreteGray),
    WorkshopModule(Routes.CHAPTER_TORRES, Routes.chapter(Routes.CHAPTER_TORRES), "Torres", "Construye hacia el cielo", Icons.Filled.CellTower, ConstructoColors.CraneOrange),
    WorkshopModule(Routes.CHAPTER_CARGAS, Routes.chapter(Routes.CHAPTER_CARGAS), "Cargas y Viento", "Resiste la simulación", Icons.Filled.Air, ConstructoColors.SteelBlueLight),
    WorkshopModule(Routes.CHAPTER_RETOS, Routes.chapter(Routes.CHAPTER_RETOS), "Gran Taller de Retos", "Todo junto, a prueba", Icons.Filled.EmojiEvents, ConstructoColors.DangerRed),
    WorkshopModule(null, Routes.BLUEPRINTS, "Planos y Logros", "Tu colección de insignias", Icons.Filled.WorkspacePremium, ConstructoColors.SuccessGreen)
)

@Composable
fun HomeScreen(
    container: AppContainer,
    onNavigate: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel(factory = GenericViewModelFactory({ HomeViewModel(it) }, container))
    val state by viewModel.uiState.collectAsState()

    Box(Modifier.fillMaxSize()) {
        BlueprintGridBackground(modifier = Modifier.fillMaxSize())
        LazyColumn(
            modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { HomeHeader(alias = state.alias, avatarId = state.avatarId, onProfileClick = onProfileClick) }
            item {
                Box(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    WorkshopProgressBar(percent = state.overallProgressPercent)
                }
            }
            state.nextChallenge?.let { next ->
                item {
                    NextChallengeBanner(
                        title = next.challenge.title,
                        chapterTitle = Routes.titleForChapter(next.challenge.worldChapter),
                        onClick = { onNavigate(Routes.builder(next.challenge.id)) }
                    )
                }
            }
            item {
                Text(
                    "Módulos del Taller",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
            items(MODULES) { module ->
                val summaries = module.chapter?.let { state.summariesByChapter[it] }
                val completed = summaries?.count { it.state == ModuleState.COMPLETADO || it.state == ModuleState.DOMINADO } ?: 0
                val total = summaries?.size ?: 0
                val subtitle = if (total > 0) "${module.subtitle} · $completed/$total" else module.subtitle
                val moduleState = when {
                    summaries == null -> ModuleState.DISPONIBLE
                    summaries.all { it.state == ModuleState.DOMINADO } && summaries.isNotEmpty() -> ModuleState.DOMINADO
                    summaries.all { it.state == ModuleState.COMPLETADO || it.state == ModuleState.DOMINADO } && summaries.isNotEmpty() -> ModuleState.COMPLETADO
                    summaries.any { it.state == ModuleState.INICIADO } -> ModuleState.INICIADO
                    else -> ModuleState.DISPONIBLE
                }
                Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    ModuleCard(
                        title = module.title,
                        subtitle = subtitle,
                        icon = module.icon,
                        state = moduleState,
                        stars = summaries?.maxOfOrNull { it.bestStars } ?: 0,
                        accentColor = module.color,
                        onClick = { onNavigate(module.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(alias: String, avatarId: Int, onProfileClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(ConstructoColors.BlueprintNavy, ConstructoColors.SteelBlue)))
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("¡Hola, $alias!", style = MaterialTheme.typography.headlineMedium, color = ConstructoColors.OffWhite, fontWeight = FontWeight.Bold)
                Text("Bienvenida de vuelta al Taller", style = MaterialTheme.typography.bodyMedium, color = ConstructoColors.WarningYellow)
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ConstructoColors.OffWhite.copy(alpha = 0.12f))
                    .clickable(onClick = onProfileClick)
                    .padding(4.dp)
            ) {
                AvatarCircle(avatarId = avatarId, size = 48.dp)
            }
        }
    }
}

@Composable
private fun NextChallengeBanner(title: String, chapterTitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ConstructoColors.CraneOrange.copy(alpha = 0.14f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.RocketLaunch, contentDescription = null, tint = ConstructoColors.CraneOrange, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Siguiente reto sugerido", style = MaterialTheme.typography.labelMedium, color = ConstructoColors.CraneOrangeDark)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(chapterTitle, style = MaterialTheme.typography.bodyMedium)
            }
            FilledTonalButton(onClick = onClick) { Text("Ir") }
        }
    }
}
