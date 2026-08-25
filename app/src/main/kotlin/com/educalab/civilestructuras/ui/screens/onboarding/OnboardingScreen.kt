package com.educalab.civilestructuras.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.educalab.civilestructuras.data.local.entity.UserProfileEntity
import com.educalab.civilestructuras.ui.components.AvatarCircle
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

private val PROFILE_PAGE_INDEX = PAGES.size

/**
 * onFinished recibe el alias y avatar elegidos (o los valores por defecto si
 * el usuario nunca llegó a la página de perfil) para guardarlos junto con
 * el flag de onboarding completado en una sola escritura.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: (alias: String, avatarId: Int) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { PAGES.size + 1 })
    val scope = rememberCoroutineScope()

    var alias by remember { mutableStateOf("Ingeniera Junior") }
    var avatarId by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ConstructoColors.OffWhite)
    ) {
        Column(Modifier.fillMaxSize()) {
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                if (page < PROFILE_PAGE_INDEX) {
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
                                    .background(ConstructoColors.CraneOrange.copy(alpha = 0.15f), shape = CircleShape),
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
                } else {
                    ProfileSetupPage(
                        alias = alias,
                        onAliasChange = { alias = it },
                        avatarId = avatarId,
                        onAvatarChange = { avatarId = it }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                            .background(
                                if (pagerState.currentPage == index) ConstructoColors.CraneOrange else ConstructoColors.ConcreteGray.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    )
                }
            }

            Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { onFinished(alias, avatarId) }) { Text("Saltar") }
                Button(onClick = {
                    if (pagerState.currentPage == PROFILE_PAGE_INDEX) {
                        onFinished(alias, avatarId)
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                }) {
                    Text(if (pagerState.currentPage == PROFILE_PAGE_INDEX) "¡Empezar!" else "Siguiente")
                }
            }
        }
    }
}

@Composable
private fun ProfileSetupPage(
    alias: String,
    onAliasChange: (String) -> Unit,
    avatarId: Int,
    onAvatarChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Crea tu perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))

        Text("Elige tu avatar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items((0 until UserProfileEntity.AVATAR_COUNT).toList()) { id ->
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onAvatarChange(id) }
                ) {
                    AvatarCircle(avatarId = id, size = 56.dp, selected = id == avatarId)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Tu alias (no uses tu nombre real)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = alias,
            onValueChange = { if (it.length <= 18) onAliasChange(it) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
