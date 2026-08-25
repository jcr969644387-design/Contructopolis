package com.educalab.civilestructuras.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.educalab.civilestructuras.ui.components.AvatarGridPicker
import com.educalab.civilestructuras.ui.theme.ConstructoColors
import com.educalab.civilestructuras.ui.theme.ConstructopolisTheme

/**
 * Pantalla dedicada a elegir avatar y alias, mostrada una sola vez justo
 * después de la introducción (onboarding) y antes de entrar al Taller.
 *
 * Se fuerza el esquema claro (fondo fijo OffWhite) para que el campo de
 * texto resuelva colores claros: en modo oscuro el campo tomaba el color de
 * texto claro del tema por defecto y quedaba invisible sobre este fondo
 * claro fijo.
 */
@Composable
fun ProfileSetupScreen(onContinue: (alias: String, avatarId: Int) -> Unit) {
    ConstructopolisTheme(darkTheme = false) {
        var alias by remember { mutableStateOf("") }
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
                AvatarGridPicker(selectedId = avatarId, onSelect = { avatarId = it })

                Spacer(Modifier.height(24.dp))
                Text("Tu alias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = alias,
                    onValueChange = { if (it.length <= 18) alias = it },
                    placeholder = { Text("Ingeniera Junior") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { onContinue(alias.ifBlank { "Ingeniera Junior" }, avatarId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("¡Empezar!")
                }
            }
        }
    }
}
