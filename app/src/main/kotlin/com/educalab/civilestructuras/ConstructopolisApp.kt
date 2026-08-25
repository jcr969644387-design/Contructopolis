package com.educalab.civilestructuras

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ConstructopolisApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch {
            container.seeder.seedIfNeeded()
            container.profileRepository.getOrCreateDefault()
        }
        // Mantiene FeedbackPlayer sincronizado con los ajustes del perfil en un
        // solo lugar: así cualquier pantalla puede pedir sonido/vibración sin
        // tener que observar el perfil por su cuenta.
        appScope.launch {
            container.profileRepository.observeProfile().collect { profile ->
                if (profile != null) {
                    container.feedbackPlayer.soundEnabled = profile.soundEnabled
                    container.feedbackPlayer.hapticEnabled = profile.hapticEnabled
                }
            }
        }
    }
}
