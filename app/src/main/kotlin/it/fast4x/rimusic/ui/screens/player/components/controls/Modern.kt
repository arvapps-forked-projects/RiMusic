package it.fast4x.rimusic.ui.screens.player.components.controls

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.ColorPaletteName
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.PlayerPlayButtonType
import it.fast4x.rimusic.models.Info
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.ui.UiMedia
import it.fast4x.rimusic.query
import it.fast4x.rimusic.service.PlayerService
import it.fast4x.rimusic.ui.components.themed.CustomElevatedButton
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.SelectorDialog
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.collapsedPlayerProgressBar
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.utils.bold
import it.fast4x.rimusic.utils.colorPaletteNameKey
import it.fast4x.rimusic.utils.effectRotationKey
import it.fast4x.rimusic.utils.forceSeekToNext
import it.fast4x.rimusic.utils.getLikedIcon
import it.fast4x.rimusic.utils.getUnlikedIcon
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun InfoAlbumAndArtistModern(
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
){

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
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.90f)
        ) {

                IconButton(
                    icon = if (albumId == null && !media.isLocal) R.drawable.logo_youtube else R.drawable.album,
                    color = if (albumId == null) colorPalette.textDisabled else colorPalette.text,
                    enabled = albumId != null,
                    onClick = {
                        if (albumId != null) {
                            //onGoToAlbum(albumId)
                            navController.navigate(route = "${NavRoutes.album.name}/${albumId}")
                            //layoutState.collapseSoft()
                            onCollapse()
                        }
                    },
                    modifier = Modifier
                        .size(26.dp)
                )

                Spacer(
                    modifier = Modifier
                        .width(8.dp)
                )


            var modifierTitle = Modifier
                .clickable {
                    if (albumId != null) {
                        navController.navigate(route = "${NavRoutes.album.name}/${albumId}")
                        //layoutState.collapseSoft()
                        onCollapse()
                    }
                }
            if (!disableScrollingText) modifierTitle = modifierTitle.basicMarquee()

            BasicText(
                text = title ?: "",
                style = TextStyle(
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
                    .padding(start = 5.dp)
                    .size(24.dp)
            )


    }


    Spacer(
        modifier = Modifier
            .height(10.dp)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
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
                    //onGoToArtist(it)
                    navController.navigate(route = "${NavRoutes.artist.name}/${it}")
                    showSelectDialog = false
                    //layoutState.collapseSoft()
                    onCollapse()
                }
            )



            IconButton(
                icon = if (artistIds?.isEmpty() == true && !media.isLocal) R.drawable.logo_youtube else R.drawable.artists,
                color = if (artistIds?.isEmpty() == true) colorPalette.textDisabled else colorPalette.text,
                onClick = {
                    if (artistIds?.isNotEmpty() == true && artistIds.size > 1)
                        showSelectDialog = true
                    if (artistIds?.isNotEmpty() == true && artistIds.size == 1) {
                        //onGoToArtist( artistIds[0].id )
                        navController.navigate(route = "${NavRoutes.artist.name}/${artistIds[0].id}")
                        //layoutState.collapseSoft()
                        onCollapse()
                    }
                },
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 2.dp)
            )

            Spacer(
                modifier = Modifier
                    .width(12.dp)
            )

        var modifierArtist = Modifier
            .clickable {
                if (artistIds?.isNotEmpty() == true && artistIds.size > 1)
                    showSelectDialog = true
                if (artistIds?.isNotEmpty() == true && artistIds.size == 1) {
                    navController.navigate(route = "${NavRoutes.artist.name}/${artistIds[0].id}")
                    //layoutState.collapseSoft()
                    onCollapse()
                }
            }
        if (!disableScrollingText) modifierArtist = modifierArtist.basicMarquee()
        BasicText(
            text = artist ?: "",
            style = TextStyle(
                color = if (artistIds?.isEmpty() == true) colorPalette.textDisabled else colorPalette.text,
                fontStyle = typography.m.bold.fontStyle,
                fontSize = typography.m.bold.fontSize,
                fontWeight = typography.m.bold.fontWeight,
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
fun ControlsModern(
    binder: PlayerService.Binder,
    position: Long,
    playbackSpeed: Float,
    shouldBePlaying: Boolean,
    playerPlayButtonType: PlayerPlayButtonType,
    rotationAngle: Float,
    isGradientBackgroundEnabled: Boolean,
    onShowSpeedPlayerDialog: () -> Unit,
){


    val (colorPalette, typography) = LocalAppearance.current

    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var isRotated by rememberSaveable { mutableStateOf(false) }

    CustomElevatedButton(
        backgroundColor = colorPalette.background2.copy(alpha = 0.95f),
        onClick = {},
        modifier = Modifier
            .size(55.dp)
            .combinedClickable(
                indication = ripple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    //binder.player.forceSeekToPrevious()
                    binder.player.seekToPrevious()
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                onLongClick = {
                    binder.player.seekTo(position - 5000)
                }
            )

    ) {
        Image(
            painter = painterResource(R.drawable.play_skip_back),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorPalette.text), //ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
            modifier = Modifier
                .padding(10.dp)
                .size(26.dp)
                .rotate(rotationAngle)
        )
    }

    CustomElevatedButton(
        backgroundColor = colorPalette.background2.copy(alpha = 0.95f),
        onClick = {},
        modifier = Modifier
            .combinedClickable(
                indication = ripple(bounded = true),
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
            .width(playerPlayButtonType.width.dp)
            .height(playerPlayButtonType.height.dp)

    ) {
        /*
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
         */

        Image(
            painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorPalette.text), //ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
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

    CustomElevatedButton(
        backgroundColor = colorPalette.background2.copy(alpha = 0.95f),
        onClick = {},
        modifier = Modifier
            .size(55.dp)
            .combinedClickable(
                indication = ripple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    binder.player.forceSeekToNext()
                    if (effectRotationEnabled) isRotated = !isRotated
                },
                onLongClick = {
                    binder.player.seekTo(position + 5000)
                }
            )

    ) {
        Image(
            painter = painterResource(R.drawable.play_skip_forward),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorPalette.text), //ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
            modifier = Modifier
                .padding(10.dp)
                .size(26.dp)
                .rotate(rotationAngle)
        )
    }

}