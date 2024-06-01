package it.fast4x.rimusic.ui.screens.player.components.controls

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.ColorPaletteName
import it.fast4x.rimusic.enums.IconLikeType
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.PlayerPlayButtonType
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Info
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.ui.UiMedia
import it.fast4x.rimusic.query
import it.fast4x.rimusic.service.PlayerService
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.SelectorDialog
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.collapsedPlayerProgressBar
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.utils.UiTypeKey
import it.fast4x.rimusic.utils.bold
import it.fast4x.rimusic.utils.colorPaletteNameKey
import it.fast4x.rimusic.utils.effectRotationKey
import it.fast4x.rimusic.utils.forceSeekToNext
import it.fast4x.rimusic.utils.getLikedIcon
import it.fast4x.rimusic.utils.getUnlikedIcon
import it.fast4x.rimusic.utils.iconLikeTypeKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.trackLoopEnabledKey

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun InfoAlbumAndArtistEssential(
    binder: PlayerService.Binder,
    navController: NavController,
    albumId: String?,
    media: UiMedia,
    mediaId: String,
    title: String?,
    likedAt: Long?,
    artistIds: List<Info>?,
    artist: String?,
    onCollapse: () -> Unit,
    disableScrollingText: Boolean = false
) {

    val (colorPalette, typography) = LocalAppearance.current
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var isRotated by rememberSaveable { mutableStateOf(false) }
    var showSelectDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {


            var modifierTitle = Modifier
                .clickable {
                    if (albumId != null) {
                        navController.navigate(route = "${NavRoutes.album.name}/${albumId}")
                        onCollapse()
                    }
                }
            if (!disableScrollingText) modifierTitle = modifierTitle.basicMarquee()

            BasicText(
                text = title ?: "",
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = if (albumId == null) colorPalette.textDisabled else colorPalette.text,
                    fontStyle = typography.xl.bold.fontStyle,
                    fontWeight = typography.xl.bold.fontWeight,
                    fontSize = typography.xl.bold.fontSize,
                    fontFamily = typography.xl.bold.fontFamily
                ),
                maxLines = 1,
                modifier = modifierTitle
            )
            //}
        }

    }


    Spacer(
        modifier = Modifier
            .height(10.dp)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
    ) {

        if (showSelectDialog)
            SelectorDialog(
                title = stringResource(R.string.artists),
                onDismiss = { showSelectDialog = false },
                values = artistIds,
                onValueSelected = {
                    navController.navigate(route = "${NavRoutes.artist.name}/${it}")
                    showSelectDialog = false
                    onCollapse()
                }
            )


        var modifierArtist = Modifier
            .clickable {
                if (artistIds?.isNotEmpty() == true && artistIds.size > 1)
                    showSelectDialog = true
                if (artistIds?.isNotEmpty() == true && artistIds.size == 1) {
                    navController.navigate(route = "${NavRoutes.artist.name}/${artistIds[0].id}")
                    onCollapse()
                }
            }
        if (!disableScrollingText) modifierArtist = modifierArtist.basicMarquee()
        BasicText(
            text = artist ?: "",
            style = TextStyle(
                textAlign = TextAlign.Center,
                color = if (artistIds?.isEmpty() == true) colorPalette.textDisabled else colorPalette.text,
                fontStyle = typography.m.bold.fontStyle,
                fontSize = typography.m.bold.fontSize,
                //fontWeight = typography.m.bold.fontWeight,
                fontFamily = typography.m.bold.fontFamily
            ),
            maxLines = 1,
            modifier = modifierArtist

        )

    }

}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ControlsEssential(
    binder: PlayerService.Binder,
    position: Long,
    playbackSpeed: Float,
    shouldBePlaying: Boolean,
    likedAt: Long?,
    mediaId: String,
    playerPlayButtonType: PlayerPlayButtonType,
    rotationAngle: Float,
    isGradientBackgroundEnabled: Boolean,
    onShowSpeedPlayerDialog: () -> Unit,
) {


    val (colorPalette, typography) = LocalAppearance.current
    val colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.ModernBlack)
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var isRotated by rememberSaveable { mutableStateOf(false) }
    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")
    val playPauseRoundness by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it) 32.dp else 16.dp }
    )

    var trackLoopEnabled by rememberPreference(trackLoopEnabledKey, defaultValue = false)

    IconButton(
        color = colorPalette.favoritesIcon,
        icon = if (likedAt == null) getUnlikedIcon() else getLikedIcon(),
        onClick = {
            val currentMediaItem = binder.player.currentMediaItem
            query {
                if (Database.like(
                        mediaId,
                        if (likedAt == null) System.currentTimeMillis() else null
                    ) == 0
                ) {
                    currentMediaItem
                        ?.takeIf { it.mediaId == mediaId }
                        ?.let {
                            Database.insert(currentMediaItem, Song::toggleLike)
                        }
                }
            }
            if (effectRotationEnabled) isRotated = !isRotated
        },
        modifier = Modifier
            .padding(10.dp)
            .size(26.dp)
    )

    Image(
        painter = painterResource(R.drawable.play_skip_back),
        contentDescription = null,
        colorFilter = ColorFilter.tint(colorPalette.text),
        modifier = Modifier
            .combinedClickable(
                indication = ripple(bounded = false),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    binder.player.seekToPrevious()
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                onLongClick = {
                    binder.player.seekTo(position - 5000)
                }
            )
            .rotate(rotationAngle)
            .padding(10.dp)
            .size(26.dp)

    )



    Box(
        modifier = Modifier
            .combinedClickable(
                indication = ripple(bounded = false),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    if (shouldBePlaying) {
                        binder.player.pause()
                    } else {
                        if (binder.player.playbackState == Player.STATE_IDLE) {
                            binder.player.prepare()
                        }
                        binder.player.play()
                    }
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                onLongClick = onShowSpeedPlayerDialog
            )
            .clip(RoundedCornerShape(playPauseRoundness))
            .background(
                when (colorPaletteName) {
                    ColorPaletteName.Dynamic, ColorPaletteName.Default,
                    ColorPaletteName.MaterialYou, ColorPaletteName.Customized -> {
                        when (playerPlayButtonType) {
                            PlayerPlayButtonType.CircularRibbed -> {
                                if (isGradientBackgroundEnabled) colorPalette.background2
                                else colorPalette.background1
                            }

                            PlayerPlayButtonType.Disabled -> colorPalette.background1
                            else -> {
                                if (isGradientBackgroundEnabled) colorPalette.background1
                                else colorPalette.background2
                            }
                        }
                    }

                    ColorPaletteName.PureBlack, ColorPaletteName.ModernBlack ->
                        if (playerPlayButtonType == PlayerPlayButtonType.CircularRibbed)
                            colorPalette.background1 else
                            if (playerPlayButtonType != PlayerPlayButtonType.Disabled)
                                colorPalette.background4 else colorPalette.background0
                }
            )
            .width(playerPlayButtonType.width.dp)
            .height(playerPlayButtonType.height.dp)

    ) {

        if (playerPlayButtonType == PlayerPlayButtonType.CircularRibbed)
            Image(
                painter = painterResource(R.drawable.a13shape),
                colorFilter = ColorFilter.tint(
                    when (colorPaletteName) {
                        ColorPaletteName.PureBlack, ColorPaletteName.ModernBlack -> colorPalette.background4
                        else -> if (isGradientBackgroundEnabled) colorPalette.background1
                        else colorPalette.background2
                    }
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotationAngle),
                contentDescription = "Background Image",
                contentScale = ContentScale.Fit
            )

        Image(
            painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorPalette.text),
            modifier = Modifier
                .rotate(rotationAngle)
                .align(Alignment.Center)
                .size(30.dp)
        )

        val fmtSpeed = "%.1fx".format(playbackSpeed).replace(",", ".")
        if (fmtSpeed != "1.0x")
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)

            ) {
                BasicText(
                    text = fmtSpeed,
                    style = TextStyle(
                        color = colorPalette.text,
                        fontStyle = typography.xxxs.semiBold.fontStyle,
                        fontSize = typography.xxxs.semiBold.fontSize
                    ),
                    maxLines = 1,
                    modifier = Modifier
                        .padding(bottom = if (playerPlayButtonType != PlayerPlayButtonType.CircularRibbed) 5.dp else 15.dp)
                )
            }
    }




    Image(
        painter = painterResource(R.drawable.play_skip_forward),
        contentDescription = null,
        colorFilter = ColorFilter.tint(colorPalette.text),
        modifier = Modifier
            .combinedClickable(
                indication = ripple(bounded = false),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    binder.player.forceSeekToNext()
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                onLongClick = {
                    binder.player.seekTo(position + 5000)
                }
            )
            .rotate(rotationAngle)
            .padding(10.dp)
            .size(26.dp)

    )



    IconButton(
        icon = R.drawable.repeat,
        color = if (trackLoopEnabled) colorPalette.iconButtonPlayer else colorPalette.textDisabled,
        onClick = {
            trackLoopEnabled = !trackLoopEnabled
        },
        modifier = Modifier
            .padding(10.dp)
            .size(26.dp)
    )

}