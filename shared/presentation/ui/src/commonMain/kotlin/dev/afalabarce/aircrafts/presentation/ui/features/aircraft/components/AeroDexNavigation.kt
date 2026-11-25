@file:Suppress("MatchingDeclarationName")

package dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.afalabarce.aircrafts.core.ui.theme.AppMaterialTheme
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.Res
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.nav_badge_cd
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource

data class AeroDexNavigationItem(
    val label: String,
    val contentDescription: String,
    val badgeCount: Int = 0,
    val icon: @Composable () -> Unit
)

private object FloatingNavTokens {
    val HorizontalMargin = 20.dp
    val BottomMargin = 12.dp
    val ExpandedHeight = 68.dp
    val CompactHeight = 52.dp
    val BarHorizontalPadding = 12.dp
    val CompactBarHorizontalPadding = 10.dp
    val BubbleInset = 6.dp
    val CompactBubbleInset = 4.dp
    val BorderWidth = 1.dp
    val MinItemWidth = 52.dp
    val CompactItemWidth = 60.dp
    val LabelSpacer = 2.dp
    val ItemSpacing = 4.dp
    val MinTouchTarget = 48.dp
    val MinBubbleSize = 36.dp
    val RailMinWidth = 80.dp
    const val CompactIconScale = 0.90f
    const val FullIconScale = 1f
    val Clearance = 92.dp
    const val ScrollThresholdPx = 12
    const val TopResetOffsetPx = 24
    const val NavSurfaceAlpha = 0.96f
    const val SingleLine = 1
    const val PairFactor = 2
}

val AeroDexFloatingBarClearance = FloatingNavTokens.Clearance

