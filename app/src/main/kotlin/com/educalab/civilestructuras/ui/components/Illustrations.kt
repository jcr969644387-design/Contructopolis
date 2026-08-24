package com.educalab.civilestructuras.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.educalab.civilestructuras.ui.theme.ConstructoColors
import kotlin.math.min

/** Cara sencilla y expresiva de la Ingeniera Nova: casco + gafas + sonrisa. Todo vectorial, sin assets. */
@Composable
fun NovaFaceCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = min(size.width, size.height)
        // Casco
        drawArc(
            color = ConstructoColors.CraneOrange,
            startAngle = 180f, sweepAngle = 180f, useCenter = true,
            topLeft = Offset(size.width / 2 - s / 2, size.height / 2 - s / 2),
            size = androidx.compose.ui.geometry.Size(s, s)
        )
        drawRect(
            color = ConstructoColors.CraneOrangeDark,
            topLeft = Offset(size.width / 2 - s / 2, size.height / 2 - 2f),
            size = androidx.compose.ui.geometry.Size(s, s * 0.08f)
        )
        // Cara
        drawCircle(color = Color(0xFFF2C29A), radius = s * 0.34f, center = Offset(size.width / 2, size.height / 2 + s * 0.12f))
        // Gafas
        val eyeY = size.height / 2 + s * 0.08f
        drawCircle(color = ConstructoColors.SteelBlueLight, radius = s * 0.09f, center = Offset(size.width / 2 - s * 0.14f, eyeY))
        drawCircle(color = ConstructoColors.SteelBlueLight, radius = s * 0.09f, center = Offset(size.width / 2 + s * 0.14f, eyeY))
        // Sonrisa
        drawArc(
            color = ConstructoColors.InkDark,
            startAngle = 20f, sweepAngle = 140f, useCenter = false,
            topLeft = Offset(size.width / 2 - s * 0.16f, size.height / 2 + s * 0.08f),
            size = androidx.compose.ui.geometry.Size(s * 0.32f, s * 0.22f),
            style = Stroke(width = s * 0.035f)
        )
    }
}

/** Patrón sutil de "papel cuadriculado de plano" para fondos, con líneas finas y algún nodo decorativo. */
@Composable
fun BlueprintGridBackground(modifier: Modifier = Modifier, lineColor: Color = ConstructoColors.SteelBlue.copy(alpha = 0.10f)) {
    Canvas(modifier = modifier) {
        val step = 28.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += step
        }
    }
}

/** Grúa de obra decorativa, usada en portada y pantallas vacías del taller. */
@Composable
fun CraneIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val baseX = w * 0.22f
        // Mástil vertical
        drawLine(ConstructoColors.SteelBlue, Offset(baseX, h * 0.95f), Offset(baseX, h * 0.15f), strokeWidth = w * 0.035f)
        // Brazo horizontal
        drawLine(ConstructoColors.SteelBlue, Offset(baseX - w * 0.05f, h * 0.15f), Offset(w * 0.92f, h * 0.18f), strokeWidth = w * 0.03f)
        // Contrapeso
        drawRect(ConstructoColors.ConcreteGray, topLeft = Offset(baseX - w * 0.14f, h * 0.15f), size = androidx.compose.ui.geometry.Size(w * 0.10f, h * 0.06f))
        // Cable y gancho
        drawLine(ConstructoColors.InkDark.copy(alpha = 0.6f), Offset(w * 0.78f, h * 0.19f), Offset(w * 0.78f, h * 0.5f), strokeWidth = w * 0.008f)
        drawCircle(ConstructoColors.WarningYellow, radius = w * 0.03f, center = Offset(w * 0.78f, h * 0.52f))
        // Base
        drawRect(ConstructoColors.SteelBlueLight, topLeft = Offset(baseX - w * 0.08f, h * 0.92f), size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.05f))
    }
}
