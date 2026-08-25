package com.educalab.civilestructuras.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.educalab.civilestructuras.ui.theme.ConstructoColors
import kotlin.math.min

/** Una de las 8 combinaciones de avatar (4 niño + 4 niña) que puede elegir el jugador. */
private data class AvatarSpec(val skinTone: Color, val hairColor: Color, val shirtColor: Color, val isGirl: Boolean)

private val AVATAR_SPECS = listOf(
    // Niños (0-3)
    AvatarSpec(Color(0xFFF2C29A), Color(0xFF1A1A1A), ConstructoColors.SteelBlue, isGirl = false),
    AvatarSpec(Color(0xFFE8B382), Color(0xFF3B2A1E), ConstructoColors.CraneOrange, isGirl = false),
    AvatarSpec(Color(0xFFC68642), Color(0xFFD9A441), ConstructoColors.SuccessGreen, isGirl = false),
    AvatarSpec(Color(0xFF8D5524), Color(0xFFB33A2E), ConstructoColors.SteelBlueLight, isGirl = false),
    // Niñas (4-7)
    AvatarSpec(Color(0xFFE8B382), Color(0xFF3B2A1E), ConstructoColors.WarningYellow, isGirl = true),
    AvatarSpec(Color(0xFFF2C29A), Color(0xFF1A1A1A), ConstructoColors.DangerRed, isGirl = true),
    AvatarSpec(Color(0xFF8D5524), Color(0xFFD9A441), ConstructoColors.WoodBrown, isGirl = true),
    AvatarSpec(Color(0xFFC68642), Color(0xFFB33A2E), ConstructoColors.ConcreteGray, isGirl = true)
)

/** true si el avatar [avatarId] es uno de los 4 modelos de niña (para elegir "Ingeniera" vs "Ingeniero"). */
fun isGirlAvatar(avatarId: Int): Boolean = AVATAR_SPECS[avatarId.mod(AVATAR_SPECS.size)].isGirl

/**
 * Retrato simple de un ingeniero/ingeniera junior con casco, para el
 * selector de avatar (8 combinaciones: 4 niño + 4 niña). Pensado para verse
 * bien incluso muy pequeño (48-96dp): formas grandes y sin detalle fino.
 * El color de fondo del círculo (ver AvatarCircle) hace de casco; aquí solo
 * se dibujan cara, cabello y facciones.
 */
