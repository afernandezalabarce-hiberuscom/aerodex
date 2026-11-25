package dev.afalabarce.aircrafts.presentation.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.afalabarce.aircrafts.presentation.viewmodels.features.aircraft.AeroDexTab
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
sealed interface AeroDexRoute : NavKey {
    @Serializable
    data object Explore : AeroDexRoute

    @Serializable
    data object Favorites : AeroDexRoute

    @Serializable
    data object Comparator : AeroDexRoute

    @Serializable
    data object Profile : AeroDexRoute

    @Serializable
    data class AircraftDetail(val icaoTypeCode: String) : AeroDexRoute
}

val aeroDexNavSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AeroDexRoute.Explore::class, AeroDexRoute.Explore.serializer())
            subclass(AeroDexRoute.Favorites::class, AeroDexRoute.Favorites.serializer())
            subclass(AeroDexRoute.Comparator::class, AeroDexRoute.Comparator.serializer())
            subclass(AeroDexRoute.Profile::class, AeroDexRoute.Profile.serializer())
            subclass(AeroDexRoute.AircraftDetail::class, AeroDexRoute.AircraftDetail.serializer())
        }
    }
}

fun AeroDexTab.toRoute(): AeroDexRoute = when (this) {
    AeroDexTab.EXPLORE -> AeroDexRoute.Explore
    AeroDexTab.FAVORITES -> AeroDexRoute.Favorites
    AeroDexTab.COMPARATOR -> AeroDexRoute.Comparator
    AeroDexTab.PROFILE -> AeroDexRoute.Profile
}

fun AeroDexRoute.toTabOrNull(): AeroDexTab? = when (this) {
    AeroDexRoute.Explore -> AeroDexTab.EXPLORE
    AeroDexRoute.Favorites -> AeroDexTab.FAVORITES
    AeroDexRoute.Comparator -> AeroDexTab.COMPARATOR
    AeroDexRoute.Profile -> AeroDexTab.PROFILE
    is AeroDexRoute.AircraftDetail -> null
}

internal fun navigateToTab(
    backStack: NavBackStack<NavKey>,
    tab: AeroDexTab
) {
    if (backStack.isEmpty()) {
        backStack.add(AeroDexRoute.Explore)
    }
    val targetRoute = tab.toRoute()
    if (backStack.lastOrNull() != targetRoute) {
        backStack.add(targetRoute)
    }
}
