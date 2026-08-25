package com.educalab.civilestructuras

import android.content.Context
import com.educalab.civilestructuras.data.local.ConstructopolisDatabase
import com.educalab.civilestructuras.data.local.Seeder
import com.educalab.civilestructuras.data.repository.*
import com.educalab.civilestructuras.util.FeedbackPlayer

/**
 * Contenedor de dependencias manual. Se evita deliberadamente un framework
 * de inyección (Hilt/Koin) para mantener el build lo más simple y estable
 * posible; con 8 repositorios el costo de cablear a mano es asumible y
 * queda todo explícito en un único lugar.
 */
class AppContainer(context: Context) {
    private val database = ConstructopolisDatabase.getInstance(context)
    val seeder = Seeder(context, database)
    val feedbackPlayer = FeedbackPlayer(context)

    val profileRepository = ProfileRepository(database.profileDao())
    val materialRepository = MaterialRepository(database.materialDao())
    val challengeRepository = ChallengeRepository(database.challengeDao(), database.progressDao())
    val designRepository = DesignRepository(database.designDao())
    val blueprintRepository = BlueprintRepository(database.blueprintDao())
    val badgeRepository = BadgeRepository(database.badgeDao())
    val simulationRepository = SimulationRepository(
        designRepository = designRepository,
        simulationDao = database.simulationDao(),
        challengeRepository = challengeRepository,
        blueprintDao = database.blueprintDao(),
        badgeDao = database.badgeDao(),
        challengeDao = database.challengeDao(),
        progressDao = database.progressDao()
    )
}
