package it.fast4x.rimusic.ui.screens.player

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.BackgroundProgress
import it.fast4x.rimusic.enums.ColorPaletteMode
import it.fast4x.rimusic.enums.DurationInSeconds
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.PlayerBackgroundColors
import it.fast4x.rimusic.enums.PlayerThumbnailSize
import it.fast4x.rimusic.enums.PlayerVisualizerType
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Info
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.ui.toUiMedia
import it.fast4x.rimusic.query
import it.fast4x.rimusic.ui.components.BottomSheetState
import it.fast4x.rimusic.ui.components.CustomModalBottomSheet
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.rememberBottomSheetState
import it.fast4x.rimusic.ui.components.themed.BlurParamsDialog
import it.fast4x.rimusic.ui.components.themed.CircularSlider
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.DefaultDialog
import it.fast4x.rimusic.ui.components.themed.DownloadStateIconButton
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.MiniPlayerMenu
import it.fast4x.rimusic.ui.components.themed.PlayerMenu
import it.fast4x.rimusic.ui.components.themed.SecondaryTextButton
import it.fast4x.rimusic.ui.components.themed.SmartToast
import it.fast4x.rimusic.ui.components.themed.animateBrushRotation
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.collapsedPlayerProgressBar
import it.fast4x.rimusic.ui.styling.dynamicColorPaletteOf
import it.fast4x.rimusic.ui.styling.favoritesOverlay
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.BlurTransformation
import it.fast4x.rimusic.utils.DisposableListener
import it.fast4x.rimusic.utils.UiTypeKey
import it.fast4x.rimusic.utils.audioFadeIn
import it.fast4x.rimusic.utils.audioFadeOut
import it.fast4x.rimusic.utils.backgroundProgressKey
import it.fast4x.rimusic.utils.blurDarkenFactorKey
import it.fast4x.rimusic.utils.blurStrengthKey
import it.fast4x.rimusic.utils.colorPaletteModeKey
import it.fast4x.rimusic.utils.currentWindow
import it.fast4x.rimusic.utils.disableClosingPlayerSwipingDownKey
import it.fast4x.rimusic.utils.disablePlayerHorizontalSwipeKey
import it.fast4x.rimusic.utils.downloadedStateMedia
import it.fast4x.rimusic.utils.durationTextToMillis
import it.fast4x.rimusic.utils.effectRotationKey
import it.fast4x.rimusic.utils.forceSeekToNext
import it.fast4x.rimusic.utils.formatAsDuration
import it.fast4x.rimusic.utils.formatAsTime
import it.fast4x.rimusic.utils.getBitmapFromUrl
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.isLandscape
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.playbackFadeDurationKey
import it.fast4x.rimusic.utils.playerBackgroundColorsKey
import it.fast4x.rimusic.utils.playerThumbnailSizeKey
import it.fast4x.rimusic.utils.playerVisualizerTypeKey
import it.fast4x.rimusic.utils.positionAndDurationState
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.shouldBePlaying
import it.fast4x.rimusic.utils.showButtonPlayerAddToPlaylistKey
import it.fast4x.rimusic.utils.showButtonPlayerArrowKey
import it.fast4x.rimusic.utils.showButtonPlayerDownloadKey
import it.fast4x.rimusic.utils.showButtonPlayerLoopKey
import it.fast4x.rimusic.utils.showButtonPlayerLyricsKey
import it.fast4x.rimusic.utils.showButtonPlayerMenuKey
import it.fast4x.rimusic.utils.showButtonPlayerShuffleKey
import it.fast4x.rimusic.utils.showButtonPlayerSleepTimerKey
import it.fast4x.rimusic.utils.showButtonPlayerSystemEqualizerKey
import it.fast4x.rimusic.utils.showNextSongsInPlayerKey
import it.fast4x.rimusic.utils.showTopActionsBarKey
import it.fast4x.rimusic.utils.showTotalTimeQueueKey
import it.fast4x.rimusic.utils.shuffleQueue
import it.fast4x.rimusic.utils.thumbnail
import it.fast4x.rimusic.utils.thumbnailTapEnabledKey
import it.fast4x.rimusic.utils.trackLoopEnabledKey
import it.fast4x.rimusic.utils.transparentBackgroundPlayerActionBarKey
import it.fast4x.rimusic.utils.windows
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.RoundingMode
import kotlin.math.absoluteValue


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation", "RememberReturnType")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun PlayerModern(
    navController: NavController,
    layoutState: PlayerSheetState,
    playerState: SheetState,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp
    ),
    onDismiss: () -> Unit,
) {
    val menuState = LocalMenuState.current

    val uiType by rememberPreference(UiTypeKey, UiType.RiMusic)

    val effectRotationEnabled by rememberPreference(effectRotationKey, true)

    val playerThumbnailSize by rememberPreference(
        playerThumbnailSizeKey,
        PlayerThumbnailSize.Medium
    )

    var disablePlayerHorizontalSwipe by rememberPreference(disablePlayerHorizontalSwipeKey, false)

    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    val binder = LocalPlayerServiceBinder.current

    binder?.player ?: return

    var nullableMediaItem by remember {
        mutableStateOf(binder.player.currentMediaItem, neverEqualPolicy())
    }

    var shouldBePlaying by remember {
        mutableStateOf(binder.player.shouldBePlaying)
    }

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

    val playerVisualizerType by rememberPreference(
        playerVisualizerTypeKey,
        PlayerVisualizerType.Disabled
    )

    val playbackFadeDuration by rememberPreference(
        playbackFadeDurationKey,
        DurationInSeconds.Disabled
    )

    val defaultStrength = 25f
    val defaultDarkenFactor = 0.2f
    var blurStrength by rememberPreference(blurStrengthKey, defaultStrength)
    var blurDarkenFactor by rememberPreference(blurDarkenFactorKey, defaultDarkenFactor)
    var showBlurPlayerDialog by rememberSaveable {
        mutableStateOf(false)
    }
    if (showBlurPlayerDialog) {
        BlurParamsDialog(
            onDismiss = { showBlurPlayerDialog = false},
            scaleValue = { blurStrength = it },
            darkenFactorValue = { blurDarkenFactor = it}
        )
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    binder.player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                nullableMediaItem = mediaItem
                if (playbackFadeDuration != DurationInSeconds.Disabled) {
                    binder.player.volume = 0f
                    audioFadeIn(binder.player, playbackFadeDuration.seconds, context)
                }
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

    val positionAndDuration by binder.player.positionAndDurationState()
    var timeRemaining by remember { mutableIntStateOf(0) }
    timeRemaining = positionAndDuration.second.toInt() - positionAndDuration.first.toInt()

    if (playbackFadeDuration != DurationInSeconds.Disabled) {
        val songProgressFloat =
            ((positionAndDuration.first.toFloat() * 100) / positionAndDuration.second.absoluteValue)
                .toBigDecimal().setScale(2, RoundingMode.UP).toDouble()
        //val songProgressInt = songProgressFloat.toInt()
        if (songProgressFloat in playbackFadeDuration.fadeOutRange && binder.player.shouldBePlaying) {
            //if (timeRemaining in playbackFadeDuration.fadeOutRange) {
            //println("mediaItem volume startFadeOut $fadeInOut")
            audioFadeOut(binder.player, playbackFadeDuration.seconds, context)
            //fadeInOut = true
            //startFadeOut(binder, playbackFadeDuration.seconds)
            //fade = !fade
        }


        /*
        if (songProgressFloat in playbackFadeDuration.fadeInRange && binder.player.shouldBePlaying) {
            //binder.player.volume = 0f
            println("mediaItem volume startFadeIn")
            audioFadeIn(binder.player, playbackFadeDuration.seconds, context)
            //fadeInOut = false
            //startFadeIn(binder, playbackFadeDuration.seconds)
            //fade = !fade
        }
         */

        //println("mediaItem positionAndDuration $positionAndDuration % ${(positionAndDuration.first.toInt()*100) / positionAndDuration.second.toInt()}")
        //println("mediaItem progress float $songProgressFloat playbackFadeDuration ${playbackFadeDuration} $fadeInOut")
    }


    val windowInsets = WindowInsets.systemBars

    val horizontalBottomPaddingValues = windowInsets
        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom).asPaddingValues()

    var albumInfo by remember {
        mutableStateOf(mediaItem.mediaMetadata.extras?.getString("albumId")?.let { albumId ->
            Info(albumId, null)
        })
    }

    var artistsInfo by remember {
        mutableStateOf(
            mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames")?.let { artistNames ->
                mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.let { artistIds ->
                    artistNames.zip(artistIds).map { (authorName, authorId) ->
                        Info(authorId, authorName)
                    }
                }
            }
        )
    }

    LaunchedEffect(mediaItem.mediaId) {
        withContext(Dispatchers.IO) {
            //if (albumInfo == null)
            albumInfo = Database.songAlbumInfo(mediaItem.mediaId)
            //if (artistsInfo == null)
            artistsInfo = Database.songArtistInfo(mediaItem.mediaId)
        }
    }


    val ExistIdsExtras =
        mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.size.toString()
    val ExistAlbumIdExtras = mediaItem.mediaMetadata.extras?.getString("albumId")

    var albumId = albumInfo?.id
    if (albumId == null) albumId = ExistAlbumIdExtras
    //var albumTitle = albumInfo?.name

    var artistIds = arrayListOf<String>()
    var artistNames = arrayListOf<String>()


    artistsInfo?.forEach { (id) -> artistIds = arrayListOf(id) }
    if (ExistIdsExtras.equals(0)
            .not()
    ) mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.toCollection(artistIds)

    artistsInfo?.forEach { (name) -> artistNames = arrayListOf(name) }
    if (ExistIdsExtras.equals(0)
            .not()
    ) mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames")?.toCollection(artistNames)



    if (artistsInfo?.isEmpty() == true && ExistIdsExtras.equals(0).not()) {
        artistsInfo = artistNames.let { artistNames ->
            artistIds.let { artistIds ->
                artistNames.zip(artistIds).map {
                    Info(it.second, it.first)
                }
            }
        }
    }


    /*
    //Log.d("mediaItem_pl_mediaId",mediaItem.mediaId)
    Log.d("mediaItem_player","--- START LOG ARTIST ---")
    Log.d("mediaItem_player","ExistIdsExtras: $ExistIdsExtras")
    Log.d("mediaItem_player","metadata artisIds "+mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds").toString())
    Log.d("mediaItem_player","variable artisIds "+artistIds.toString())
    Log.d("mediaItem_player","variable artisNames pre"+artistNames.toString())
    Log.d("mediaItem_player","variable artistsInfo pre "+artistsInfo.toString())

    //Log.d("mediaItem_pl_artinfo",artistsInfo.toString())
    //Log.d("mediaItem_pl_artId",artistIds.toString())
    Log.d("mediaItem_player","--- START LOG ALBUM ---")
    Log.d("mediaItem_player",ExistAlbumIdExtras.toString())
    Log.d("mediaItem_player","metadata albumId "+mediaItem.mediaMetadata.extras?.getString("albumId").toString())
    Log.d("mediaItem_player","metadata extra "+mediaItem.mediaMetadata.extras?.toString())
    Log.d("mediaItem_player","metadata full "+mediaItem.mediaMetadata.toString())
    //Log.d("mediaItem_pl_extrasArt",mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames").toString())
    //Log.d("mediaItem_pl_extras",mediaItem.mediaMetadata.extras.toString())
    Log.d("mediaItem_player","albumInfo "+albumInfo.toString())
    Log.d("mediaItem_player","albumId "+albumId.toString())

    Log.d("mediaItem_pl","--- END LOG ---")

    */


    var trackLoopEnabled by rememberPreference(trackLoopEnabledKey, defaultValue = false)


    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }
    LaunchedEffect(mediaItem.mediaId) {
        Database.likedAt(mediaItem.mediaId).distinctUntilChanged().collect { likedAt = it }
    }


    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }
    downloadState = getDownloadState(mediaItem.mediaId)

