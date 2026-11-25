package dev.afalabarce.aircrafts.presentation.ui.features.profile.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.afalabarce.aircrafts.core.ui.theme.AppMaterialTheme
import dev.afalabarce.aircrafts.presentation.ui.features.aircraft.components.AeroDexTopBar
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.Res
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.nav_profile_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.profile_avatar
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.profile_avatar_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.profile_email_label
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.profile_email_value
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.profile_github_label
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.profile_github_value
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.profile_linkedin_label
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.profile_linkedin_value
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.profile_name
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.profile_open_link_cd
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.profile_title
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.profile_web_label
import dev.afalabarce.aircrafts.presentation.ui.generated.resources.profile_web_value
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private object ProfileTokens {
    val CardMaxWidth = 540.dp
    val CardCornerRadius = 24.dp
    val HeaderHeight = 88.dp
    val AvatarSize = 128.dp
    val AvatarOverlapOffset = 64.dp
    val AvatarBorderWidth = 4.dp
    val CardBorderWidth = 1.dp
    val DividerThickness = 1.dp
    val IconContainerSize = 40.dp
    val IconSize = 20.dp
    val TrailingIconSize = 18.dp
    val ItemCornerRadius = 14.dp
    const val MailtoPrefix = "mailto:"
    const val HeaderGradientAlpha = 0.22f
    const val BorderAlpha = 0.45f
    const val DividerAlpha = 0.4f
}

@Composable
fun ProfilePane(
    bottomContentPadding: Dp,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    val profileTitle = stringResource(Res.string.profile_title)
    val profileName = stringResource(Res.string.profile_name)
    val avatarContentDescription = stringResource(Res.string.profile_avatar_cd)
    val emailLabel = stringResource(Res.string.profile_email_label)
    val emailValue = stringResource(Res.string.profile_email_value)
    val githubLabel = stringResource(Res.string.profile_github_label)
    val githubValue = stringResource(Res.string.profile_github_value)
    val linkedinLabel = stringResource(Res.string.profile_linkedin_label)
    val linkedinValue = stringResource(Res.string.profile_linkedin_value)
    val webLabel = stringResource(Res.string.profile_web_label)
    val webValue = stringResource(Res.string.profile_web_value)
    val openLinkLabel = stringResource(Res.string.profile_open_link_cd)

    ProfilePaneContent(
        profileTitle = profileTitle,
        profileName = profileName,
        avatarContentDescription = avatarContentDescription,
        emailLabel = emailLabel,
        emailValue = emailValue,
        githubLabel = githubLabel,
        githubValue = githubValue,
        linkedinLabel = linkedinLabel,
        linkedinValue = linkedinValue,
        webLabel = webLabel,
        webValue = webValue,
        openLinkLabel = openLinkLabel,
        bottomContentPadding = bottomContentPadding,
        onOpenUri = { uri ->
            runCatching {
                uriHandler.openUri(uri)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun ProfilePaneContent(
    profileTitle: String,
    profileName: String,
    avatarContentDescription: String,
    emailLabel: String,
    emailValue: String,
    githubLabel: String,
    githubValue: String,
    linkedinLabel: String,
    linkedinValue: String,
    webLabel: String,
    webValue: String,
    openLinkLabel: String,
    bottomContentPadding: Dp,
    onOpenUri: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = AppMaterialTheme.dimens
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        AeroDexTopBar(
            origin = null,
            subtitle = stringResource(Res.string.nav_profile_cd)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = dimens.appMargin, vertical = dimens.minTopSurface)
                .padding(bottom = bottomContentPadding + dimens.bottomSurface),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = ProfileTokens.CardMaxWidth),
                shape = RoundedCornerShape(ProfileTokens.CardCornerRadius),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = ProfileTokens.CardBorderWidth,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(
                        alpha = ProfileTokens.BorderAlpha
                    )
                ),
                tonalElevation = dimens.focusedBorder
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ProfileTokens.HeaderHeight)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(
                                                alpha = ProfileTokens.HeaderGradientAlpha
                                            ),
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = ProfileTokens.HeaderGradientAlpha
                                            )
                                        )
                                    )
                                )
                        )

                        Surface(
                            modifier = Modifier
                                .padding(top = ProfileTokens.HeaderHeight - ProfileTokens.AvatarOverlapOffset)
                                .size(ProfileTokens.AvatarSize),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = ProfileTokens.AvatarBorderWidth,
                                color = MaterialTheme.colorScheme.surface
                            ),
                            tonalElevation = dimens.focusedBorder
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.profile_avatar),
                                contentDescription = avatarContentDescription,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimens.topSurface))

                    Surface(
                        shape = RoundedCornerShape(dimens.textFieldCorner),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = profileTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(
                                horizontal = dimens.startSurface,
                                vertical = dimens.minTopSurface / 2
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(dimens.minTopSurface))

                    Text(
                        text = profileName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = dimens.startSurface)
                            .semantics { heading() }
                    )

                    Spacer(modifier = Modifier.height(dimens.topSurface))

                    HorizontalDivider(
                        thickness = ProfileTokens.DividerThickness,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(
                            alpha = ProfileTokens.DividerAlpha
                        ),
                        modifier = Modifier.padding(horizontal = dimens.startSurface)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = dimens.startSurface,
                                vertical = dimens.topSurface
                            ),
                        verticalArrangement = Arrangement.spacedBy(dimens.minTopSurface)
                    ) {
                        ProfileContactItem(
                            icon = Icons.Rounded.Email,
                            label = emailLabel,
                            value = emailValue,
                            openLinkLabel = openLinkLabel,
                            onClick = { onOpenUri("${ProfileTokens.MailtoPrefix}$emailValue") }
                        )

                        ProfileContactItem(
                            icon = Icons.Rounded.Code,
                            label = githubLabel,
                            value = githubValue,
                            openLinkLabel = openLinkLabel,
                            onClick = { onOpenUri(githubValue) }
                        )

                        ProfileContactItem(
                            icon = Icons.Rounded.Badge,
                            label = linkedinLabel,
                            value = linkedinValue,
                            openLinkLabel = openLinkLabel,
                            onClick = { onOpenUri(linkedinValue) }
                        )

                        ProfileContactItem(
                            icon = Icons.Rounded.Language,
                            label = webLabel,
                            value = webValue,
                            openLinkLabel = openLinkLabel,
                            onClick = { onOpenUri(webValue) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileContactItem(
    icon: ImageVector,
    label: String,
    value: String,
    openLinkLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = AppMaterialTheme.dimens
    val itemShape = RoundedCornerShape(ProfileTokens.ItemCornerRadius)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = dimens.buttonHeight)
            .clip(itemShape)
            .clickable(
                onClickLabel = openLinkLabel,
                onClick = onClick
            )
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "$label: $value"
            },
        shape = itemShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = ProfileTokens.BorderAlpha
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimens.startSurface,
                    vertical = dimens.minTopSurface
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.minStartSurface)
        ) {
            Box(
                modifier = Modifier
                    .size(ProfileTokens.IconContainerSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(ProfileTokens.IconSize)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimens.none)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(ProfileTokens.TrailingIconSize)
            )
        }
    }
}
