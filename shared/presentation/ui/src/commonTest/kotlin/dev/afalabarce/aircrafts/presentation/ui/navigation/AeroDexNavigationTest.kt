package dev.afalabarce.aircrafts.presentation.ui.navigation

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dev.afalabarce.aircrafts.core.ui.theme.AppMaterialTheme
import dev.afalabarce.aircrafts.presentation.ui.common.CommonRunWith
import dev.afalabarce.aircrafts.presentation.ui.common.PlatformTestRunner
import dev.afalabarce.aircrafts.presentation.viewmodels.features.aircraft.AeroDexTab
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
@CommonRunWith(PlatformTestRunner::class)
class AeroDexNavigationTest {

    @Test
    fun `tab and route mapping round trips all AeroDexTabs`() {
        AeroDexTab.entries.forEach { tab ->
            assertEquals(tab, tab.toRoute().toTabOrNull())
        }
        assertNull(AeroDexRoute.AircraftDetail("A320").toTabOrNull())
    }

    @Test
    fun `navDisplay navigates between tabs and aircraft detail using rememberNavBackStack`() =
        runComposeUiTest {
            setContent {
                AppMaterialTheme {
                    val backStack = rememberNavBackStack(
                        aeroDexNavSavedStateConfig,
                        AeroDexRoute.Explore
                    )
                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryProvider = entryProvider {
                            entry<AeroDexRoute.Explore> {
                                Button(
                                    onClick = { backStack.add(AeroDexRoute.AircraftDetail("A359")) }
                                ) {
                                    Text("Explore Root")
                                }
                            }
                            entry<AeroDexRoute.Favorites> {
                                Button(
                                    onClick = { navigateToTab(backStack, AeroDexTab.EXPLORE) }
                                ) {
                                    Text("Favorites Tab")
                                }
                            }
                            entry<AeroDexRoute.Comparator> {
                                Text("Comparator Tab")
                            }
                            entry<AeroDexRoute.Profile> {
                                Text("Profile Tab")
                            }
                            entry<AeroDexRoute.AircraftDetail> { route ->
                                Button(
                                    onClick = {
                                        backStack.removeLastOrNull()
                                        navigateToTab(backStack, AeroDexTab.FAVORITES)
                                    }
                                ) {
                                    Text("Detail ${route.icaoTypeCode}")
                                }
                            }
                        }
                    )
                }
            }

            onNodeWithText("Explore Root").assertIsDisplayed().performClick()
            waitForIdle()
            onNodeWithText("Detail A359").assertIsDisplayed().performClick()
            waitForIdle()
            onNodeWithText("Favorites Tab").assertIsDisplayed().performClick()
            waitForIdle()
            onNodeWithText("Explore Root").assertIsDisplayed()
        }

    @Test
    fun `navigateToTab preserves traversal order and pops in reverse order`() =
        runComposeUiTest {
            lateinit var capturedBackStack: androidx.navigation3.runtime.NavBackStack<androidx.navigation3.runtime.NavKey>
            setContent {
                AppMaterialTheme {
                    val backStack = rememberNavBackStack(
                        aeroDexNavSavedStateConfig,
                        AeroDexRoute.Explore
                    )
                    capturedBackStack = backStack
                    NavDisplay(
                        backStack = backStack,
                        onBack = {
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            }
                        },
                        entryProvider = entryProvider {
                            entry<AeroDexRoute.Explore> {
                                Text("Explore Tab")
                            }
                            entry<AeroDexRoute.Profile> {
                                Text("Profile Tab")
                            }
                            entry<AeroDexRoute.Comparator> {
                                Text("Comparator Tab")
                            }
                            entry<AeroDexRoute.Favorites> {
                                Text("Favorites Tab")
                            }
                        }
                    )
                }
            }

            waitForIdle()
            runOnUiThread {
                navigateToTab(capturedBackStack, AeroDexTab.PROFILE)
                navigateToTab(capturedBackStack, AeroDexTab.COMPARATOR)
                navigateToTab(capturedBackStack, AeroDexTab.FAVORITES)
                // Tapping the current tab should not duplicate it
                navigateToTab(capturedBackStack, AeroDexTab.FAVORITES)
            }
            waitForIdle()
            onNodeWithText("Favorites Tab").assertIsDisplayed()
            assertEquals(
                listOf<androidx.navigation3.runtime.NavKey>(
                    AeroDexRoute.Explore,
                    AeroDexRoute.Profile,
                    AeroDexRoute.Comparator,
                    AeroDexRoute.Favorites
                ),
                capturedBackStack.toList()
            )

            runOnUiThread { capturedBackStack.removeLastOrNull() }
            waitForIdle()
            onNodeWithText("Comparator Tab").assertIsDisplayed()

            runOnUiThread { capturedBackStack.removeLastOrNull() }
            waitForIdle()
            onNodeWithText("Profile Tab").assertIsDisplayed()

            runOnUiThread { capturedBackStack.removeLastOrNull() }
            waitForIdle()
            onNodeWithText("Explore Tab").assertIsDisplayed()
        }
}
