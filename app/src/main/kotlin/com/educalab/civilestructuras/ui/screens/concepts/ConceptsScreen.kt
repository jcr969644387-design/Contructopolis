package com.educalab.civilestructuras.ui.screens.concepts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educalab.civilestructuras.AppContainer
import com.educalab.civilestructuras.ui.theme.ConstructoColors
import com.educalab.civilestructuras.viewmodel.GenericViewModelFactory
import com.educalab.civilestructuras.viewmodel.ProfileViewModel

private data class ConceptCard(
    val title: String,
    val everydayExample: String,
    val explanation: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color
)

private val CONCEPTS = listOf(
    ConceptCard(
        "Carga",
        "Como cuando cargas la mochila del cole: entre más libros metas, más pesada se siente.",
        "Una carga es cualquier peso que empuja sobre una estructura: personas, muebles, nieve o hasta el viento.",
        Icons.Filled.FitnessCenter, ConstructoColors.SteelBlue
    ),
    ConceptCard(
        "Apoyo",
        "Como las patas de una mesa: sin ellas, la mesa se cae al piso.",
        "Un apoyo conecta la estructura con el suelo. Sin apoyos, no hay forma de sostener ningún peso.",
        Icons.Filled.Anchor, ConstructoColors.ConcreteGray
    ),
    ConceptCard(
        "Viga",
        "Como el estante de una repisa: se acuesta y aguanta cosas encima a lo largo de toda su longitud.",
        "Una viga es una pieza horizontal que reparte el peso desde arriba hacia sus apoyos en los extremos.",
        Icons.Filled.HorizontalRule, ConstructoColors.WoodBrown
    ),
    ConceptCard(
        "Columna",
        "Como tus piernas cuando estás de pie: sostienen todo tu cuerpo en vertical.",
        "Una columna es una pieza vertical que transporta el peso de arriba hacia abajo, hasta el suelo.",
        Icons.Filled.ViewColumn, ConstructoColors.SteelBlueLight
    ),
    ConceptCard(
        "Triangulación",
        "Un triángulo de cartón no se deforma al empujarlo de lado; un cuadrado de cartón sí.",
        "Agregar diagonales forma triángulos, que son mucho más rígidos frente a empujes laterales (viento).",
        Icons.Filled.ChangeHistory, ConstructoColors.CraneOrange
    ),
    ConceptCard(
        "Esbeltez",
        "Un lápiz muy largo se dobla más fácil que uno corto y grueso si lo empujas por las puntas.",
        "Cuanto más larga y delgada es una columna, más fácil se pandea (se dobla) bajo el mismo peso.",
        Icons.Filled.Height, ConstructoColors.DangerRed
    ),
    ConceptCard(
        "Estabilidad",
        "Un banquito de tres patas firmes no se tambalea; uno con una pata floja, sí.",
        "La estabilidad mide qué tan bien reparte una estructura sus cargas sin que ninguna pieza falle.",
        Icons.Filled.Balance, ConstructoColors.SuccessGreen
    ),
    ConceptCard(
        "Presupuesto",
        "Como el dinero de tu alcancía: hay que elegir bien en qué gastarlo para que alcance.",
        "Cada material cuesta distinto. Un buen ingeniero busca la estructura más segura al menor costo.",
        Icons.Filled.Savings, ConstructoColors.WarningYellow
    )
)

@Composable
fun ConceptsScreen(container: AppContainer, onAllConceptsConfirmed: () -> Unit) {
    val viewModel: ProfileViewModel = viewModel(factory = GenericViewModelFactory({ ProfileViewModel(it) }, container))
    val profile by viewModel.profile.collectAsState()
    val alreadyViewed = profile?.conceptsViewed == true
    var confirmed by remember { mutableStateOf(setOf<String>()) }
    LaunchedEffect(alreadyViewed) {
        if (alreadyViewed) confirmed = CONCEPTS.map { it.title }.toSet()
    }
    LaunchedEffect(confirmed) {
        if (!alreadyViewed && confirmed.size >= CONCEPTS.size) {
            viewModel.markConceptsViewed()
            onAllConceptsConfirmed()
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Conceptos del Taller", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Toca cada tarjeta, léela y marca \"Entendido\". Cuando confirmes las ${CONCEPTS.size}, pasarás automáticamente a Materiales.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(4.dp))
        }
        items(CONCEPTS) { concept ->
            ConceptCardItem(
                concept = concept,
                confirmed = concept.title in confirmed,
                onConfirm = {
                    container.feedbackPlayer.confirm()
                    confirmed = confirmed + concept.title
                }
            )
        }
    }
}

@Composable
private fun ConceptCardItem(concept: ConceptCard, confirmed: Boolean, onConfirm: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (confirmed) ConstructoColors.SuccessGreen.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .then(if (!expanded) Modifier.clickable { expanded = true } else Modifier),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(concept.color.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(concept.icon, contentDescription = null, tint = concept.color, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(concept.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(concept.everydayExample, style = MaterialTheme.typography.bodyMedium, maxLines = if (expanded) 6 else 2)
                }
                if (confirmed) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Entendido", tint = ConstructoColors.SuccessGreen, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(4.dp))
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = "Expandir")
                }
            }
            if (expanded) {
                Text(
                    concept.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    if (confirmed) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            leadingIcon = { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("¡Entendido!") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = ConstructoColors.SuccessGreen.copy(alpha = 0.25f),
                                labelColor = ConstructoColors.SuccessGreen,
                                leadingIconContentColor = ConstructoColors.SuccessGreen,
                                disabledContainerColor = ConstructoColors.SuccessGreen.copy(alpha = 0.25f),
                                disabledLabelColor = ConstructoColors.SuccessGreen,
                                disabledLeadingIconContentColor = ConstructoColors.SuccessGreen
                            )
                        )
                    } else {
                        Button(onClick = onConfirm) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Entendido")
                        }
                    }
                }
            }
        }
    }
}
