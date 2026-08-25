package com.educalab.civilestructuras.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.educalab.civilestructuras.data.local.entity.UserProfileEntity
import com.educalab.civilestructuras.ui.components.AvatarCircle
import com.educalab.civilestructuras.ui.theme.ConstructoColors

/**
 * Pantalla dedicada a elegir avatar y alias, mostrada una sola vez justo
 * después de la introducción (onboarding) y antes de entrar al Taller.
 */
@Composable
fun ProfileSetupScreen(onContinue: (alias: String, avatarId: Int) -> Unit) {
    var alias by remember { mutableStateOf("Ingeniera Junior") }
    var avatarId by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ConstructoColors.OffWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Crea tu perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "Elige un avatar y un alias. No uses tu nombre real.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = ConstructoColors.InkDark.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(28.dp))

            Text("Elige tu avatar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items((0 until UserProfileEntity.AVATAR_COUNT).toList()) { id ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { avatarId = id }
                    ) {
                        AvatarCircle(avatarId = id, size = 56.dp, selected = id == avatarId)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Tu alias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = alias,
                onValueChange = { if (it.length <= 18) alias = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))
            Button(onClick = { onContinue(alias, avatarId) }, modifier = Modifier.fillMaxWidth()) {
                Text("¡Empezar!")
            }
        }
    }
}
