package com.educalab.civilestructuras.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.educalab.civilestructuras.ui.components.CraneIllustration
import com.educalab.civilestructuras.ui.theme.ConstructoColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0.7f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        delay(650)
        onFinished()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ConstructoColors.BlueprintNavy, ConstructoColors.SteelBlue))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CraneIllustration(modifier = Modifier.size(160.dp).scale(scale.value))
            Spacer(Modifier.height(20.dp))
            Text(
                "CONSTRUCTÓPOLIS",
                style = MaterialTheme.typography.headlineLarge,
                color = ConstructoColors.OffWhite,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Taller del Ingeniero Junior",
                style = MaterialTheme.typography.titleMedium,
                color = ConstructoColors.WarningYellow
            )
        }
    }
}

private fun Modifier.graphicsLayerScale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)

private fun Modifier.graphicsLayer(scaleX: Float, scaleY: Float): Modifier =
    this.scale(scaleX = scaleX, scaleY = scaleY)
