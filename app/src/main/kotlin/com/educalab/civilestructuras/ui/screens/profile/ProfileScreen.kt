package com.educalab.civilestructuras.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educalab.civilestructuras.AppContainer
import com.educalab.civilestructuras.data.local.entity.UserProfileEntity
import com.educalab.civilestructuras.ui.components.AvatarCircle
import com.educalab.civilestructuras.viewmodel.GenericViewModelFactory
import com.educalab.civilestructuras.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(container: AppContainer, onBack: () -> Unit) {
    val viewModel: ProfileViewModel = viewModel(factory = GenericViewModelFactory({ ProfileViewModel(it) }, container))
    val profile by viewModel.profile.collectAsState()

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
            Text("Mi perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))

        val p = profile
        if (p == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            var alias by remember(p.alias) { mutableStateOf(p.alias) }

            Text("Elige tu avatar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items((0 until UserProfileEntity.AVATAR_COUNT).toList()) { avatarId ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { viewModel.updateAvatar(avatarId) }
                    ) {
                        AvatarCircle(avatarId = avatarId, size = 56.dp, selected = avatarId == p.avatarId)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Tu alias (no uses tu nombre real)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = alias,
                onValueChange = { if (it.length <= 18) alias = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { viewModel.updateAlias(alias) }) { Text("Guardar") }
                }
            )

            Spacer(Modifier.height(24.dp))
            SettingSwitchRow("Sonido", p.soundEnabled) { viewModel.setSoundEnabled(it) }
            SettingSwitchRow("Vibración (háptica)", p.hapticEnabled) { viewModel.setHapticEnabled(it) }

            Spacer(Modifier.height(24.dp))
            Text(
                "Constructópolis guarda todo solo en este dispositivo. No pedimos correo, teléfono ni ubicación.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