@Composable
fun EngineerAvatarCanvas(avatarId: Int, modifier: Modifier = Modifier) {
    val spec = AVATAR_SPECS[avatarId.mod(AVATAR_SPECS.size)]
    Canvas(modifier = modifier) {
        val s = min(size.width, size.height)
        val cx = size.width / 2
        val cy = size.height / 2
        val faceCenter = Offset(cx, cy + s * 0.04f)

        // Coletas (solo niñas), dibujadas antes que la cara para que asomen a los lados
        if (spec.isGirl) {
            drawCircle(spec.hairColor, radius = s * 0.14f, center = Offset(cx - s * 0.36f, cy + s * 0.22f))
            drawCircle(spec.hairColor, radius = s * 0.14f, center = Offset(cx + s * 0.36f, cy + s * 0.22f))
        }

        // Cara
        drawCircle(spec.skinTone, radius = s * 0.34f, center = faceCenter)

        // Mechones de cabello en las sienes
        drawArc(
            spec.hairColor, startAngle = 150f, sweepAngle = 55f, useCenter = false,
            topLeft = Offset(cx - s * 0.40f, cy - s * 0.20f), size = Size(s * 0.22f, s * 0.22f), style = Stroke(width = s * 0.05f)
        )
        drawArc(
            spec.hairColor, startAngle = -25f, sweepAngle = -55f, useCenter = false,
            topLeft = Offset(cx + s * 0.18f, cy - s * 0.20f), size = Size(s * 0.22f, s * 0.22f), style = Stroke(width = s * 0.05f)
        )

        // Cuerpo/hombros: se dibuja encima de la base de la cara para marcar el cuello,
        // como un retrato de busto dentro del círculo del avatar.
        val neckY = faceCenter.y + s * 0.26f
        val shoulderPath = Path().apply {
            moveTo(cx - s * 0.16f, neckY)
            lineTo(cx - s * 0.46f, size.height)
            lineTo(cx + s * 0.46f, size.height)
            lineTo(cx + s * 0.16f, neckY)
            close()
        }
        drawPath(shoulderPath, color = spec.shirtColor)

        // Casco: domo + ala, siempre blanco (con borde oscuro) para que resalte sobre cualquier fondo
        val brimTopLeft = Offset(cx - s * 0.38f, cy - s * 0.30f)
        val brimSize = Size(s * 0.76f, s * 0.09f)
        val domeSize = Size(s * 0.62f, s * 0.32f)
        val domeTopLeft = Offset(cx - domeSize.width / 2f, brimTopLeft.y - domeSize.height / 2f)

        drawArc(color = Color.White, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = domeTopLeft, size = domeSize)
        drawArc(color = ConstructoColors.InkDark, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = domeTopLeft, size = domeSize, style = Stroke(width = s * 0.018f))

        drawRect(color = Color.White, topLeft = brimTopLeft, size = brimSize)
        drawRect(color = ConstructoColors.InkDark, topLeft = brimTopLeft, size = brimSize, style = Stroke(width = s * 0.018f))

        // Ojos
        drawCircle(ConstructoColors.InkDark, radius = s * 0.045f, center = Offset(cx - s * 0.13f, cy - s * 0.02f))
        drawCircle(ConstructoColors.InkDark, radius = s * 0.045f, center = Offset(cx + s * 0.13f, cy - s * 0.02f))

        // Sonrisa
        drawArc(
            color = ConstructoColors.InkDark,
            startAngle = 20f, sweepAngle = 140f, useCenter = false,
            topLeft = Offset(cx - s * 0.11f, cy + s * 0.06f),
            size = Size(s * 0.22f, s * 0.16f),
            style = Stroke(width = s * 0.035f)
        )
    }
}

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
        val faceCy = size.height * 0.34f
        val skinTone = Color(0xFFF2C29A)
        val hairColor = Color(0xFF6B4226)

        // Hombros con chaleco de seguridad (semicírculo INFERIOR: cae hacia
        // abajo desde el cuello, en vez de subir como una cúpula).
        val shoulderTop = faceCy + s * 0.32f
        drawArc(
            color = ConstructoColors.CraneOrange,
            startAngle = 0f, sweepAngle = 180f, useCenter = true,
            topLeft = Offset(cx - s * 0.525f, shoulderTop - s * 0.275f),
            size = Size(s * 1.05f, s * 0.55f)
        )
        // Cintas reflectantes del chaleco
        drawLine(ConstructoColors.WarningYellow, Offset(cx - s * 0.30f, shoulderTop + s * 0.03f), Offset(cx - s * 0.06f, shoulderTop + s * 0.17f), strokeWidth = s * 0.045f)
        drawLine(ConstructoColors.WarningYellow, Offset(cx + s * 0.30f, shoulderTop + s * 0.03f), Offset(cx + s * 0.06f, shoulderTop + s * 0.17f), strokeWidth = s * 0.045f)

        // Cuello
        drawRect(
            color = skinTone,
            topLeft = Offset(cx - s * 0.09f, faceCy + s * 0.19f),
            size = Size(s * 0.18f, s * 0.15f)
        )

        // Orejas (dibujadas antes que la cara: solo asoma el borde exterior)
        drawCircle(color = skinTone, radius = s * 0.045f, center = Offset(cx - s * 0.225f, faceCy))
        drawCircle(color = skinTone, radius = s * 0.045f, center = Offset(cx + s * 0.225f, faceCy))

        // Cara
        drawCircle(color = skinTone, radius = s * 0.23f, center = Offset(cx, faceCy))

        // Mechones de cabello en las sienes
        drawArc(hairColor, startAngle = 130f, sweepAngle = 60f, useCenter = false, topLeft = Offset(cx - s * 0.27f, faceCy - s * 0.10f), size = Size(s * 0.14f, s * 0.14f), style = Stroke(width = s * 0.03f))
        drawArc(hairColor, startAngle = -10f, sweepAngle = -60f, useCenter = false, topLeft = Offset(cx + s * 0.13f, faceCy - s * 0.10f), size = Size(s * 0.14f, s * 0.14f), style = Stroke(width = s * 0.03f))

        // Casco: cúpula (semicírculo SUPERIOR) + ala/borde bien por encima de
        // las cejas y los ojos, para que no se solapen (antes quedaba justo
        // sobre los ojos y parecía unas gafas sobre una franja naranja).
        drawArc(
            color = ConstructoColors.CraneOrange,
            startAngle = 180f, sweepAngle = 180f, useCenter = true,
            topLeft = Offset(cx - s * 0.275f, faceCy - s * 0.325f),
            size = Size(s * 0.55f, s * 0.39f)
        )
        drawRect(
            color = ConstructoColors.CraneOrangeDark,
            topLeft = Offset(cx - s * 0.30f, faceCy - s * 0.155f),
            size = Size(s * 0.60f, s * 0.05f)
        )

        // Cejas (entre el ala del casco y los ojos, sin tocar ninguno de los dos)
        drawLine(ConstructoColors.InkDark, Offset(cx - s * 0.16f, faceCy - s * 0.095f), Offset(cx - s * 0.06f, faceCy - s * 0.085f), strokeWidth = s * 0.013f)
        drawLine(ConstructoColors.InkDark, Offset(cx + s * 0.06f, faceCy - s * 0.085f), Offset(cx + s * 0.16f, faceCy - s * 0.095f), strokeWidth = s * 0.013f)

        // Ojos (esclera + iris)
        val eyeY = faceCy - s * 0.02f
        drawCircle(Color.White, radius = s * 0.06f, center = Offset(cx - s * 0.12f, eyeY))
        drawCircle(Color.White, radius = s * 0.06f, center = Offset(cx + s * 0.12f, eyeY))
        drawCircle(ConstructoColors.SteelBlue, radius = s * 0.035f, center = Offset(cx - s * 0.11f, eyeY))
        drawCircle(ConstructoColors.SteelBlue, radius = s * 0.035f, center = Offset(cx + s * 0.13f, eyeY))

        // Mejillas sonrosadas
        drawCircle(ConstructoColors.CraneOrange.copy(alpha = 0.28f), radius = s * 0.045f, center = Offset(cx - s * 0.17f, faceCy + s * 0.10f))
        drawCircle(ConstructoColors.CraneOrange.copy(alpha = 0.28f), radius = s * 0.045f, center = Offset(cx + s * 0.17f, faceCy + s * 0.10f))

        // Sonrisa
        drawArc(
            color = ConstructoColors.InkDark,
            startAngle = 20f, sweepAngle = 140f, useCenter = false,
            topLeft = Offset(cx - s * 0.11f, faceCy + s * 0.04f),
            size = Size(s * 0.22f, s * 0.15f),
            style = Stroke(width = s * 0.025f)
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
