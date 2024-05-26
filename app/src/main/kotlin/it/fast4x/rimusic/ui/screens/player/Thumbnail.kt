package it.fast4x.rimusic.ui.screens.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.ClickLyricsText
import it.fast4x.rimusic.enums.PlayerControlsType
import it.fast4x.rimusic.enums.TransitionEffect
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.service.LoginRequiredException
import it.fast4x.rimusic.service.MyDownloadService
import it.fast4x.rimusic.service.PlayableFormatNonSupported
import it.fast4x.rimusic.service.PlayableFormatNotFoundException
import it.fast4x.rimusic.service.PlayerService
import it.fast4x.rimusic.service.UnplayableException
import it.fast4x.rimusic.service.VideoIdMismatchException
import it.fast4x.rimusic.service.isLocal
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.ui.styling.favoritesOverlay
import it.fast4x.rimusic.ui.styling.overlay
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.DisposableListener
import it.fast4x.rimusic.utils.UiTypeKey
import it.fast4x.rimusic.utils.clickLyricsTextKey
import it.fast4x.rimusic.utils.currentWindow
import it.fast4x.rimusic.utils.doubleShadowDrop
import it.fast4x.rimusic.utils.dropShadow
import it.fast4x.rimusic.utils.fadingEdge
import it.fast4x.rimusic.utils.intent
import it.fast4x.rimusic.utils.playerControlsTypeKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.resize
import it.fast4x.rimusic.utils.thumbnail
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

