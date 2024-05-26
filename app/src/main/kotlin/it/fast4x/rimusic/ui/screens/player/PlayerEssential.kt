package it.fast4x.rimusic.ui.screens.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.BottomNavigationDefaults.windowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.BackgroundProgress
import it.fast4x.rimusic.ui.components.themed.SmartToast
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.favoritesOverlay
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.DisposableListener
import it.fast4x.rimusic.utils.backgroundProgressKey
import it.fast4x.rimusic.utils.disableClosingPlayerSwipingDownKey
import it.fast4x.rimusic.utils.effectRotationKey
import it.fast4x.rimusic.utils.forceSeekToNext
import it.fast4x.rimusic.utils.forceSeekToPrevious
import it.fast4x.rimusic.utils.mediaItemToggleLike
import it.fast4x.rimusic.utils.positionAndDurationState
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.shouldBePlaying
import it.fast4x.rimusic.utils.thumbnail
import kotlinx.coroutines.flow.distinctUntilChanged

import kotlin.math.absoluteValue

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerEssential(
    showPlayer: () -> Unit,
    hidePlayer: () -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    val context = LocalContext.current

    var nullableMediaItem by remember {
        mutableStateOf(
            binder.player.currentMediaItem,
            neverEqualPolicy()
        )
    }
    var shouldBePlaying by remember { mutableStateOf(binder.player.shouldBePlaying) }

    binder.player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableMediaItem = mediaItem
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                shouldBePlaying = binder.player.shouldBePlaying
            }
        }
    }

    val mediaItem = nullableMediaItem ?: return

    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    LaunchedEffect(mediaItem.mediaId) {
        Database.likedAt(mediaItem.mediaId).distinctUntilChanged().collect { likedAt = it }
    }

    var updateLike by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(updateLike) {
        if (updateLike) {
            mediaItemToggleLike(mediaItem)
            updateLike = false
            if (likedAt == null) SmartToast(context.getString(R.string.added_to_favorites)) 
            else SmartToast(context.getString(R.string.removed_from_favorites))
        }
    }

    val positionAndDuration by binder.player.positionAndDurationState()

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) updateLike = true
            else if (value == SwipeToDismissBoxValue.EndToStart) binder.player.forceSeekToNext()

            return@rememberSwipeToDismissBoxState false
        }
    )
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val backgroundProgress by rememberPreference(backgroundProgressKey, BackgroundProgress.MiniPlayer)
    val effectRotationEnabled by rememberPreference(effectRotationKey, true)
    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")
    val playPauseRoundness by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it) 24.dp else 12.dp }
    )

    var isRotated by rememberSaveable { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 360F else 0f,
        animationSpec = tween(durationMillis = 200), label = ""
    )
    val disableClosingPlayerSwipingDown by rememberPreference(disableClosingPlayerSwipingDownKey, true)

    SwipeToDismissBox(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp)),
        state = dismissState,
        backgroundContent = {
            /*
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primaryContainer
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                label = "background"
            )
             */

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorPalette.background1)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Arrangement.Start
                    SwipeToDismissBoxValue.EndToStart -> Arrangement.End
                    SwipeToDismissBoxValue.Settled -> Arrangement.Center
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.StartToEnd -> { if (likedAt == null)
                            ImageVector.vectorResource(R.drawable.heart_outline)
                            else ImageVector.vectorResource(R.drawable.heart)
                        }
                        SwipeToDismissBoxValue.EndToStart ->  ImageVector.vectorResource(R.drawable.play_skip_forward)
                        SwipeToDismissBoxValue.Settled ->  ImageVector.vectorResource(R.drawable.play)
                    },
                    contentDescription = null,
                    tint = colorPalette.iconButtonPlayer,
                )
            }
        }
    ) {

        /***** */
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .clickable(onClick = showPlayer)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            if (dragAmount < 0) showPlayer()
                            else if (dragAmount > 20) {
                                if (!disableClosingPlayerSwipingDown) {
                                    binder.stopRadio()
                                    binder.player.clearMediaItems()
                                    hidePlayer()
                                } else SmartToast(context.getString(R.string.player_swiping_down_is_disabled))
                            }
                        }
                    )
                }
                .background(colorPalette.background2)
                .fillMaxWidth()
                .drawBehind {
                    if (backgroundProgress == BackgroundProgress.Both || backgroundProgress == BackgroundProgress.MiniPlayer) {
                        drawRect(
                            color = colorPalette.favoritesOverlay,
                            topLeft = Offset.Zero,
                            size = Size(
                                width = positionAndDuration.first.toFloat() /
                                        positionAndDuration.second.absoluteValue * size.width,
                                height = size.maxDimension
                            )
                        )
                    }
                }
        ) {

            Spacer(
                modifier = Modifier
                    .width(2.dp)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(Dimensions.collapsedPlayer)
            ) {
                AsyncImage(
                    model = mediaItem.mediaMetadata.artworkUri.thumbnail(Dimensions.thumbnails.song.px),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(thumbnailShape)
                        .size(48.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .height(Dimensions.collapsedPlayer)
                    .weight(1f)
            ) {
                BasicText(
                    text = mediaItem.mediaMetadata.title?.toString() ?: "",
                    style = typography.xxs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                BasicText(
                    text = mediaItem.mediaMetadata.artist?.toString() ?: "",
                    style = typography.xxs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(
                modifier = Modifier
                    .width(2.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(Dimensions.collapsedPlayer)
            ) {
                it.fast4x.rimusic.ui.components.themed.IconButton(
                    icon = R.drawable.play_skip_back,
                    color = colorPalette.iconButtonPlayer,
                    onClick = {
                        binder.player.forceSeekToPrevious()
                        if (effectRotationEnabled) isRotated = !isRotated
                    },
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .padding(horizontal = 2.dp, vertical = 8.dp)
                        .size(24.dp)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(playPauseRoundness))
                        .clickable {
                            if (shouldBePlaying) {
                                binder.player.pause()
                            } else {
                                if (binder.player.playbackState == Player.STATE_IDLE) {
                                    binder.player.prepare()
                                }
                                binder.player.play()
                            }
                            if (effectRotationEnabled) isRotated = !isRotated
                        }
                        .background(colorPalette.background2)
                        .size(42.dp)
                ) {
                    Image(
                        painter = painterResource(if (shouldBePlaying) R.drawable.pause else R.drawable.play),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.iconButtonPlayer),
                        modifier = Modifier
                            .rotate(rotationAngle)
                            .align(Alignment.Center)
                            .size(24.dp)
                    )
                }

                it.fast4x.rimusic.ui.components.themed.IconButton(
                    icon = R.drawable.play_skip_forward,
                    color = colorPalette.iconButtonPlayer,
                    onClick = {
                        binder.player.forceSeekToNext()
                        if (effectRotationEnabled) isRotated = !isRotated
                    },
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .padding(horizontal = 2.dp, vertical = 8.dp)
                        .size(24.dp)
                )
            }

            Spacer(
                modifier = Modifier
                    .width(2.dp)
            )
        }
        /*****  */

    }
}