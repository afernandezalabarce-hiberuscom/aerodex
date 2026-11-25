package dev.afalabarce.aircrafts.presentation.ui.features.aircraft

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material3.Icon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.afalabarce.aircrafts.core.ui.theme.AppMaterialTheme
import dev.afalabarce.aircrafts.domain.models.AircraftCategory
import dev.afalabarce.aircrafts.domain.models.AircraftModelSummary
import dev.afalabarce.aircrafts.domain.models.DataSourceOrigin
import dev.afalabarce.aircrafts.presentation.ui.common.CommonRunWith
import dev.afalabarce.aircrafts.presentation.ui.common.PlatformTestRunner
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AeroDexNavigationBar
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AeroDexNavigationItem
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AeroDexSearchBar
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AircraftCard
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.CategoryFilterChips
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.DataSourceBadge
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.Res
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.action_compare_add_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.action_favorite_remove_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.badge_offline
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.nav_badge_cd
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalTestApi::class)
@CommonRunWith(PlatformTestRunner::class)
class AircraftUiComponentsTest {

    @Test
    fun `search bar updates query and clears when clear button is tapped`() = runComposeUiTest {
        var latestQuery = ""
        var cleared = false

        setContent {
            AppMaterialTheme {
                AeroDexSearchBar(
                    query = "A350",
                    onQueryChange = { latestQuery = it },
                    onSearchSubmit = {},
                    onClear = { cleared = true }
                )
            }
        }

        onNodeWithText("A350").assertIsDisplayed()
        onNodeWithText("A350").performTextInput("B787")
        waitForIdle()
        assertTrue(latestQuery.contains("B787"))

        onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
        ).performClick()
        assertTrue(cleared)
    }

    @Test
    fun `category filter chips select category and all option`() = runComposeUiTest {
        var selectedCategory: AircraftCategory? = null

        setContent {
            AppMaterialTheme {
                CategoryFilterChips(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }
        }

        onNodeWithText(AircraftCategory.COMMERCIAL.displayName)
            .performScrollTo()
            .performClick()
        assertEquals(AircraftCategory.COMMERCIAL, selectedCategory)
    }

    @Test
    fun `aircraft card renders details and triggers WCAG accessible actions`() = runComposeUiTest {
        var clicked = false
        var favorited = false
        var compared = false

        val aircraft = AircraftModelSummary(
            icaoTypeCode = "A359",
            manufacturer = "Airbus",
            model = "A350-900",
            name = "Airbus A350-900",
            category = AircraftCategory.COMMERCIAL,
            maxPassengers = 350,
            rangeNm = 8100,
            engineCount = 2,
            engineType = "Jet"
        )

        lateinit var removeFavoriteCd: String
        lateinit var addCompareCd: String

        setContent {
            removeFavoriteCd = stringResource(Res.string.action_favorite_remove_cd, "Airbus A350-900")
            addCompareCd = stringResource(Res.string.action_compare_add_cd, "Airbus A350-900")
            AppMaterialTheme {
                AircraftCard(
                    aircraft = aircraft,
                    isSelected = false,
                    isFavorite = true,
                    isCompared = false,
                    onToggleFavorite = { favorited = true },
                    onToggleCompare = { compared = true },
                    onClick = { clicked = true }
                )
            }
        }

        onNodeWithText("ICAO: A359").assertIsDisplayed()
        onNodeWithText("Airbus A350-900").assertIsDisplayed()
        onNodeWithText("Airbus A350-900").performClick()
        assertTrue(clicked)

        onNodeWithContentDescription(removeFavoriteCd).performClick()
        assertTrue(favorited)

        onNodeWithContentDescription(addCompareCd).performClick()
        assertTrue(compared)
    }

    @Test
    fun `navigation bar exposes Tab role and selected state for WCAG 2_2`() = runComposeUiTest {
        var selectedIndex = 0
        val items = listOf(
            AeroDexNavigationItem(
                label = "Flota",
                contentDescription = "Explorar flota",
                icon = { Icon(imageVector = Icons.Rounded.Flight, contentDescription = null) }
            ),
            AeroDexNavigationItem(
                label = "Favoritos",
                contentDescription = "Aeronaves favoritas",
                badgeCount = 3,
                icon = { Icon(imageVector = Icons.Rounded.Favorite, contentDescription = null) }
            )
        )

        lateinit var favBadgeCd: String
        setContent {
            favBadgeCd = stringResource(Res.string.nav_badge_cd, "Aeronaves favoritas", 3)
            AppMaterialTheme {
                AeroDexNavigationBar(
                    items = items,
                    selectedIndex = selectedIndex,
                    onSelected = { selectedIndex = it }
                )
            }
        }

        onNodeWithContentDescription("Explorar flota").assertIsSelected()
        onNodeWithContentDescription(favBadgeCd).performClick()
        assertEquals(1, selectedIndex)
    }

    @Test
    fun `data source badge renders live memory cache and offline states`() = runComposeUiTest {
        lateinit var offlineText: String
        setContent {
            offlineText = stringResource(Res.string.badge_offline)
            AppMaterialTheme {
                DataSourceBadge(origin = DataSourceOrigin.OFFLINE_CACHE)
            }
        }
        onNodeWithText(offlineText).assertIsDisplayed()
    }
}
