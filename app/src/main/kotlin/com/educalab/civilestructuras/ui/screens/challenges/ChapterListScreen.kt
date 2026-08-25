package com.educalab.civilestructuras.ui.screens.challenges

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educalab.civilestructuras.AppContainer
import com.educalab.civilestructuras.ui.components.ModuleCard
import com.educalab.civilestructuras.ui.navigation.Routes
import com.educalab.civilestructuras.ui.theme.ConstructoColors
import com.educalab.civilestructuras.util.IconRegistry
import com.educalab.civilestructuras.viewmodel.ChapterListViewModel
import com.educalab.civilestructuras.viewmodel.GenericViewModelFactory

private fun chapterIconRes(category: String): String = when (category) {
    "VIGA" -> "ic_challenge_viga"
    "COLUMNA" -> "ic_challenge_columna"
    "TORRE" -> "ic_challenge_torre"
    "CARGA" -> "ic_challenge_carga"
    else -> "ic_challenge_reto"
}

@Composable
fun ChapterListScreen(
    container: AppContainer,
    chapter: Int,
    onOpenChallenge: (String) -> Unit
) {
    val viewModel: ChapterListViewModel = viewModel(factory = GenericViewModelFactory({ ChapterListViewModel(it) }, container))
    val challenges by remember(chapter) { viewModel.challengesFor(chapter) }.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(Routes.titleForChapter(chapter), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            challenges.firstOrNull()?.challenge?.briefing?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            Spacer(Modifier.height(6.dp))
        }
        items(challenges, key = { it.challenge.id }) { summary ->
            ModuleCard(
                title = summary.challenge.title,
                subtitle = "Presupuesto: ${summary.challenge.maxBudget} monedas",
                icon = Icons.Filled.EmojiEvents,
                iconDrawableRes = IconRegistry.resolve(chapterIconRes(summary.challenge.category)),
                state = summary.state,
                stars = summary.bestStars,
                accentColor = ConstructoColors.CraneOrange,
                onClick = { container.feedbackPlayer.tap(); onOpenChallenge(summary.challenge.id) }
            )
        }
    }
}
