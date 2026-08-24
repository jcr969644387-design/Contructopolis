package com.educalab.civilestructuras.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.educalab.civilestructuras.ui.theme.ConstructoColors

/**
 * Diálogo breve de la Ingeniera Nova, la guía del Taller. Se usa con
 * moderación (spec §5): frases cortas, nunca interrumpe más de una vez por
 * pantalla salvo que el niño la vuelva a tocar.
 */
@Composable
fun NovaGuideBubble(
    message: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NovaAvatar(size = 52.dp)
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Avatar circular simple de Nova (casco naranja + cara), dibujado con Canvas, sin assets externos. */
@Composable
fun NovaAvatar(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(ConstructoColors.SteelBlue),
        contentAlignment = Alignment.Center
    ) {
        NovaFaceCanvas(modifier = Modifier.fillMaxSize(0.82f))
    }
}
