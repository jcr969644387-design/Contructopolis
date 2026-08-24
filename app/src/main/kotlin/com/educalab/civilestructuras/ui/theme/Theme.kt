package com.educalab.civilestructuras.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
    primary = ConstructoColors.CraneOrange,
    onPrimary = ConstructoColors.OffWhite,
    secondary = ConstructoColors.SteelBlue,
    onSecondary = ConstructoColors.OffWhite,
    tertiary = ConstructoColors.WarningYellow,
    onTertiary = ConstructoColors.InkDark,
    background = ConstructoColors.OffWhite,
    onBackground = ConstructoColors.InkDark,
    surface = ConstructoColors.PaperCard,
    onSurface = ConstructoColors.InkDark,
    error = ConstructoColors.DangerRed
)

private val DarkScheme = darkColorScheme(
    primary = ConstructoColors.CraneOrange,
    onPrimary = ConstructoColors.BlueprintNavy,
    secondary = ConstructoColors.SteelBlueLight,
    onSecondary = ConstructoColors.BlueprintNavy,
    tertiary = ConstructoColors.WarningYellow,
    onTertiary = ConstructoColors.BlueprintNavy,
    background = ConstructoColors.BlueprintNavy,
    onBackground = ConstructoColors.OffWhite,
    surface = ConstructoColors.BlueprintGrid,
    onSurface = ConstructoColors.OffWhite,
    error = ConstructoColors.DangerRed
)

@Composable
fun ConstructopolisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkScheme else LightScheme
    MaterialTheme(colorScheme = colors, typography = ConstructoTypography, content = content)
}