@Composable
fun rememberLazyGridScrollDownState(gridState: LazyGridState): Boolean {
    var isScrollingDown by remember { mutableStateOf(false) }
    var previousIndex by remember { mutableIntStateOf(gridState.firstVisibleItemIndex) }
    var previousOffset by remember { mutableIntStateOf(gridState.firstVisibleItemScrollOffset) }

    LaunchedEffect(gridState) {
        snapshotFlow {
            (gridState.firstVisibleItemIndex.toLong() shl 32) or
                (gridState.firstVisibleItemScrollOffset.toLong() and 0xFFFF_FFFFL)
        }
            .distinctUntilChanged()
            .collect { packed ->
                val index = (packed ushr 32).toInt()
                val offset = packed.toInt()
                if (index == 0 && offset <= FloatingNavTokens.TopResetOffsetPx) {
                    isScrollingDown = false
                } else if (index > previousIndex) {
                    isScrollingDown = true
                } else if (index < previousIndex) {
                    isScrollingDown = false
                } else {
                    val delta = offset - previousOffset
                    if (delta > FloatingNavTokens.ScrollThresholdPx) {
                        isScrollingDown = true
                    } else if (delta < -FloatingNavTokens.ScrollThresholdPx) {
                        isScrollingDown = false
                    }
                }
                previousIndex = index
                previousOffset = offset
            }
    }

    return isScrollingDown
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AeroDexNavigationBar(
    items: List<AeroDexNavigationItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val navSurface = AppMaterialTheme.colorScheme.surfaceContainerHigh.copy(
        alpha = FloatingNavTokens.NavSurfaceAlpha
    )
    val navBorder = AppMaterialTheme.colorScheme.outlineVariant
    val indicatorColor = AppMaterialTheme.colorScheme.primaryContainer
    val selectedContentColor = AppMaterialTheme.colorScheme.onPrimaryContainer
    val unselectedContentColor = AppMaterialTheme.colorScheme.onSurfaceVariant

    val pillShape = CircleShape

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = FloatingNavTokens.BottomMargin),
        contentAlignment = Alignment.Center
    ) {
        val itemCount = items.size.coerceAtLeast(1)
        val expandedBarWidth = (maxWidth - (FloatingNavTokens.HorizontalMargin * 2))
            .coerceAtLeast(FloatingNavTokens.MinItemWidth * itemCount)
        val compactBarWidth = ((FloatingNavTokens.CompactItemWidth * itemCount) +
            (FloatingNavTokens.CompactBarHorizontalPadding * 2))
            .coerceAtMost(expandedBarWidth)

        val spatialSpec = AppMaterialTheme.motionScheme.defaultSpatialSpec<Dp>()
        val floatSpec = AppMaterialTheme.motionScheme.defaultSpatialSpec<Float>()
        val barWidth by animateDpAsState(
            targetValue = if (isCompact) compactBarWidth else expandedBarWidth,
            animationSpec = spatialSpec
        )
        val barHeight by animateDpAsState(
            targetValue = if (isCompact) FloatingNavTokens.CompactHeight else FloatingNavTokens.ExpandedHeight,
            animationSpec = spatialSpec
        )
        val barHorizontalPadding by animateDpAsState(
            targetValue = if (isCompact) FloatingNavTokens.CompactBarHorizontalPadding else FloatingNavTokens.BarHorizontalPadding,
            animationSpec = spatialSpec
        )
        val bubbleInset by animateDpAsState(
            targetValue = if (isCompact) FloatingNavTokens.CompactBubbleInset else FloatingNavTokens.BubbleInset,
            animationSpec = spatialSpec
        )
        val iconScale by animateFloatAsState(
            targetValue = if (isCompact) FloatingNavTokens.CompactIconScale else 1f,
            animationSpec = floatSpec
        )

        val innerWidth = (barWidth - (barHorizontalPadding * 2)).coerceAtLeast(0.dp)
        val slotWidth = innerWidth / itemCount
        val safeSelectedIndex = selectedIndex.coerceIn(0, itemCount - 1)
        val animatedSlotOffset by animateDpAsState(
            targetValue = slotWidth * safeSelectedIndex,
            animationSpec = spatialSpec
        )
        val bubbleWidth = (slotWidth - AppMaterialTheme.dimens.minStartSurface)
            .coerceAtLeast(FloatingNavTokens.MinBubbleSize)
        val bubbleHeight = (barHeight - (bubbleInset * FloatingNavTokens.PairFactor))
            .coerceAtLeast(FloatingNavTokens.MinBubbleSize)
        val bubbleOffsetX = animatedSlotOffset + ((slotWidth - bubbleWidth) / 2)

        Surface(
            modifier = Modifier
                .width(barWidth)
                .height(barHeight)
                .clip(pillShape),
            shape = pillShape,
            color = navSurface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(
                width = FloatingNavTokens.BorderWidth,
                color = navBorder
            ),
            shadowElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = barHorizontalPadding)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset { IntOffset(bubbleOffsetX.roundToPx(), 0) }
                        .size(width = bubbleWidth, height = bubbleHeight)
                        .clip(pillShape)
                        .background(indicatorColor)
                )
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        AeroDexFloatingBarItem(
                            item = item,
                            isSelected = index == selectedIndex,
                            isCompact = isCompact,
                            iconScale = iconScale,
                            selectedColor = selectedContentColor,
                            unselectedColor = unselectedContentColor,
                            onClick = { onSelected(index) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AeroDexFloatingBarItem(
    item: AeroDexNavigationItem,
    isSelected: Boolean,
    isCompact: Boolean,
    iconScale: Float,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else unselectedColor,
        animationSpec = AppMaterialTheme.motionScheme.fastEffectsSpec()
    )
    val interactionSource = remember { MutableInteractionSource() }
    val badgeAccessibilityText = if (item.badgeCount > 0) {
        stringResource(Res.string.nav_badge_cd, item.contentDescription, item.badgeCount)
    } else {
        item.contentDescription
    }

    Column(
        modifier = modifier
            .defaultMinSize(
                minWidth = FloatingNavTokens.MinTouchTarget,
                minHeight = AppMaterialTheme.dimens.minTouchTarget
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = FloatingNavTokens.ItemSpacing, vertical = 4.dp)
            .semantics {
                contentDescription = badgeAccessibilityText
                selected = isSelected
                role = Role.Tab
                if (item.badgeCount > 0) {
                    stateDescription = item.badgeCount.toString()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = iconScale
                scaleY = iconScale
            }
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                if (item.badgeCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ) {
                                Text(text = item.badgeCount.toString())
                            }
                        }
                    ) {
                        item.icon()
                    }
                } else {
                    item.icon()
                }
            }
        }

        AnimatedVisibility(
            visible = !isCompact,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(FloatingNavTokens.LabelSpacer))

                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AeroDexNavigationRail(
    items: List<AeroDexNavigationItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier
            .navigationBarsPadding()
            .widthIn(min = FloatingNavTokens.RailMinWidth),
        containerColor = AppMaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = AppMaterialTheme.colorScheme.onSurface
    ) {
        Spacer(modifier = Modifier.height(AppMaterialTheme.dimens.spacingLg))
        items.forEachIndexed { index, item ->
            val badgeAccessibilityText = if (item.badgeCount > 0) {
                stringResource(Res.string.nav_badge_cd, item.contentDescription, item.badgeCount)
            } else {
                item.contentDescription
            }
            NavigationRailItem(
                modifier = Modifier.semantics {
                    contentDescription = badgeAccessibilityText
                },
                selected = index == selectedIndex,
                onClick = { onSelected(index) },
                icon = {
                    if (item.badgeCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = AppMaterialTheme.colorScheme.tertiary,
                                    contentColor = AppMaterialTheme.colorScheme.onTertiary
                                ) { Text(text = item.badgeCount.toString()) }
                            }
                        ) {
                            item.icon()
                        }
                    } else {
                        item.icon()
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = AppMaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = AppMaterialTheme.colorScheme.primary,
                    indicatorColor = AppMaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = AppMaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = AppMaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