@ExperimentalAnimationApi
@UnstableApi
@Composable
fun Thumbnail(
    thumbnailTapEnabledKey: Boolean,
    isShowingLyrics: Boolean,
    onShowLyrics: (Boolean) -> Unit,
    isShowingStatsForNerds: Boolean,
    onShowStatsForNerds: (Boolean) -> Unit,
    isShowingEqualizer: Boolean,
    onShowEqualizer: (Boolean) -> Unit,
    onMaximize: () -> Unit,
    onDoubleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val player = binder?.player ?: return

    val (thumbnailSizeDp, thumbnailSizePx) = Dimensions.thumbnails.player.song.let {
        it to (it - 64.dp).px
    }

    var nullableWindow by remember {
        mutableStateOf(player.currentWindow)
    }

    var error by remember {
        mutableStateOf<PlaybackException?>(player.playerError)
    }

    val localMusicFileNotFoundError = stringResource(R.string.error_local_music_not_found)
    val networkerror = stringResource(R.string.error_a_network_error_has_occurred)
    val notfindplayableaudioformaterror =
        stringResource(R.string.error_couldn_t_find_a_playable_audio_format)
    val originalvideodeletederror =
        stringResource(R.string.error_the_original_video_source_of_this_song_has_been_deleted)
    val songnotplayabledueserverrestrictionerror =
        stringResource(R.string.error_this_song_cannot_be_played_due_to_server_restrictions)
    val videoidmismatcherror =
        stringResource(R.string.error_the_returned_video_id_doesn_t_match_the_requested_one)
    val unknownplaybackerror =
        stringResource(R.string.error_an_unknown_playback_error_has_occurred) + " " +
                stringResource(R.string.restart_app_please)

    val formatUnsupported = "This file seems to have an unsupported format"

    var artImageAvailable by remember {
        mutableStateOf(true)
    }

    val clickLyricsText by rememberPreference(clickLyricsTextKey, ClickLyricsText.FullScreen)

    player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableWindow = player.currentWindow
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                error = player.playerError
            }

            override fun onPlayerError(playbackException: PlaybackException) {
                error = playbackException
                binder.stopRadio()
                context.stopService(context.intent<PlayerService>())
                context.stopService(context.intent<MyDownloadService>())
            }
        }
    }

    val window = nullableWindow ?: return

    AnimatedContent(
        targetState = window,
        transitionSpec = {
            val duration = 500
            val slideDirection = if (targetState.firstPeriodIndex > initialState.firstPeriodIndex)
                AnimatedContentTransitionScope.SlideDirection.Left
            else AnimatedContentTransitionScope.SlideDirection.Right

            ContentTransform(
                targetContentEnter = slideIntoContainer(
                    towards = slideDirection,
                    animationSpec = tween(duration)
                ) + fadeIn(
                    animationSpec = tween(duration)
                ) + scaleIn(
                    initialScale = 0.85f,
                    animationSpec = tween(duration)
                ),
                initialContentExit = slideOutOfContainer(
                    towards = slideDirection,
                    animationSpec = tween(duration)
                ) + fadeOut(
                    animationSpec = tween(duration)
                ) + scaleOut(
                    targetScale = 0.85f,
                    animationSpec = tween(duration)
                ),
                sizeTransform = SizeTransform(clip = false)
            )
        },
        contentAlignment = Alignment.Center, label = ""
    ) { currentWindow ->

        val playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Modern)
        var modifierUiType by remember { mutableStateOf(modifier) }
        if (playerControlsType == PlayerControlsType.Modern)
            modifierUiType = modifier
                .aspectRatio(1f)
                //.size(thumbnailSizeDp)
                .fillMaxSize()
                //.dropShadow(LocalAppearance.current.thumbnailShape, LocalAppearance.current.colorPalette.overlay.copy(0.1f), 6.dp, 2.dp, 2.dp)
                //.dropShadow(LocalAppearance.current.thumbnailShape, LocalAppearance.current.colorPalette.overlay.copy(0.1f), 6.dp, (-2).dp, (-2).dp)
                .doubleShadowDrop(LocalAppearance.current.thumbnailShape, 4.dp, 8.dp)
                .clip(LocalAppearance.current.thumbnailShape)
                //.padding(14.dp)
        else modifierUiType = modifier
            .aspectRatio(1f)
            //.size(thumbnailSizeDp)
            .padding(14.dp)
            .fillMaxSize()
            .clip(LocalAppearance.current.thumbnailShape)


        Box(
            modifier = modifierUiType
        ) {
            if(artImageAvailable)
                AsyncImage(
                    /*
                    model = currentWindow.mediaItem.mediaMetadata.artworkUri.thumbnail(
                        thumbnailSizePx
                    ),
                     */
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentWindow.mediaItem.mediaMetadata.artworkUri.toString().resize(1200, 1200))
                        .size(Size.ORIGINAL)
                        .scale(Scale.FIT)
                        .build(),
                    onSuccess = {
                        artImageAvailable = true
                    },
                    onError = {
                        artImageAvailable = false
                    },
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { onShowStatsForNerds(true) },
                                onTap = if (thumbnailTapEnabledKey) {
                                    {
                                        onShowLyrics(true)
                                        onShowEqualizer(false)
                                    }
                                } else null,
                                onDoubleTap = { onDoubleTap() }
                            )

                        }
                        .fillMaxSize()
                        .clip(LocalAppearance.current.thumbnailShape)


                )

            if(!artImageAvailable)
                Image(
                    painter = painterResource(R.drawable.app_icon),
                    colorFilter = ColorFilter.tint(LocalAppearance.current.colorPalette.accent),
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { onShowStatsForNerds(true) },
                                onTap = if (thumbnailTapEnabledKey) {
                                    {
                                        onShowLyrics(true)
                                        onShowEqualizer(false)
                                    }
                                } else null,
                                onDoubleTap = { onDoubleTap() }
                            )

                        }
                        .fillMaxSize()
                        .clip(LocalAppearance.current.thumbnailShape),
                    contentDescription = "Background Image",
                    contentScale = ContentScale.Fit
                )

            //if (!currentWindow.mediaItem.isLocal)
                Lyrics(
                mediaId = currentWindow.mediaItem.mediaId,
                isDisplayed = isShowingLyrics && error == null,
                onDismiss = {
                    if (thumbnailTapEnabledKey) onShowLyrics(false)
                },
                ensureSongInserted = { Database.insert(currentWindow.mediaItem) },
                size = thumbnailSizeDp,
                mediaMetadataProvider = currentWindow.mediaItem::mediaMetadata,
                durationProvider = player::getDuration,
                onMaximize = onMaximize,
                enableClick = when (clickLyricsText) {
                    ClickLyricsText.Player, ClickLyricsText.Both -> true
                    else -> false
                }
            )

            StatsForNerds(
                mediaId = currentWindow.mediaItem.mediaId,
                isDisplayed = isShowingStatsForNerds && error == null,
                onDismiss = { onShowStatsForNerds(false) }
            )


            ShowVisualizer(
                isDisplayed = isShowingEqualizer && error == null
            )

            PlaybackError(
                isDisplayed = error != null,
                messageProvider = {
                    if (currentWindow.mediaItem.isLocal) localMusicFileNotFoundError
                    else when (error?.cause?.cause) {
                        is UnresolvedAddressException, is UnknownHostException -> networkerror
                        is PlayableFormatNotFoundException -> notfindplayableaudioformaterror
                        is UnplayableException -> originalvideodeletederror
                        is LoginRequiredException -> songnotplayabledueserverrestrictionerror
                        is VideoIdMismatchException -> videoidmismatcherror
                        is PlayableFormatNonSupported -> formatUnsupported
                        else -> unknownplaybackerror
                    }
                },
                onDismiss = {
                    //player::prepare
                    //player.stop()
                    player.seekToNext()
                }
            )
        }
    }
}
