package com.educalab.civilestructuras.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.educalab.civilestructuras.domain.logic.ModuleState
import com.educalab.civilestructuras.domain.model.MemberDemandState
import com.educalab.civilestructuras.ui.theme.ConstructoColors
import kotlinx.coroutines.delay

/** Tarjeta ilustrada de un módulo/reto del mapa del Taller. Nunca solo texto: icono + estado + estrellas. */
@Composable
fun ModuleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    state: ModuleState,
    stars: Int,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconDrawableRes: Int? = null
) {
    val locked = state == ModuleState.BLOQUEADO
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cardScale"
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = cardScale; scaleY = cardScale }
            .clickable(enabled = !locked, interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (locked) 0.dp else 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (locked) ConstructoColors.ConcreteGray.copy(alpha = 0.25f) else accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    locked -> Icon(Icons.Filled.Lock, contentDescription = null, tint = ConstructoColors.ConcreteGray, modifier = Modifier.size(28.dp))
                    iconDrawableRes != null -> Icon(
                        painter = androidx.compose.ui.res.painterResource(id = iconDrawableRes),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(36.dp)
                    )
                    else -> Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (state != ModuleState.BLOQUEADO && state != ModuleState.DISPONIBLE) {
                    Spacer(Modifier.height(4.dp))
                    StarRow(stars = stars, size = 14.dp)
                }
            }
            Spacer(Modifier.width(8.dp))
            ModuleStateBadge(state)
        }
    }
}

@Composable
fun ModuleStateBadge(state: ModuleState) {
    val (label, targetColor, icon) = when (state) {
        ModuleState.BLOQUEADO -> Triple("Bloqueado", ConstructoColors.ConcreteGray, Icons.Filled.Lock)
        ModuleState.DISPONIBLE -> Triple("Nuevo", ConstructoColors.SteelBlue, Icons.Filled.PlayArrow)
        ModuleState.INICIADO -> Triple("En curso", ConstructoColors.WarningYellow, Icons.Filled.Build)
        ModuleState.COMPLETADO -> Triple("Hecho", ConstructoColors.SuccessGreen, Icons.Filled.Check)
        ModuleState.DOMINADO -> Triple("¡Dominado!", ConstructoColors.CraneOrange, Icons.Filled.Star)
    }
    val color by animateColorAsState(targetValue = targetColor, animationSpec = tween(300), label = "badgeColor")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

/**
 * Fila de 1 a 3 estrellas. Con [animated] = true, aparecen una por una con un
 * pequeño rebote (usada al revelar el resultado justo después de "Probar").
 */
@Composable
fun StarRow(stars: Int, size: androidx.compose.ui.unit.Dp = 20.dp, animated: Boolean = false) {
    Row {
        repeat(3) { index ->
            var appeared by remember { mutableStateOf(!animated) }
            LaunchedEffect(stars, animated) {
                if (animated) {
                    appeared = false
                    delay(120L * index)
                    appeared = true
                }
            }
            val scale by animateFloatAsState(
                targetValue = if (appeared) 1f else 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "starPop"
            )
            Icon(
                imageVector = if (index < stars) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = if (index < stars) ConstructoColors.WarningYellow else ConstructoColors.ConcreteGray.copy(alpha = 0.5f),
                modifier = Modifier.size(size).scale(if (animated) scale else 1f)
            )
        }
    }
}

/** Barra de progreso global animada, con etiqueta numérica (nunca solo el color). */
@Composable
fun WorkshopProgressBar(percent: Int, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(targetValue = percent / 100f, animationSpec = tween(600), label = "progress")
    Column(modifier) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Progreso del Taller", style = MaterialTheme.typography.labelLarge)
            Text("$percent%", style = MaterialTheme.typography.labelLarge, color = ConstructoColors.CraneOrange)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ConstructoColors.ConcreteGray.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(8.dp))
                    .background(ConstructoColors.CraneOrange)
            )
        }
    }
}

/** Chip de estado de demanda de un miembro: color + icono + texto, nunca solo color (accesibilidad). */
@Composable
fun DemandStateChip(state: MemberDemandState, modifier: Modifier = Modifier) {
    val (label, color, icon) = when (state) {
        MemberDemandState.SIN_CARGA -> Triple("Sin carga", ConstructoColors.DemandNone, Icons.Filled.RadioButtonUnchecked)
        MemberDemandState.BAJA -> Triple("Carga baja", ConstructoColors.DemandLow, Icons.Filled.CheckCircle)
        MemberDemandState.MEDIA -> Triple("Carga media", ConstructoColors.DemandMedium, Icons.Filled.Info)
        MemberDemandState.ALTA -> Triple("Carga alta", ConstructoColors.DemandHigh, Icons.Filled.Warning)
        MemberDemandState.FALLO -> Triple("¡Se rompió!", ConstructoColors.DemandFail, Icons.Filled.Close)
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

/**
 * Avatar circular seleccionable por índice: 4 modelos de niño + 4 de niña,
 * cada uno con su propio color de casco (ver [EngineerAvatarCanvas]).
 */
@Composable
fun AvatarCircle(avatarId: Int, size: androidx.compose.ui.unit.Dp, selected: Boolean = false, modifier: Modifier = Modifier) {
    val palette = listOf(
        ConstructoColors.CraneOrange, ConstructoColors.SteelBlue, ConstructoColors.WarningYellow,
        ConstructoColors.SuccessGreen, ConstructoColors.WoodBrown, ConstructoColors.ConcreteGray,
        ConstructoColors.DangerRed, ConstructoColors.SteelBlueLight
    )
    val helmetColor = palette[avatarId % palette.size]
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(helmetColor)
            .then(if (selected) Modifier.border(3.dp, ConstructoColors.CraneOrange, CircleShape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        EngineerAvatarCanvas(
            avatarId = avatarId,
            modifier = Modifier.fillMaxSize(0.86f)
        )
    }
}

/**
 * Selector fijo de los 8 avatares (4 niño arriba, 4 niña debajo), sin desplazamiento:
 * todos son visibles a la vez para que no se pueda pasar por alto ningún modelo.
 */
@Composable
fun AvatarGridPicker(selectedId: Int, onSelect: (Int) -> Unit, circleSize: androidx.compose.ui.unit.Dp = 56.dp, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for (id in 0 until 4) {
                Box(modifier = Modifier.clip(CircleShape).clickable { onSelect(id) }) {
                    AvatarCircle(avatarId = id, size = circleSize, selected = id == selectedId)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for (id in 4 until 8) {
                Box(modifier = Modifier.clip(CircleShape).clickable { onSelect(id) }) {
                    AvatarCircle(avatarId = id, size = circleSize, selected = id == selectedId)
                }
            }
        }
    }
}