//    val isLocal by remember { derivedStateOf { mediaItem.isLocal } }

    var isDownloaded by rememberSaveable { mutableStateOf(false) }
    isDownloaded = downloadedStateMedia(mediaItem.mediaId)

    val showButtonPlayerAddToPlaylist by rememberPreference(showButtonPlayerAddToPlaylistKey, true)
    val showButtonPlayerArrow by rememberPreference(showButtonPlayerArrowKey, false)
    val showButtonPlayerDownload by rememberPreference(showButtonPlayerDownloadKey, true)
    val showButtonPlayerLoop by rememberPreference(showButtonPlayerLoopKey, true)
    val showButtonPlayerLyrics by rememberPreference(showButtonPlayerLyricsKey, true)
    val showButtonPlayerShuffle by rememberPreference(showButtonPlayerShuffleKey, true)
    val showButtonPlayerSleepTimer by rememberPreference(showButtonPlayerSleepTimerKey, false)
    val showButtonPlayerMenu by rememberPreference(showButtonPlayerMenuKey, false)
    val showButtonPlayerSystemEqualizer by rememberPreference(
        showButtonPlayerSystemEqualizerKey,
        false
    )
    val disableClosingPlayerSwipingDown by rememberPreference(
        disableClosingPlayerSwipingDownKey,
        true
    )
    val showTotalTimeQueue by rememberPreference(showTotalTimeQueueKey, true)
    val backgroundProgress by rememberPreference(
        backgroundProgressKey,
        BackgroundProgress.MiniPlayer
    )
    /*
    val playlistPreviews by remember {
        Database.playlistPreviews(PlaylistSortBy.Name, SortOrder.Ascending)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)


    var showPlaylistSelectDialog by remember {
        mutableStateOf(false)
    }
     */

    var isShowingSleepTimerDialog by remember {
        mutableStateOf(false)
    }

    val sleepTimerMillisLeft by (binder?.sleepTimerMillisLeft
        ?: flowOf(null))
        .collectAsState(initial = null)

    var showCircularSlider by remember {
        mutableStateOf(false)
    }

    if (isShowingSleepTimerDialog) {
        if (sleepTimerMillisLeft != null) {
            ConfirmationDialog(
                text = stringResource(R.string.stop_sleep_timer),
                cancelText = stringResource(R.string.no),
                confirmText = stringResource(R.string.stop),
                onDismiss = { isShowingSleepTimerDialog = false },
                onConfirm = {
                    binder.cancelSleepTimer()
                    //onDismiss()
                }
            )
        } else {
            DefaultDialog(
                onDismiss = { isShowingSleepTimerDialog = false }
            ) {
                var amount by remember {
                    mutableStateOf(1)
                }

                BasicText(
                    text = stringResource(R.string.set_sleep_timer),
                    style = typography.s.semiBold,
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 24.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 16.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                ) {
                    if (!showCircularSlider) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .alpha(if (amount <= 1) 0.5f else 1f)
                                .clip(CircleShape)
                                .clickable(enabled = amount > 1) { amount-- }
                                .size(48.dp)
                                .background(colorPalette.background0)
                        ) {
                            BasicText(
                                text = "-",
                                style = typography.xs.semiBold
                            )
                        }

                        Box(contentAlignment = Alignment.Center) {
                            BasicText(
                                text = stringResource(
                                    R.string.left,
                                    formatAsDuration(amount * 5 * 60 * 1000L)
                                ),
                                style = typography.s.semiBold,
                                modifier = Modifier
                                    .clickable {
                                        showCircularSlider = !showCircularSlider
                                    }
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .alpha(if (amount >= 60) 0.5f else 1f)
                                .clip(CircleShape)
                                .clickable(enabled = amount < 60) { amount++ }
                                .size(48.dp)
                                .background(colorPalette.background0)
                        ) {
                            BasicText(
                                text = "+",
                                style = typography.xs.semiBold
                            )
                        }

                    } else {
                        CircularSlider(
                            stroke = 40f,
                            thumbColor = colorPalette.accent,
                            text = formatAsDuration(amount * 5 * 60 * 1000L),
                            modifier = Modifier
                                .size(300.dp),
                            onChange = {
                                amount = (it * 120).toInt()
                            }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .fillMaxWidth()
                ) {
                    SecondaryTextButton(
                        text = stringResource(R.string.set_to) + " "
                                + formatAsDuration(timeRemaining.toLong())
                                + " " + stringResource(R.string.end_of_song),
                        onClick = {
                            binder?.startSleepTimer(timeRemaining.toLong())
                            isShowingSleepTimerDialog = false
                        }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {

                    IconButton(
                        onClick = { showCircularSlider = !showCircularSlider },
                        icon = R.drawable.time,
                        color = colorPalette.text
                    )
                    IconButton(
                        onClick = { isShowingSleepTimerDialog = false },
                        icon = R.drawable.close,
                        color = colorPalette.text
                    )
                    IconButton(
                        enabled = amount > 0,
                        onClick = {
                            binder?.startSleepTimer(amount * 5 * 60 * 1000L)
                            isShowingSleepTimerDialog = false
                        },
                        icon = R.drawable.checkmark,
                        color = colorPalette.accent
                    )
                }
            }
        }
    }

    var position by remember {
        mutableIntStateOf(0)
    }

    var dynamicColorPalette by remember { mutableStateOf(colorPalette) }
    val colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.Light)
    val playerBackgroundColors by rememberPreference(
        playerBackgroundColorsKey,
        PlayerBackgroundColors.ThemeColor
    )
    val isGradientBackgroundEnabled =
        playerBackgroundColors == PlayerBackgroundColors.ThemeColorGradient ||
                playerBackgroundColors == PlayerBackgroundColors.CoverColorGradient ||
                playerBackgroundColors == PlayerBackgroundColors.FluidThemeColorGradient ||
                playerBackgroundColors == PlayerBackgroundColors.FluidCoverColorGradient

    if (playerBackgroundColors == PlayerBackgroundColors.CoverColorGradient ||
        playerBackgroundColors == PlayerBackgroundColors.CoverColor ||
        playerBackgroundColors == PlayerBackgroundColors.FluidCoverColorGradient
    ) {
        val context = LocalContext.current
        val isSystemDarkMode = isSystemInDarkTheme()
        LaunchedEffect(mediaItem.mediaId) {
            dynamicColorPalette = dynamicColorPaletteOf(
                getBitmapFromUrl(
                    context,
                    binder.player.currentWindow?.mediaItem?.mediaMetadata?.artworkUri.toString()
                ),
                isSystemDarkMode,
                colorPaletteMode == ColorPaletteMode.PitchBlack
            ) ?: colorPalette
        }
    }

    /*  */
    var sizeShader by remember { mutableStateOf(Size.Zero) }

    val shaderA = LinearGradientShader(
        Offset(sizeShader.width / 2f, 0f),
        Offset(sizeShader.width / 2f, sizeShader.height),
        listOf(
            dynamicColorPalette.background2,
            colorPalette.background2,
        ),
        listOf(0f, 1f)
    )

    val shaderB = LinearGradientShader(
        Offset(sizeShader.width / 2f, 0f),
        Offset(sizeShader.width / 2f, sizeShader.height),
        listOf(
            colorPalette.background1,
            dynamicColorPalette.accent,
        ),
        listOf(0f, 1f)
    )

    val shaderMask = LinearGradientShader(
        Offset(sizeShader.width / 2f, 0f),
        Offset(sizeShader.width / 2f, sizeShader.height),
        listOf(
            //Color.White,
            colorPalette.background2,
            Color.Transparent,
        ),
        listOf(0f, 1f)
    )

    val brushA by animateBrushRotation(shaderA, sizeShader, 20_000, true)
    val brushB by animateBrushRotation(shaderB, sizeShader, 12_000, false)
    val brushMask by animateBrushRotation(shaderMask, sizeShader, 15_000, true)
    /*  */

    val (thumbnailSizeDp, thumbnailSizePx) = Dimensions.thumbnails.player.song.let {
        it to (it - 64.dp).px
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(
                mediaItem.mediaMetadata.artworkUri.thumbnail(
                    thumbnailSizePx
                )
            )
            .size(coil.size.Size.ORIGINAL)
            .transformations(
                listOf(
                    BlurTransformation(
                        scale = 0.5f,
                        radius = blurStrength.toInt(),
                        //darkenFactor = blurDarkenFactor
                    )
                )
            )
            .build()
    )

    //val imageState = painter.state

    /*
    OnGlobalRoute {
        layoutState.collapseSoft()
    }
     */

    //val onGoToHome = homeRoute::global


    val windows by remember {
        mutableStateOf(binder.player.currentTimeline.windows)
    }

    var queuedSongs by remember {
        mutableStateOf<List<Song>>(emptyList())
    }

    LaunchedEffect(Unit, mediaItem.mediaId, windows) {
        Database.getSongsList(
            windows.map {
                it.mediaItem.mediaId
            }
        ).collect { queuedSongs = it }
    }

    var totalPlayTimes = 0L
    queuedSongs.forEach {
        totalPlayTimes += it.durationText?.let { it1 ->
            durationTextToMillis(it1)
        }?.toLong() ?: 0
    }


    var isShowingLyrics by rememberSaveable {
        mutableStateOf(false)
    }

    var isShowingStatsForNerds by rememberSaveable {
        mutableStateOf(false)
    }

    var isShowingEqualizer by remember {
        mutableStateOf(false)
    }

    val thumbnailTapEnabled by rememberPreference(thumbnailTapEnabledKey, false)
    val showNextSongsInPlayer by rememberPreference(showNextSongsInPlayerKey, false)

    val playerBottomHeight = if (showNextSongsInPlayer) 80.dp else 50.dp
    //val playerBottomHeight = 0.dp
    /*
    val playerBottomSheetState = rememberBottomSheetState(
        playerBottomHeight + horizontalBottomPaddingValues.calculateBottomPadding(),
        layoutState.expandedBound
    )
     */

    //val queueSheetBottomHeight = 0.dp
    /*
    val queueSheetState = rememberBottomSheetState(
        horizontalBottomPaddingValues.calculateBottomPadding(),
        layoutState.expandedBound
    )


    val lyricsBottomSheetState =rememberBottomSheetState(
        horizontalBottomPaddingValues.calculateBottomPadding(),
        layoutState.expandedBound
    )
    */

    var showQueue by rememberSaveable { mutableStateOf(false) }
    var showFullLyrics by rememberSaveable { mutableStateOf(false) }

    val transparentBackgroundActionBarPlayer by rememberPreference(transparentBackgroundPlayerActionBarKey, false)
    val showTopActionsBar by rememberPreference(showTopActionsBarKey, true)

    /*
    val density = LocalDensity.current
    val windowsInsets = WindowInsets.systemBars
    val bottomDp = with(density) { windowsInsets.getBottom(density).toDp() }
     */

    var containerModifier = Modifier
        //.padding(bottom = bottomDp)
        .padding(bottom = 0.dp)
        /*
        .padding(
            windowInsets
                .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                .asPaddingValues()
        )
         */
        //.padding(bottom = playerSheetState.collapsedBound)
        //.padding(bottom = horizontalBottomPaddingValues.calculateBottomPadding())


    if (!isGradientBackgroundEnabled) {
        if (playerBackgroundColors == PlayerBackgroundColors.BlurredCoverColor) {
            containerModifier = containerModifier
                .background(dynamicColorPalette.background1)
                .paint(
                    painter = painter,
                    contentScale = ContentScale.Crop,
                    sizeToIntrinsics = false
                )
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                    },
                    onLongClick = {
                        showBlurPlayerDialog = true
                    }
                )

        } else {
            containerModifier = containerModifier
                .background(
                    dynamicColorPalette.background1
                    //colorPalette.background1
                )
        }
    } else {
        when (playerBackgroundColors) {
            PlayerBackgroundColors.FluidThemeColorGradient,
            PlayerBackgroundColors.FluidCoverColorGradient -> {
                containerModifier = containerModifier
                    .onSizeChanged {
                        sizeShader = Size(it.width.toFloat(), it.height.toFloat())
                    }
                    .drawBehind {
                        drawRect(brush = brushA)
                        drawRect(brush = brushMask, blendMode = BlendMode.DstOut)
                        drawRect(brush = brushB, blendMode = BlendMode.DstAtop)
                    }
            }

            else -> {
                containerModifier = containerModifier
                    .background(
                        Brush.verticalGradient(
                            0.5f to dynamicColorPalette.background2,
                            1.0f to colorPalette.background2,
                            //0.0f to colorPalette.background0,
                            //1.0f to colorPalette.background2,
                            startY = 0.0f,
                            endY = 1500.0f
                        )
                    )

            }
        }

    }

    val thumbnailContent: @Composable (modifier: Modifier) -> Unit = { modifier ->
        var deltaX by remember { mutableStateOf(0f) }
        //var direction by remember { mutableIntStateOf(-1)}
        Thumbnail(
            thumbnailTapEnabledKey = thumbnailTapEnabled,
            isShowingLyrics = isShowingLyrics,
            onShowLyrics = { isShowingLyrics = it },
            isShowingStatsForNerds = isShowingStatsForNerds,
            onShowStatsForNerds = { isShowingStatsForNerds = it },
            isShowingEqualizer = isShowingEqualizer,
            onShowEqualizer = { isShowingEqualizer = it },
            onMaximize = {
                    showFullLyrics = true
            },
            onDoubleTap = {
                val currentMediaItem = binder.player.currentMediaItem
                query {
                    if (Database.like(
                            mediaItem.mediaId,
                            if (likedAt == null) System.currentTimeMillis() else null
                        ) == 0
                    ) {
                        currentMediaItem
                            ?.takeIf { it.mediaId == mediaItem.mediaId }
                            ?.let {
                                Database.insert(currentMediaItem, Song::toggleLike)
                            }
                    }
                }
                if (effectRotationEnabled) isRotated = !isRotated
            },
            modifier = modifier
                //.nestedScroll( connection = scrollConnection )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            deltaX = dragAmount
                        },
                        onDragStart = {
                            //Log.d("mediaItemGesture","ondragStart offset ${it}")
                        },
                        onDragEnd = {
                            if (!disablePlayerHorizontalSwipe) {
                                if (deltaX > 0) {
                                    binder.player.seekToPreviousMediaItem()
                                    //binder.player.forceSeekToPrevious()
                                    //Log.d("mediaItem","Swipe to LEFT")
                                } else {
                                    binder.player.forceSeekToNext()
                                    //Log.d("mediaItem","Swipe to RIGHT")
                                }

                            }

                        }

                    )
                }

        )
    }


    val controlsContent: @Composable (modifier: Modifier) -> Unit = { modifier ->
        Controls(
            navController = navController,
            onCollapse = onDismiss,
            layoutState = layoutState,
            media = mediaItem.toUiMedia(positionAndDuration.second),
            mediaId = mediaItem.mediaId,
            title = mediaItem.mediaMetadata.title?.toString(),
            artist = mediaItem.mediaMetadata.artist?.toString(),
            artistIds = artistsInfo,
            albumId = albumId,
            shouldBePlaying = shouldBePlaying,
            position = positionAndDuration.first,
            duration = positionAndDuration.second,
            modifier = modifier
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val actionsBarContent: @Composable (modifier: Modifier) -> Unit = { modifier ->
            if (
                !showButtonPlayerDownload &&
                !showButtonPlayerAddToPlaylist &&
                !showButtonPlayerLoop &&
                !showButtonPlayerShuffle &&
                !showButtonPlayerLyrics &&
                !showButtonPlayerSleepTimer &&
                !showButtonPlayerSystemEqualizer &&
                !showButtonPlayerArrow &&
                !showButtonPlayerMenu
            ) {
                Row(
                ) {
                }
            } else
            Row(
                modifier = Modifier
                    .align(if (isLandscape) Alignment.BottomEnd else Alignment.BottomCenter)
                    .requiredHeight(if (showNextSongsInPlayer) 90.dp else 50.dp)
                    .fillMaxWidth(if (isLandscape) 0.8f else 1f)
                    .clickable { showQueue = true }
                    .background(colorPalette.background2.copy(
                        alpha = if (transparentBackgroundActionBarPlayer) 0.1f else 0.7f
                    ))
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->
                                if (dragAmount < 0) showQueue = true
                            }
                        )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (showNextSongsInPlayer) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .background(colorPalette.background2.copy(alpha = 0.3f))
                                .padding(horizontal = 12.dp)
                                .fillMaxWidth()
                        ) {
                            val nextMediaItemIndex = binder.player.nextMediaItemIndex
                            val nextMediaItem = if (binder.player.hasNextMediaItem())
                                binder.player.getMediaItemAt(binder.player.nextMediaItemIndex)
                            else MediaItem.EMPTY
                            val nextNextMediaItem = try {
                                binder.player.getMediaItemAt(nextMediaItemIndex + 1)
                            } catch (e: Exception) {
                                MediaItem.EMPTY
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                            ) {
                                AsyncImage(
                                    model = nextMediaItem.mediaMetadata.artworkUri.thumbnail(
                                        Dimensions.thumbnails.song.px / 2
                                    ),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .padding(all = 5.dp)
                                        .clip(thumbnailShape)
                                        .size(30.dp)
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .height(40.dp)
                                    .weight(1f)
                            ) {

                                BasicText(
                                    text = nextMediaItem.mediaMetadata.title?.toString() ?: "",
                                    style = typography.xxxs.semiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                BasicText(
                                    text = nextMediaItem.mediaMetadata.artist?.toString() ?: "",
                                    style = typography.xxxs.semiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                            ) {
                                AsyncImage(
                                    model = nextNextMediaItem.mediaMetadata.artworkUri.thumbnail(
                                        Dimensions.thumbnails.song.px / 2
                                    ),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .padding(all = 5.dp)
                                        .clip(thumbnailShape)
                                        .size(30.dp)
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .height(40.dp)
                                    .weight(1f)
                            ) {

                                BasicText(
                                    text = nextNextMediaItem.mediaMetadata.title?.toString()
                                        ?: "",
                                    style = typography.xxxs.semiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                BasicText(
                                    text = nextNextMediaItem.mediaMetadata.artist?.toString()
                                        ?: "",
                                    style = typography.xxxs.semiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth()
                    ) {
                        if (showButtonPlayerDownload)
                            DownloadStateIconButton(
                                icon = if (isDownloaded) R.drawable.downloaded else R.drawable.download,
                                color = if (isDownloaded) colorPalette.text else colorPalette.textDisabled,
                                downloadState = downloadState,
                                onClick = {
                                    manageDownload(
                                        context = context,
                                        songId = mediaItem.mediaId,
                                        songTitle = mediaItem.mediaMetadata.title.toString(),
                                        downloadState = isDownloaded
                                    )
                                },
                                modifier = Modifier
                                    //.padding(start = 12.dp)
                                    .size(24.dp)
                            )


                        if (showButtonPlayerAddToPlaylist)
                            IconButton(
                                icon = R.drawable.add_in_playlist,
                                color = colorPalette.text,
                                onClick = {
                                    menuState.display {
                                        MiniPlayerMenu(
                                            navController = navController,
                                            onDismiss = menuState::hide,
                                            mediaItem = mediaItem,
                                            binder = binder,
                                            onClosePlayer = {
                                                onDismiss()
                                                layoutState.collapseSoft()
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    //.padding(horizontal = 4.dp)
                                    .size(24.dp)
                            )



                        if (showButtonPlayerLoop)
                            IconButton(
                                icon = R.drawable.repeat,
                                color = if (trackLoopEnabled) colorPalette.text else colorPalette.textDisabled,
                                onClick = {
                                    trackLoopEnabled = !trackLoopEnabled
                                    if (effectRotationEnabled) isRotated = !isRotated
                                },
                                modifier = Modifier
                                    //.padding(horizontal = 4.dp)
                                    .size(24.dp)
                            )

                        if (showButtonPlayerShuffle)
                            IconButton(
                                icon = R.drawable.shuffle,
                                color = colorPalette.text,
                                enabled = true,
                                onClick = {
                                    binder?.player?.shuffleQueue()
                                    binder.player.forceSeekToNext()
                                },
                                modifier = Modifier
                                    .size(24.dp),
                            )

                        if (showButtonPlayerLyrics)
                            IconButton(
                                icon = R.drawable.song_lyrics,
                                color = if (isShowingLyrics) colorPalette.text else colorPalette.textDisabled,
                                enabled = true,
                                onClick = {
                                    if (isShowingEqualizer) isShowingEqualizer = !isShowingEqualizer
                                    isShowingLyrics = !isShowingLyrics
                                },
                                modifier = Modifier
                                    .size(24.dp),
                            )


                        if (playerVisualizerType != PlayerVisualizerType.Disabled)
                            IconButton(
                                icon = R.drawable.sound_effect,
                                color = if (isShowingEqualizer) colorPalette.text else colorPalette.textDisabled,
                                enabled = true,
                                onClick = {
                                    if (isShowingLyrics) isShowingLyrics = !isShowingLyrics
                                    isShowingEqualizer = !isShowingEqualizer
                                },
                                modifier = Modifier
                                    .size(24.dp)
                            )


                        if (showButtonPlayerSleepTimer)
                            IconButton(
                                icon = R.drawable.sleep,
                                color = if (sleepTimerMillisLeft != null) colorPalette.text else colorPalette.textDisabled,
                                enabled = true,
                                onClick = {
                                    isShowingSleepTimerDialog = true
                                },
                                modifier = Modifier
                                    .size(24.dp),
                            )

                        if (showButtonPlayerSystemEqualizer) {
                            val activityResultLauncher =
                                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

                            IconButton(
                                icon = R.drawable.equalizer,
                                color = colorPalette.text,
                                enabled = true,
                                onClick = {
                                    try {
                                        activityResultLauncher.launch(
                                            Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                                putExtra(
                                                    AudioEffect.EXTRA_AUDIO_SESSION,
                                                    binder.player.audioSessionId
                                                )
                                                putExtra(
                                                    AudioEffect.EXTRA_PACKAGE_NAME,
                                                    context.packageName
                                                )
                                                putExtra(
                                                    AudioEffect.EXTRA_CONTENT_TYPE,
                                                    AudioEffect.CONTENT_TYPE_MUSIC
                                                )
                                            }
                                        )
                                    } catch (e: ActivityNotFoundException) {
                                        SmartToast(
                                            context.resources.getString(R.string.info_not_find_application_audio),
                                            type = PopupType.Warning
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .size(20.dp),
                            )
                        }

                        if (showButtonPlayerArrow)
                            IconButton(
                                icon = R.drawable.chevron_up,
                                color = colorPalette.text,
                                enabled = true,
                                onClick = {
                                    showQueue = true
                                },
                                modifier = Modifier
                                    //.padding(end = 12.dp)
                                    .size(24.dp),
                            )

                        if (showButtonPlayerMenu && !isLandscape)
                            IconButton(
                                icon = R.drawable.ellipsis_vertical,
                                color = colorPalette.text,
                                onClick = {
                                    menuState.display {
                                        PlayerMenu(
                                            navController = navController,
                                            onDismiss = menuState::hide,
                                            mediaItem = mediaItem,
                                            binder = binder,
                                            onClosePlayer = {
                                                onDismiss()
                                                layoutState.collapseSoft()
                                            }

                                        )
                                    }
                                },
                                modifier = Modifier
                                    //.padding(end = 12.dp)
                                    .size(24.dp)
                            )


                        if (isLandscape) {
                            IconButton(
                                icon = R.drawable.ellipsis_horizontal,
                                color = colorPalette.text,
                                onClick = {
                                    menuState.display {
                                        PlayerMenu(
                                            navController = navController,
                                            onDismiss = menuState::hide,
                                            mediaItem = mediaItem,
                                            binder = binder,
                                            onClosePlayer = {
                                                onDismiss()
                                                layoutState.collapseSoft()
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(24.dp)
                            )
                        }
                    }


                }
            }
        }

        if (isLandscape) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = containerModifier
                    .padding(top = 40.dp)
                    .drawBehind {
                        if (backgroundProgress == BackgroundProgress.Both || backgroundProgress == BackgroundProgress.Player) {
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
                Column (
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxHeight()
                       // .border(BorderStroke(1.dp, Color.Blue))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            //.padding(vertical = 10.dp)
                    ) {

                        thumbnailContent(
                            modifier = Modifier
                                .padding(all = 12.dp)
                                //.padding(horizontal = 10.dp)
                        )
                    }
                }
                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    controlsContent(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxHeight()
                            .weight(1f)
                    )

                    actionsBarContent(
                        modifier = Modifier
                    )
                }

            }
        } else {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = containerModifier
                    //.padding(top = 10.dp)
                    .drawBehind {
                        if (backgroundProgress == BackgroundProgress.Both || backgroundProgress == BackgroundProgress.Player) {
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


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(
                            windowInsets
                                .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                                .asPaddingValues()
                        )
                        //.padding(top = 5.dp)
                        .fillMaxWidth(0.9f)
                        .height(30.dp)
                ) {

                    if (showTopActionsBar) {
                        Image(
                            painter = painterResource(R.drawable.chevron_down),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
                            modifier = Modifier
                                .clickable {
                                    onDismiss()
                                }
                                .rotate(rotationAngle)
                                //.padding(10.dp)
                                .size(24.dp)
                        )


                        Image(
                            painter = painterResource(R.drawable.app_icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
                            modifier = Modifier
                                .clickable {
                                    onDismiss()
                                    navController.navigate(NavRoutes.home.name)
                                }
                                .rotate(rotationAngle)
                                //.padding(10.dp)
                                .size(24.dp)

                        )

                        if (!showButtonPlayerMenu)
                            Image(
                                painter = painterResource(R.drawable.ellipsis_vertical),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorPalette.collapsedPlayerProgressBar),
                                modifier = Modifier
                                    .clickable {
                                        menuState.display {
                                            PlayerMenu(
                                                navController = navController,
                                                onDismiss = menuState::hide,
                                                mediaItem = mediaItem,
                                                binder = binder,
                                                onClosePlayer = {
                                                    onDismiss()
                                                }
                                            )
                                        }
                                    }
                                    .rotate(rotationAngle)
                                    //.padding(10.dp)
                                    .size(24.dp)

                            )

                    }
                }

                Spacer(
                    modifier = Modifier
                        .height(5.dp)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1.2f)
                ) {
                    thumbnailContent(
                        modifier = Modifier
                            .clip(thumbnailShape)
                            .padding(
                                horizontal = playerThumbnailSize.size.dp,
                                vertical = 4.dp
                            )
                    )
                }


                if (showTotalTimeQueue)
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


                Spacer(
                    modifier = Modifier
                        .height(10.dp)
                )



                controlsContent(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()
                        .weight(1f)
                )

                actionsBarContent(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                )
            }
        }


        /*
        Queue(
            navController = navController,
            layoutState = queueSheetState,
            content = {},
            backgroundColorProvider = { colorPalette.background2 },
            modifier = Modifier
                .align(Alignment.BottomCenter),
            shape = shape
        )
         */
        CustomModalBottomSheet(
            showSheet = showQueue,
            onDismissRequest = { showQueue = false },
            containerColor = colorPalette.background2,
            contentColor = colorPalette.background2,
            modifier = Modifier.fillMaxWidth(),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = {
                Surface(
                    modifier = Modifier.padding(vertical = 0.dp),
                    color = colorPalette.background0,
                    shape = thumbnailShape
                ) {}
            }
        ) {
            QueueModern(
                navController = navController,
                onDismiss = { showQueue = false },
            )
        }



        /*
        FullLyricsSheet(
            layoutState = lyricsBottomSheetState,
            content = {},
            backgroundColorProvider = { colorPalette.background2 },
            onMaximize = { lyricsBottomSheetState.collapseSoft() },
            onRefresh = {
                lyricsBottomSheetState.collapse(tween(50))
                lyricsBottomSheetState.expand(tween(50))
            }
        )
         */

        CustomModalBottomSheet(
            showSheet = showFullLyrics,
            onDismissRequest = { showFullLyrics = false },
            containerColor = colorPalette.background2,
            contentColor = colorPalette.background2,
            modifier = Modifier.fillMaxWidth(),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = {
                Surface(
                    modifier = Modifier.padding(vertical = 0.dp),
                    color = colorPalette.background0,
                    shape = thumbnailShape
                ) {}
            }
        ) {
            FullLyricsSheetModern(
                onMaximize = { showFullLyrics = false },
                onRefresh = {}
            )
        }

    }

}



