package com.educalab.civilestructuras.ui.screens.materials

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educalab.civilestructuras.AppContainer
import com.educalab.civilestructuras.data.local.entity.MaterialEntity
import com.educalab.civilestructuras.ui.theme.ConstructoColors
import com.educalab.civilestructuras.util.IconRegistry
import com.educalab.civilestructuras.viewmodel.GenericViewModelFactory
import com.educalab.civilestructuras.viewmodel.MaterialsViewModel

@Composable
fun MaterialsScreen(container: AppContainer) {
    val viewModel: MaterialsViewModel = viewModel(factory = GenericViewModelFactory({ MaterialsViewModel(it) }, container))
    val materials by viewModel.materials.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Materiales del Taller", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Cada material tiene resistencia, peso y costo distintos. Elige con cabeza.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        items(materials) { material -> MaterialCard(material) }
    }
}

@Composable
private fun MaterialCard(material: MaterialEntity) {
    val color = runCatching { Color(android.graphics.Color.parseColor(material.colorHex)) }.getOrDefault(ConstructoColors.SteelBlue)
    Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = IconRegistry.resolve(material.iconRes)),
                        contentDescription = material.displayName,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(material.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(material.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatPill(icon = Icons.Filled.FitnessCenter, label = "Resistencia", value = material.strength.toInt().toString(), color = ConstructoColors.SuccessGreen)
                StatPill(icon = Icons.Filled.Scale, label = "Peso", value = material.weight.toInt().toString(), color = ConstructoColors.SteelBlue)
                StatPill(icon = Icons.Filled.AttachMoney, label = "Costo", value = material.cost.toString(), color = ConstructoColors.WarningYellow)
            }
        }
    }
}

@Composable
private fun StatPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
