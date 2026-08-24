package com.educalab.civilestructuras.ui.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import com.educalab.civilestructuras.AppContainer
import com.educalab.civilestructuras.ConstructopolisApp
import com.educalab.civilestructuras.ui.screens.blueprints.BlueprintsScreen
import com.educalab.civilestructuras.ui.screens.builder.BuilderScreen
import com.educalab.civilestructuras.ui.screens.challenges.ChapterListScreen
import com.educalab.civilestructuras.ui.screens.concepts.ConceptsScreen
import com.educalab.civilestructuras.ui.screens.home.HomeScreen
import com.educalab.civilestructuras.ui.screens.materials.MaterialsScreen
import com.educalab.civilestructuras.ui.screens.onboarding.OnboardingScreen
import com.educalab.civilestructuras.ui.screens.profile.ProfileScreen
import com.educalab.civilestructuras.ui.screens.splash.SplashScreen
import kotlinx.coroutines.launch

@Composable
fun ConstructopolisNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            var ready by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            SplashScreen(onFinished = { ready = true })
            if (ready) {
                LaunchedEffect(Unit) {
                    val profile = container.profileRepository.getOrCreateDefault()
                    val destination = if (profile.onboardingCompleted) Routes.HOME else Routes.ONBOARDING
                    navController.navigate(destination) { popUpTo(Routes.SPLASH) { inclusive = true } }
                }
            }
        }
        composable(Routes.ONBOARDING) {
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            OnboardingScreen(onFinished = {
                scope.launch {
                    container.profileRepository.completeOnboarding()
                    navController.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                container = container,
                onNavigate = { route -> navController.navigate(route) },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }
        composable(Routes.CONCEPTS) { ConceptsScreen() }
        composable(Routes.MATERIALS) { MaterialsScreen(container) }
        composable(Routes.BLUEPRINTS) { BlueprintsScreen(container) }
        composable(Routes.PROFILE) { ProfileScreen(container, onBack = { navController.popBackStack() }) }
        composable(
            route = Routes.CHAPTER_PATTERN,
            arguments = listOf(navArgument("chapter") { type = androidx.navigation.NavType.IntType })
        ) { backStackEntry ->
            val chapter = backStackEntry.arguments?.getInt("chapter") ?: Routes.CHAPTER_VIGAS
            ChapterListScreen(
                container = container,
                chapter = chapter,
                onOpenChallenge = { challengeId -> navController.navigate(Routes.builder(challengeId)) }
            )
        }
        composable(
            route = Routes.BUILDER_PATTERN,
            arguments = listOf(navArgument("challengeId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId") ?: return@composable
            BuilderScreen(container = container, challengeId = challengeId, onBack = { navController.popBackStack() })
        }
    }
}
