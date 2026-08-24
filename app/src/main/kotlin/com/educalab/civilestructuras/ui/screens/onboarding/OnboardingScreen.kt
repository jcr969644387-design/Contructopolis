package com.educalab.civilestructuras.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.educalab.civilestructuras.ui.components.CraneIllustration
import com.educalab.civilestructuras.ui.components.NovaAvatar
import com.educalab.civilestructuras.ui.theme.ConstructoColors
import kotlinx.coroutines.launch

private data class OnboardingPage(val title: String, val body: String, val icon: ImageVector)

private val PAGES = listOf(
    OnboardingPage(
        "Bienvenida al Taller",
        "Soy la Ingeniera Nova. Aquí vas a diseñar puentes, torres y estructuras de verdad, pieza por pieza.",
        Icons.Filled.Engineering
    ),
    OnboardingPage(
        "Elige tus materiales",
        "Madera, acero y concreto. Cada uno pesa distinto, cuesta distinto y aguanta distinto. Tú decides.",
        Icons.Filled.Construction
    ),
    OnboardingPage(
        "Construye y pon a prueba",
        "Conecta nodos, arma vigas y columnas, añade cargas y presiona Probar. Verás si tu estructura resiste.",
        Icons.Filled.Handyman
    ),
    OnboardingPage(
        "Tu taller es privado",
        "Todo se guarda solo en este dispositivo. No pedimos tu nombre real ni tu correo: elige un alias y un avatar.",
        Icons.Filled.Shield
    )
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { PAGES.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ConstructoColors.OffWhite)
    ) {
        Column(Modifier.fillMaxSize()) {
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                val data = PAGES[page]
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (page == 0) {
                        CraneIllustration(modifier = Modifier.size(120.dp))
                        Spacer(Modifier.height(12.dp))
                        NovaAvatar(size = 72.dp)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .background(ConstructoColors.CraneOrange.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(data.icon, contentDescription = null, tint = ConstructoColors.CraneOrange, modifier = Modifier.size(48.dp))
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(data.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Text(data.body, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = ConstructoColors.InkDark.copy(alpha = 0.75f))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(PAGES.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                            .background(
                                if (pagerState.currentPage == index) ConstructoColors.CraneOrange else ConstructoColors.ConcreteGray.copy(alpha = 0.4f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }

            Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onFinished) { Text("Saltar") }
                Button(onClick = {
                    if (pagerState.currentPage == PAGES.lastIndex) {
                        onFinished()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                }) {
                    Text(if (pagerState.currentPage == PAGES.lastIndex) "¡Empezar!" else "Siguiente")
                }
            }
        }
    }
}
