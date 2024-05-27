package it.fast4x.rimusic.ui.screens.player

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.ColorPaletteName
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.PauseBetweenSongs
import it.fast4x.rimusic.enums.PlayerControlsType
import it.fast4x.rimusic.enums.PlayerPlayButtonType
import it.fast4x.rimusic.enums.PlayerThumbnailSize
import it.fast4x.rimusic.enums.PlayerTimelineType
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Info
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.ui.UiMedia
import it.fast4x.rimusic.query
import it.fast4x.rimusic.ui.components.SeekBar
import it.fast4x.rimusic.ui.components.SeekBarCustom
import it.fast4x.rimusic.ui.components.SeekBarWaved
import it.fast4x.rimusic.ui.components.themed.CustomElevatedButton
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.PlaybackParamsDialog
import it.fast4x.rimusic.ui.components.themed.SelectorDialog
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.collapsedPlayerProgressBar
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.utils.UiTypeKey
import it.fast4x.rimusic.utils.bold
import it.fast4x.rimusic.utils.colorPaletteNameKey
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.downloadedStateMedia
import it.fast4x.rimusic.utils.effectRotationKey
import it.fast4x.rimusic.utils.forceSeekToNext
import it.fast4x.rimusic.utils.formatAsDuration
import it.fast4x.rimusic.utils.isCompositionLaunched
import it.fast4x.rimusic.utils.isGradientBackgroundEnabledKey
import it.fast4x.rimusic.utils.pauseBetweenSongsKey
import it.fast4x.rimusic.utils.playbackSpeedKey
import it.fast4x.rimusic.utils.playerControlsTypeKey
import it.fast4x.rimusic.utils.playerPlayButtonTypeKey
import it.fast4x.rimusic.utils.playerThumbnailSizeKey
import it.fast4x.rimusic.utils.playerTimelineTypeKey
import it.fast4x.rimusic.utils.positionAndDurationState
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.showRemainingSongTimeKey
import it.fast4x.rimusic.utils.trackLoopEnabledKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun Controls(
    navController: NavController,
    onCollapse: () -> Unit,
    layoutState: PlayerSheetState,
    media: UiMedia,
    mediaId: String,
    title: String?,
    artist: String?,
    artistIds: List<Info>?,
    albumId: String?,
    shouldBePlaying: Boolean,
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current
    val colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.ModernBlack)

    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    val uiType by rememberPreference(UiTypeKey, UiType.RiMusic)

    var trackLoopEnabled by rememberPreference(trackLoopEnabledKey, defaultValue = false)

    var scrubbingPosition by remember(mediaId) {
        mutableStateOf<Long?>(null)
    }

    //val onGoToArtist = artistRoute::global
    //val onGoToAlbum = albumRoute::global


    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    /*
    var nextmediaItemIndex = binder.player.nextMediaItemIndex ?: -1
    var nextmediaItemtitle = ""


    if (nextmediaItemIndex.toShort() > -1)
        nextmediaItemtitle = binder.player.getMediaItemAt(nextmediaItemIndex).mediaMetadata.title.toString()
    */

    var isRotated by rememberSaveable { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 360F else 0f,
        animationSpec = tween(durationMillis = 200), label = ""
    )
    var effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var disableScrollingText by rememberPreference(disableScrollingTextKey, false)
    var playerTimelineType by rememberPreference(playerTimelineTypeKey, PlayerTimelineType.Default)
    var playerPlayButtonType by rememberPreference(
        playerPlayButtonTypeKey,
        PlayerPlayButtonType.Rectangular
    )

    val scope = rememberCoroutineScope()
    val animatedPosition = remember { Animatable(position.toFloat()) }
    var isSeeking by remember { mutableStateOf(false) }


    val compositionLaunched = isCompositionLaunched()
    LaunchedEffect(mediaId) {
        if (compositionLaunched) animatedPosition.animateTo(0f)
    }
    LaunchedEffect(position) {
        if (!isSeeking && !animatedPosition.isRunning)
            animatedPosition.animateTo(
                position.toFloat(), tween(
                    durationMillis = 1000,
                    easing = LinearEasing
                )
            )
    }
    //val durationVisible by remember(isSeeking) { derivedStateOf { isSeeking } }


    LaunchedEffect(mediaId) {
        Database.likedAt(mediaId).distinctUntilChanged().collect { likedAt = it }
    }

    var isDownloaded by rememberSaveable {
        mutableStateOf<Boolean>(false)
    }

    isDownloaded = downloadedStateMedia(mediaId)

    //val menuState = LocalMenuState.current

    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")

    val playPauseRoundness by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it) 32.dp else 16.dp }
    )

    var showSelectDialog by remember { mutableStateOf(false) }

    val playerThumbnailSize by rememberPreference(
        playerThumbnailSizeKey,
        PlayerThumbnailSize.Medium
    )
    val isGradientBackgroundEnabled by rememberPreference(isGradientBackgroundEnabledKey, false)

    /*
    var windows by remember {
        mutableStateOf(binder.player.currentTimeline.windows)
    }
    var queuedSongs by remember {
        mutableStateOf<List<Song>>(emptyList())
    }
    LaunchedEffect(mediaId, windows) {
        Database.getSongsList(
            windows.map {
                it.mediaItem.mediaId
            }
        ).collect{ queuedSongs = it}
    }

    var totalPlayTimes = 0L
    queuedSongs.forEach {
        totalPlayTimes += it.durationText?.let { it1 ->
            durationTextToMillis(it1)
        }?.toLong() ?: 0
    }
     */

    /*
    var showLyrics by rememberSaveable {
        mutableStateOf(false)
    }
     */

    val pauseBetweenSongs by rememberPreference(pauseBetweenSongsKey, PauseBetweenSongs.`0`)

    var playbackSpeed by rememberPreference(playbackSpeedKey, 1f)
    var showSpeedPlayerDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val showRemainingSongTime by rememberPreference(showRemainingSongTimeKey, true)


    if (showSpeedPlayerDialog) {
        PlaybackParamsDialog(
            onDismiss = { showSpeedPlayerDialog = false },
            speedValue = { playbackSpeed = it },
            pitchValue = {}
        )
    }

    val playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Modern)

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = playerThumbnailSize.size.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (uiType != UiType.ViMusic) Arrangement.Start else Arrangement.Center,
                modifier = Modifier.fillMaxWidth(if (uiType != UiType.ViMusic) 0.90f else 1f)
            ) {
                if (uiType != UiType.ViMusic) {

                    IconButton(
                        icon = if (albumId == null && !media.isLocal) R.drawable.logo_youtube else R.drawable.album,
                        color = if (albumId == null) colorPalette.textDisabled else colorPalette.text,
                        enabled = albumId != null,
                        onClick = {
                            if (albumId != null) {
                                //onGoToAlbum(albumId)
                                navController.navigate(route = "${NavRoutes.album.name}/${albumId}")
                                layoutState.collapseSoft()
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
                }

                /*
                if (disableScrollingText == false) {
                    ScrollText(
                        text = title ?: "",
                        style = TextStyle(
                            color = if (albumId == null) colorPalette.textDisabled else colorPalette.text,
                            fontStyle = typography.l.bold.fontStyle,
                            fontSize = typography.l.bold.fontSize,
                            fontFamily = typography.l.bold.fontFamily
                        ),
                        onClick = {
                            //if (albumId != null) onGoToAlbum(albumId)
                            navController.navigate(route = "${NavRoutes.album.name}/${albumId}")
                            layoutState.collapseSoft()
                        },

                    )
                } else {
                */
                var modifierTitle = Modifier
                    .clickable {
                        if (albumId != null) {
                            navController.navigate(route = "${NavRoutes.album.name}/${albumId}")
                            layoutState.collapseSoft()
                            onCollapse()
                        }
                    }
                if (!disableScrollingText) modifierTitle = modifierTitle.basicMarquee()

                BasicText(
                    text = title ?: "",
                    style = TextStyle(
                        color = if (albumId == null) colorPalette.textDisabled else colorPalette.text,
                        fontStyle = typography.l.bold.fontStyle,
                        fontSize = typography.l.bold.fontSize,
                        fontFamily = typography.l.bold.fontFamily
                    ),
                    maxLines = 1,
                    modifier = modifierTitle
                )
                //}
            }

            if (uiType != UiType.ViMusic) {
                IconButton(
                    //color = if (likedAt == null) colorPalette.textDisabled else colorPalette.text,
                    color = colorPalette.favoritesIcon,
                    //icon = R.drawable.heart,
                    icon = if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
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

                /*
                IconButton(
                    icon = R.drawable.ellipsis_vertical,
                    color = colorPalette.text,
                    onClick = {
                        menuState.display {
                            binder.player.currentMediaItem?.let {
                                PlayerMenu(
                                    onDismiss = menuState::hide,
                                    mediaItem = it,
                                    binder = binder
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        //.padding(horizontal = 15.dp)
                        .size(24.dp)
                )
                 */
            }

        }



        Spacer(
            modifier = Modifier
                .height(10.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (uiType != UiType.ViMusic) Arrangement.Start else Arrangement.Center,
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
                        layoutState.collapseSoft()
                        onCollapse()
                    }
                )


            if (uiType != UiType.ViMusic) {
                IconButton(
                    icon = if (artistIds?.isEmpty() == true && !media.isLocal) R.drawable.logo_youtube else R.drawable.artists,
                    color = if (artistIds?.isEmpty() == true) colorPalette.textDisabled else colorPalette.text,
                    onClick = {
                        if (artistIds?.isNotEmpty() == true && artistIds.size > 1)
                            showSelectDialog = true
                        if (artistIds?.isNotEmpty() == true && artistIds.size == 1) {
                            //onGoToArtist( artistIds[0].id )
                            navController.navigate(route = "${NavRoutes.artist.name}/${artistIds[0].id}")
                            layoutState.collapseSoft()
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
            }

            /*
            if (disableScrollingText == false) {
            ScrollText(
                text = artist ?: "",
                style = TextStyle(
                    color = if (artistIds?.isEmpty() == true) colorPalette.textDisabled else colorPalette.text,
                    fontStyle = typography.s.bold.fontStyle,
                    fontSize = typography.s.bold.fontSize,
                    fontFamily = typography.s.bold.fontFamily
                ),
                onClick = {
                    if (artistIds?.isNotEmpty() == true && artistIds.size > 1)
                        showSelectDialog = true
                    if (artistIds?.isNotEmpty() == true && artistIds.size == 1) {
                        //onGoToArtist( artistIds[0].id )
                        navController.navigate(route = "${NavRoutes.artist.name}/${artistIds[0].id}")
                        layoutState.collapseSoft()
                    }

                }
            )
            } else {
             */
            var modifierArtist = Modifier
                .clickable {
                    if (artistIds?.isNotEmpty() == true && artistIds.size > 1)
                        showSelectDialog = true
                    if (artistIds?.isNotEmpty() == true && artistIds.size == 1) {
                        navController.navigate(route = "${NavRoutes.artist.name}/${artistIds[0].id}")
                        layoutState.collapseSoft()
                        onCollapse()
                    }
                }
            if (!disableScrollingText) modifierArtist = modifierArtist.basicMarquee()
            BasicText(
                text = artist ?: "",
                style = TextStyle(
                    color = if (artistIds?.isEmpty() == true) colorPalette.textDisabled else colorPalette.text,
                    fontStyle = typography.s.bold.fontStyle,
                    fontSize = typography.s.bold.fontSize,
                    fontFamily = typography.s.bold.fontFamily
                ),
                maxLines = 1,
                modifier = modifierArtist

            )
            //}

        }


        Spacer(
            modifier = Modifier
                .height(15.dp)
        )


        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
        ) {

            /*
            val visualizerData = remember {
                mutableStateOf(VisualizerData())
            }
            val amp = getAmplitudes(
                binder = binder,
                context = context,
                visualizerData = visualizerData
            )
            //println("mediaItem Controls amplitudes ${amp}")

            AudioWaveform(
                amplitudes = amp,
                progress = position.toFloat(),
                onProgressChange = {},
                amplitudeType = AmplitudeType.Avg,
                waveformBrush = SolidColor(colorPalette.text),
                progressBrush = SolidColor(colorPalette.favoritesIcon)

            )
             */

            if (playerTimelineType != PlayerTimelineType.Default && playerTimelineType != PlayerTimelineType.Wavy)
                SeekBarCustom(
                    type = playerTimelineType,
                    value = scrubbingPosition ?: position,
                    minimumValue = 0,
                    maximumValue = duration,
                    onDragStart = {
                        scrubbingPosition = it
                    },
                    onDrag = { delta ->
                        scrubbingPosition = if (duration != C.TIME_UNSET) {
                            scrubbingPosition?.plus(delta)?.coerceIn(0, duration)
                        } else {
                            null
                        }
                    },
                    onDragEnd = {
                        scrubbingPosition?.let(binder.player::seekTo)
                        scrubbingPosition = null
                    },
                    color = colorPalette.collapsedPlayerProgressBar,
                    backgroundColor = colorPalette.textSecondary,
                    shape = RoundedCornerShape(8.dp)
                )

            if (playerTimelineType == PlayerTimelineType.Default)
                SeekBar(
                    value = scrubbingPosition ?: position,
                    minimumValue = 0,
                    maximumValue = duration,
                    onDragStart = {
                        scrubbingPosition = it
                    },
                    onDrag = { delta ->
                        scrubbingPosition = if (duration != C.TIME_UNSET) {
                            scrubbingPosition?.plus(delta)?.coerceIn(0, duration)
                        } else {
                            null
                        }
                    },
                    onDragEnd = {
                        scrubbingPosition?.let(binder.player::seekTo)
                        scrubbingPosition = null
                    },
                    color = colorPalette.collapsedPlayerProgressBar,
                    backgroundColor = colorPalette.textSecondary,
                    shape = RoundedCornerShape(8.dp),
                )




            if (playerTimelineType == PlayerTimelineType.Wavy) {
                SeekBarWaved(
                    position = { animatedPosition.value },
                    range = 0f..media.duration.toFloat(),
                    onSeekStarted = {
                        scrubbingPosition = it.toLong()

                        //isSeeking = true
                        scope.launch {
                            animatedPosition.animateTo(it)
                        }

                    },
                    onSeek = { delta ->
                        scrubbingPosition = if (duration != C.TIME_UNSET) {
                            scrubbingPosition?.plus(delta)?.coerceIn(0F, duration.toFloat())
                                ?.toLong()
                        } else {
                            null
                        }

                        if (media.duration != C.TIME_UNSET) {
                            //isSeeking = true
                            scope.launch {
                                animatedPosition.snapTo(
                                    animatedPosition.value.plus(delta)
                                        .coerceIn(0f, media.duration.toFloat())
                                )
                            }
                        }

                    },
                    onSeekFinished = {
                        scrubbingPosition?.let(binder.player::seekTo)
                        scrubbingPosition = null
                        /*
                    isSeeking = false
                    animatedPosition.let {
                        binder.player.seekTo(it.targetValue.toLong())
                    }
                     */
                    },
                    color = colorPalette.collapsedPlayerProgressBar,
                    isActive = binder.player.isPlaying,
                    backgroundColor = colorPalette.textSecondary,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }



        Spacer(
            modifier = Modifier
                .height(8.dp)
        )


        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
        ) {
            BasicText(
                text = formatAsDuration(scrubbingPosition ?: position),
                style = typography.xxs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (duration != C.TIME_UNSET) {
                val positionAndDuration = binder.player.positionAndDurationState()
                var timeRemaining by remember { mutableIntStateOf(0) }
                timeRemaining =
                    positionAndDuration.value.second.toInt() - positionAndDuration.value.first.toInt()
                var paused by remember { mutableStateOf(false) }

                if (pauseBetweenSongs != PauseBetweenSongs.`0`)
                    LaunchedEffect(timeRemaining) {
                        if (
                        //formatAsDuration(timeRemaining.toLong()) == "0:00"
                            timeRemaining.toLong() < 500
                        ) {
                            paused = true
                            binder.player.pause()
                            delay(pauseBetweenSongs.number)
                            //binder.player.seekTo(position+2000)
                            binder.player.play()
                            paused = false
                        }
                    }

                if (!paused) {

                    if (showRemainingSongTime)
                        BasicText(
                            text = "-${formatAsDuration(timeRemaining.toLong())}",
                            style = typography.xxs.semiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                        )

                    /*
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                    ) {
                        Image(
                            painter = painterResource(R.drawable.time),
                            colorFilter = ColorFilter.tint(colorPalette.accent),
                            modifier = Modifier
                                .size(20.dp)
                                .padding(horizontal = 5.dp),
                            contentDescription = "Background Image",
                            contentScale = ContentScale.Fit
                        )
                        BasicText(
                            text = " ${formatAsTime(totalPlayTimes)}",
                            style = typography.xxs.semiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                     */

                } else {
                    Image(
                        painter = painterResource(R.drawable.pause),
                        colorFilter = ColorFilter.tint(colorPalette.accent),
                        modifier = Modifier
                            .size(20.dp),
                        contentDescription = "Background Image",
                        contentScale = ContentScale.Fit
                    )
                }

                /*
                BasicText(
                    text = "-${formatAsDuration(timeRemaining.toLong())} / ${formatAsDuration(duration)}",
                    style = typography.xxs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                 */

                BasicText(
                    text = formatAsDuration(duration),
                    style = typography.xxs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

            }
        }

        Spacer(
            modifier = Modifier
                .weight(0.4f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {

            if (playerControlsType == PlayerControlsType.Essential) {
                IconButton(
                    color = colorPalette.favoritesIcon,
                    icon = if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
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
                                //binder.player.forceSeekToPrevious()
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
                            onLongClick = {
                                showSpeedPlayerDialog = true
                            }
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
                    //.width(if (uiType != UiType.RiMusic) PlayerPlayButtonType.Default.width.dp else playerPlayButtonType.width.dp)
                    //.height(if (uiType != UiType.RiMusic) PlayerPlayButtonType.Default.height.dp else playerPlayButtonType.height.dp)
                ) {
                    //if (uiType == UiType.RiMusic && playerPlayButtonType == PlayerPlayButtonType.CircularRibbed)
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
                        colorFilter = ColorFilter.tint(
                            if (uiType == UiType.RiMusic)
                                colorPalette.collapsedPlayerProgressBar
                            else colorPalette.text
                        ), //if (playerPlayButtonType == PlayerPlayButtonType.CircularRibbed) ColorFilter.tint(colorPalette.iconButtonPlayer) else ColorFilter.tint(colorPalette.text),
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
                                    color = colorPalette.collapsedPlayerProgressBar,
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


            if (playerControlsType == PlayerControlsType.Modern) {
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
                        colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
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
                            onLongClick = {
                                showSpeedPlayerDialog = true
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
                        colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar), //if (playerPlayButtonType == PlayerPlayButtonType.CircularRibbed) ColorFilter.tint(colorPalette.iconButtonPlayer) else ColorFilter.tint(colorPalette.text),
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
                                    color = colorPalette.collapsedPlayerProgressBar,
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
                        colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
                        modifier = Modifier
                            .padding(10.dp)
                            .size(26.dp)
                            .rotate(rotationAngle)
                    )
                }
            }

        }


        Spacer(
            modifier = Modifier
                .weight(0.5f)
        )

    }

}


/*
@ExperimentalTextApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
private fun PlayerMenu(
    binder: PlayerService.Binder,
    mediaItem: MediaItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    BaseMediaItemMenu(
        mediaItem = mediaItem,
        onStartRadio = {
            binder.stopRadio()
            binder.player.seamlessPlay(mediaItem)
            binder.setupRadio(NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId))
        },
        onGoToEqualizer = {
            try {
                activityResultLauncher.launch(
                    Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                        putExtra(AudioEffect.EXTRA_AUDIO_SESSION, binder.player.audioSessionId)
                        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                    }
                )
            } catch (e: ActivityNotFoundException) {
                context.toast("Couldn't find an application to equalize audio")
            }
        },
        onShowSleepTimer = {},
        onDismiss = onDismiss
    )
}

@Composable
private fun Duration(
    position: Float,
    duration: Long,
) {
    val typography = LocalAppearance.current.typography
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        BasicText(
            text = formatAsDuration(position.toLong()),
            style = typography.xxs.semiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (duration != C.TIME_UNSET) {
            BasicText(
                text = formatAsDuration(duration),
                style = typography.xxs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
*/