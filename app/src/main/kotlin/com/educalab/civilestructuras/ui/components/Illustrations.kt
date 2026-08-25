package com.educalab.civilestructuras.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.educalab.civilestructuras.ui.theme.ConstructoColors
import kotlin.math.min

/**
 * Retrato de la Ingeniera Nova: casco + cara + hombros con chaleco de
 * seguridad, para que se lea como una persona (busto) y no solo una cara
 * flotante. Todo vectorial, sin assets.
 */
@Composable
fun NovaFaceCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = min(size.width, size.height)
        val cx = size.width / 2
        val faceCy = size.height * 0.42f
        val skinTone = Color(0xFFF2C29A)
        val hairColor = Color(0xFF6B4226)

        // Hombros con chaleco de seguridad (da la sensación de persona, no solo cabeza)
        val shoulderTop = size.height * 0.74f
        drawArc(
            color = ConstructoColors.CraneOrange,
            startAngle = 180f, sweepAngle = 180f, useCenter = true,
            topLeft = Offset(cx - s * 0.60f, shoulderTop - s * 0.05f),
            size = Size(s * 1.20f, s * 0.55f)
        )
        // Cuello
        drawRect(
            color = skinTone,
            topLeft = Offset(cx - s * 0.09f, faceCy + s * 0.18f),
            size = Size(s * 0.18f, s * 0.18f)
        )
        // Cintas reflectantes del chaleco
        drawLine(ConstructoColors.WarningYellow, Offset(cx - s * 0.38f, shoulderTop + s * 0.10f), Offset(cx - s * 0.10f, shoulderTop - s * 0.02f), strokeWidth = s * 0.045f)
        drawLine(ConstructoColors.WarningYellow, Offset(cx + s * 0.38f, shoulderTop + s * 0.10f), Offset(cx + s * 0.10f, shoulderTop - s * 0.02f), strokeWidth = s * 0.045f)

        // Orejas
        drawCircle(color = skinTone, radius = s * 0.045f, center = Offset(cx - s * 0.29f, faceCy + s * 0.02f))
        drawCircle(color = skinTone, radius = s * 0.045f, center = Offset(cx + s * 0.29f, faceCy + s * 0.02f))

        // Cara
        drawCircle(color = skinTone, radius = s * 0.30f, center = Offset(cx, faceCy))

        // Mechones de cabello asomando bajo el casco
        drawArc(hairColor, startAngle = 140f, sweepAngle = 55f, useCenter = false, topLeft = Offset(cx - s * 0.31f, faceCy - s * 0.12f), size = Size(s * 0.18f, s * 0.18f), style = Stroke(width = s * 0.035f))
        drawArc(hairColor, startAngle = -15f, sweepAngle = -55f, useCenter = false, topLeft = Offset(cx + s * 0.13f, faceCy - s * 0.12f), size = Size(s * 0.18f, s * 0.18f), style = Stroke(width = s * 0.035f))

        // Casco
        drawArc(
            color = ConstructoColors.CraneOrange,
            startAngle = 180f, sweepAngle = 180f, useCenter = true,
            topLeft = Offset(cx - s * 0.34f, faceCy - s * 0.34f),
            size = Size(s * 0.68f, s * 0.58f)
        )
        drawRect(
            color = ConstructoColors.CraneOrangeDark,
            topLeft = Offset(cx - s * 0.36f, faceCy - s * 0.02f),
            size = Size(s * 0.72f, s * 0.065f)
        )

        // Cejas
        drawLine(ConstructoColors.InkDark, Offset(cx - s * 0.20f, faceCy - s * 0.09f), Offset(cx - s * 0.08f, faceCy - s * 0.11f), strokeWidth = s * 0.018f)
        drawLine(ConstructoColors.InkDark, Offset(cx + s * 0.08f, faceCy - s * 0.11f), Offset(cx + s * 0.20f, faceCy - s * 0.09f), strokeWidth = s * 0.018f)

        // Ojos (esclera + iris)
        val eyeY = faceCy - s * 0.01f
        drawCircle(Color.White, radius = s * 0.07f, center = Offset(cx - s * 0.14f, eyeY))
        drawCircle(Color.White, radius = s * 0.07f, center = Offset(cx + s * 0.14f, eyeY))
        drawCircle(ConstructoColors.SteelBlue, radius = s * 0.04f, center = Offset(cx - s * 0.13f, eyeY))
        drawCircle(ConstructoColors.SteelBlue, radius = s * 0.04f, center = Offset(cx + s * 0.15f, eyeY))

        // Mejillas sonrosadas
        drawCircle(ConstructoColors.CraneOrange.copy(alpha = 0.28f), radius = s * 0.055f, center = Offset(cx - s * 0.20f, faceCy + s * 0.12f))
        drawCircle(ConstructoColors.CraneOrange.copy(alpha = 0.28f), radius = s * 0.055f, center = Offset(cx + s * 0.20f, faceCy + s * 0.12f))

        // Sonrisa
        drawArc(
            color = ConstructoColors.InkDark,
            startAngle = 20f, sweepAngle = 140f, useCenter = false,
            topLeft = Offset(cx - s * 0.13f, faceCy + s * 0.05f),
            size = Size(s * 0.26f, s * 0.18f),
            style = Stroke(width = s * 0.028f)
        )
    }
}

/**
 * Diagrama simple de nodo + viga + columna, usado en la introducción para
 * que se entienda de un vistazo qué es cada pieza básica del Constructor.
 */
@Composable
fun StructureExampleDiagram(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val leftX = w * 0.22f
        val rightX = w * 0.78f
        val topY = h * 0.18f
        val baseY = h * 0.82f
        val nodeRadius = w * 0.05f

        // Columnas (piezas verticales)
        drawLine(ConstructoColors.SteelBlueLight, Offset(leftX, baseY), Offset(leftX, topY), strokeWidth = w * 0.05f)
        drawLine(ConstructoColors.SteelBlueLight, Offset(rightX, baseY), Offset(rightX, topY), strokeWidth = w * 0.05f)

        // Viga (pieza horizontal)
        drawLine(ConstructoColors.WoodBrown, Offset(leftX, topY), Offset(rightX, topY), strokeWidth = w * 0.05f)

        // Símbolo de apoyo en la base
        drawLine(ConstructoColors.ConcreteGray, Offset(leftX - w * 0.07f, baseY + h * 0.05f), Offset(leftX + w * 0.07f, baseY + h * 0.05f), strokeWidth = w * 0.022f)
        drawLine(ConstructoColors.ConcreteGray, Offset(rightX - w * 0.07f, baseY + h * 0.05f), Offset(rightX + w * 0.07f, baseY + h * 0.05f), strokeWidth = w * 0.022f)

        // Nodos (uniones)
        listOf(Offset(leftX, baseY), Offset(rightX, baseY), Offset(leftX, topY), Offset(rightX, topY)).forEach { p ->
            drawCircle(ConstructoColors.OffWhite, radius = nodeRadius, center = p)
            drawCircle(ConstructoColors.InkDark, radius = nodeRadius, center = p, style = Stroke(width = w * 0.009f))
        }
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
