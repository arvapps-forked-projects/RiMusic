package it.fast4x.rimusic.ui.screens.player

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.models.bodies.NextBody
import it.fast4x.innertube.requests.lyrics
import it.fast4x.kugou.KuGou
import it.fast4x.lrclib.LrcLib
import it.fast4x.lrclib.models.Track
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.LyricsFontSize
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Lyrics
import it.fast4x.rimusic.query
import it.fast4x.rimusic.transaction
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.themed.DefaultDialog
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.InputTextDialog
import it.fast4x.rimusic.ui.components.themed.Menu
import it.fast4x.rimusic.ui.components.themed.MenuEntry
import it.fast4x.rimusic.ui.components.themed.SmartToast
import it.fast4x.rimusic.ui.components.themed.TextPlaceholder
import it.fast4x.rimusic.ui.components.themed.ValueSelectorDialogBody
import it.fast4x.rimusic.ui.styling.DefaultDarkColorPalette
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.PureBlackColorPalette
import it.fast4x.rimusic.ui.styling.onOverlayShimmer
import it.fast4x.rimusic.ui.styling.overlay
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.SynchronizedLyrics
import it.fast4x.rimusic.utils.TextCopyToClipboard
import it.fast4x.rimusic.utils.UiTypeKey
import it.fast4x.rimusic.utils.bold
import it.fast4x.rimusic.utils.center
import it.fast4x.rimusic.utils.color
import it.fast4x.rimusic.utils.getHttpClient
import it.fast4x.rimusic.utils.isShowingSynchronizedLyricsKey
import it.fast4x.rimusic.utils.languageDestination
import it.fast4x.rimusic.utils.lyricsFontSizeKey
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.showBackgroundLyricsKey
import it.fast4x.rimusic.utils.thumbnail
import it.fast4x.rimusic.utils.toast
import it.fast4x.rimusic.utils.verticalFadingEdge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bush.translator.Language
import me.bush.translator.Translator
import okhttp3.internal.toImmutableList
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


@UnstableApi
@Composable
fun Lyrics(
    mediaId: String,
    isDisplayed: Boolean,
    onDismiss: () -> Unit,
    onMaximize: () -> Unit,
    size: Dp,
    mediaMetadataProvider: () -> MediaMetadata,
    durationProvider: () -> Long,
    ensureSongInserted: () -> Unit,
    modifier: Modifier = Modifier,
    enableClick: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = isDisplayed,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val coroutineScope = rememberCoroutineScope()
        val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
        val context = LocalContext.current
        val menuState = LocalMenuState.current
        val currentView = LocalView.current
        val binder = LocalPlayerServiceBinder.current
        val player = binder?.player

        var isShowingSynchronizedLyrics by rememberPreference(isShowingSynchronizedLyricsKey, false)
        var invalidLrc by remember(mediaId, isShowingSynchronizedLyrics) { mutableStateOf(false) }
        var isPicking by remember(mediaId, isShowingSynchronizedLyrics) { mutableStateOf(false) }

        val thumbnailSize = Dimensions.thumbnails.player.song

        var isEditing by remember(mediaId, isShowingSynchronizedLyrics) {
            mutableStateOf(false)
        }

        var showPlaceholder by remember {
            mutableStateOf(false)
        }

        var lyrics by remember {
            mutableStateOf<Lyrics?>(null)
        }

        val text = if (isShowingSynchronizedLyrics) lyrics?.synced else lyrics?.fixed

        var isError by remember(mediaId, isShowingSynchronizedLyrics) {
            mutableStateOf(false)
        }
        var isErrorSync by remember(mediaId, isShowingSynchronizedLyrics) {
            mutableStateOf(false)
        }

        val languageDestination = languageDestination()

        var translateEnabled by remember {
            mutableStateOf(false)
        }

        val translator = Translator(getHttpClient())

        var copyToClipboard by remember {
            mutableStateOf(false)
        }

        if (copyToClipboard) text?.let {
                TextCopyToClipboard(it)
        }

        var fontSize by rememberPreference(lyricsFontSizeKey, LyricsFontSize.Medium)
        val showBackgroundLyrics by rememberPreference(showBackgroundLyricsKey, false)

        LaunchedEffect(mediaId, isShowingSynchronizedLyrics) {
            withContext(Dispatchers.IO) {

                Database.lyrics(mediaId).collect { currentLyrics ->
                    if (isShowingSynchronizedLyrics && currentLyrics?.synced == null) {
                        lyrics = null
                        val mediaMetadata = mediaMetadataProvider()
                        var duration = withContext(Dispatchers.Main) {
                            durationProvider()
                        }

                        while (duration == C.TIME_UNSET) {
                            delay(100)
                            duration = withContext(Dispatchers.Main) {
                                durationProvider()
                            }
                        }

                        LrcLib.lyrics(
                            artist = mediaMetadata.artist?.toString() ?: "",
                            title = mediaMetadata.title?.toString() ?: "",
                            duration = duration.milliseconds,
                            album = mediaMetadata.albumTitle?.toString()
                        )?.onSuccess {
                            if (it?.text?.isNotEmpty() == true || it?.sentences?.isNotEmpty() == true)
                                coroutineScope.launch {
                                    SmartToast(
                                        context.getString(R.string.info_lyrics_found_on_s).format("LrcLib.net"),
                                        type = PopupType.Success
                                    )
                                }
                            else
                                coroutineScope.launch {
                                    SmartToast(
                                        context.getString(R.string.info_lyrics_not_found_on_s).format("LrcLib.net"),
                                        type = PopupType.Error,
                                        durationLong = true
                                    )
                                }

                            isError = false
                            Database.upsert(
                                Lyrics(
                                    songId = mediaId,
                                    fixed = currentLyrics?.fixed,
                                    synced = it?.text.orEmpty()
                                )
                            )
                        }?.onFailure {
                            coroutineScope.launch {
                                SmartToast(
                                    context.getString(R.string.info_lyrics_not_found_on_s_try_on_s).format("LrcLib.net", "KuGou.com"),
                                    type = PopupType.Error,
                                    durationLong = true
                                )
                            }
                            KuGou.lyrics(
                                artist = mediaMetadata.artist?.toString() ?: "",
                                title = mediaMetadata.title?.toString() ?: "",
                                duration = duration / 1000
                            )?.onSuccess {
                                if (it?.value?.isNotEmpty() == true || it?.sentences?.isNotEmpty() == true)
                                    coroutineScope.launch {
                                        SmartToast(
                                            context.getString(R.string.info_lyrics_found_on_s).format("KuGou.com"),
                                            type = PopupType.Success
                                        )
                                    }
                                else
                                    coroutineScope.launch {
                                        SmartToast(
                                            context.getString(R.string.info_lyrics_not_found_on_s).format("KuGou.com"),
                                            type = PopupType.Error,
                                            durationLong = true
                                        )
                                    }

                                isError = false
                                Database.upsert(
                                    Lyrics(
                                        songId = mediaId,
                                        fixed = currentLyrics?.fixed,
                                        synced = it?.value.orEmpty()
                                    )
                                )
                            }?.onFailure {
                                coroutineScope.launch {
                                    SmartToast(
                                        context.getString(R.string.info_lyrics_not_found_on_s).format("KuGou.com"),
                                        type = PopupType.Error,
                                        durationLong = true
                                    )
                                }
                                isError = true
                            }
                        }

                    } else if (!isShowingSynchronizedLyrics && currentLyrics?.fixed == null) {
                        isError = false
                        lyrics = null
                        Innertube.lyrics(NextBody(videoId = mediaId))?.onSuccess { fixedLyrics ->
                            Database.upsert(
                                Lyrics(
                                    songId = mediaId,
                                    fixed = fixedLyrics ?: "",
                                    synced = currentLyrics?.synced
                                )
                            )
                        }?.onFailure {
                            isError = true
                        }
                    } else {
                        lyrics = currentLyrics
                    }
                }

            }

        }


        if (isEditing) {
            InputTextDialog(
                onDismiss = { isEditing = false },
                setValueRequireNotNull = false,
                title = stringResource(R.string.enter_the_lyrics),
                value = text ?: "",
                placeholder = stringResource(R.string.enter_the_lyrics),
                setValue = {
                    query {
                        ensureSongInserted()
                        Database.upsert(
                            Lyrics(
                                songId = mediaId,
                                fixed = if (isShowingSynchronizedLyrics) lyrics?.fixed else it,
                                synced = if (isShowingSynchronizedLyrics) it else lyrics?.synced,
                            )
                        )
                    }

                }
            )
        }


        if (isPicking && isShowingSynchronizedLyrics) {
            var loading by remember { mutableStateOf(true) }
            val tracks = remember { mutableStateListOf<Track>() }
            var error by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val mediaMetadata = mediaMetadataProvider()
                LrcLib.lyrics(
                    artist = mediaMetadata.artist?.toString().orEmpty(),
                    title = mediaMetadata.title?.toString().orEmpty()
                )?.onSuccess {
                    if (it.isNotEmpty())
                        coroutineScope.launch {
                            SmartToast(
                                context.getString(R.string.info_lyrics_tracks_found_on_s).format("LrcLib.net"),
                                type = PopupType.Success
                            )
                        }
                    else
                        coroutineScope.launch {
                            SmartToast(
                                context.getString(R.string.info_lyrics_tracks_not_found_on_s).format("LrcLib.net"),
                                type = PopupType.Error,
                                durationLong = true
                            )
                        }
                    tracks.clear()
                    tracks.addAll(it)
                    loading = false
                    error = false
                }?.onFailure {
                    coroutineScope.launch {
                        SmartToast(
                            context.getString(R.string.an_error_has_occurred_while_fetching_the_lyrics).format("LrcLib.net"),
                            type = PopupType.Error,
                            durationLong = true
                        )
                    }
                    loading = false
                    error = true
                } ?: run { loading = false }
            }

            if (loading)
                DefaultDialog(
                    onDismiss = {
                        isPicking = false
                    }
                ) {
                   CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

            if (tracks.isNotEmpty()) {
                SelectLyricFromTrack(tracks = tracks, mediaId = mediaId, lyrics = lyrics)
                isPicking = false
            }
        }




        if (isShowingSynchronizedLyrics) {
            DisposableEffect(Unit) {
                currentView.keepScreenOn = true
                onDispose {
                    currentView.keepScreenOn = false
                }
            }
        }

        /*
        AsyncImage(
            model = player?.currentMediaItem?.mediaMetadata?.artworkUri.thumbnail(
                Dimensions.thumbnails.song.px
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(all = 10.dp)
                .clip(thumbnailShape)
                .background(colorPalette.overlay)
                .fillMaxSize(0.9f)
                .aspectRatio(1f)
        )
         */

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDismiss() }
                    )
                }
                .fillMaxSize()
                .background(Color.Black.copy(0.8f))

        ) {
            AnimatedVisibility(
                visible = (isError && text == null) || (invalidLrc && isShowingSynchronizedLyrics),
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                BasicText(
                    text = stringResource(R.string.an_error_has_occurred_while_fetching_the_lyrics),
                    style = typography.xs.center.medium.color(PureBlackColorPalette.text),
                    modifier = Modifier
                        .background(Color.Black.copy(0.4f))
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = text?.let(String::isEmpty) ?: false,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                BasicText(
                    text = "${
                        if (isShowingSynchronizedLyrics) stringResource(R.string.synchronized_lyrics) else stringResource(
                            R.string.unsynchronized_lyrics
                        )
                    } " +
                            "${stringResource(R.string.are_not_available_for_this_song)}",
                    style = typography.xs.center.medium.color(PureBlackColorPalette.text),
                    modifier = Modifier
                        .background(Color.Black.copy(0.4f))
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                )
                BasicText(
                    text = "${stringResource(R.string.click_to_switch_to)} ${if(isShowingSynchronizedLyrics) stringResource(R.string.unsynchronized_lyrics) else stringResource(
                        R.string.synchronized_lyrics)}",
                    style = typography.xs.center.bold.color(PureBlackColorPalette.text),
                    modifier = Modifier
                        .background(Color.Black.copy(0.4f))
                        .padding(all = 8.dp)
                        .padding(top = 30.dp)
                        .fillMaxWidth()
                        .clickable {
                            isShowingSynchronizedLyrics = !isShowingSynchronizedLyrics
                        }
                )
            }


            if (text?.isNotEmpty() == true) {
                if (isShowingSynchronizedLyrics) {
                    val density = LocalDensity.current
                    val player = LocalPlayerServiceBinder.current?.player
                        ?: return@AnimatedVisibility

                    val synchronizedLyrics = remember(text) {
                        val sentences = LrcLib.Lyrics(text).sentences

                        run {
                            invalidLrc = false
                            SynchronizedLyrics(sentences) {
                                player.currentPosition + 50L //- (lyrics?.startTime ?: 0L)
                            }
                        }
                    }

                    val lazyListState = rememberLazyListState()

                    LaunchedEffect(synchronizedLyrics, density) {
                        //val centerOffset = with(density) { (-thumbnailSize / 3).roundToPx() }
                        val centerOffset = with(density) {
                            (-thumbnailSize.div(if (trailingContent == null) 3 else 2))
                                .roundToPx()
                        }

                        lazyListState.animateScrollToItem(
                            index = synchronizedLyrics.index + 1,
                            scrollOffset = centerOffset
                        )

                        while (isActive) {
                            delay(50)
                            if (!synchronizedLyrics.update()) continue

                            lazyListState.animateScrollToItem(
                                index = synchronizedLyrics.index + 1,
                                scrollOffset = centerOffset
                            )
                        }
                    }

                    var modifierBG = Modifier.verticalFadingEdge()
                    if (showBackgroundLyrics) modifierBG = modifierBG.background(colorPalette.accent)

                    LazyColumn(
                        state = lazyListState,
                        userScrollEnabled = true,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = modifierBG
                    ) {
                        item(key = "header", contentType = 0) {
                            Spacer(modifier = Modifier.height(thumbnailSize))
                        }
                        itemsIndexed(
                            items = synchronizedLyrics.sentences
                        ) { index, sentence ->
                            var translatedText by remember { mutableStateOf("") }
                            if (translateEnabled) {
                                LaunchedEffect(Unit) {
                                    val result = withContext(Dispatchers.IO) {
                                        try {
                                            translator.translate(
                                                sentence.second,
                                                languageDestination,
                                                Language.AUTO
                                            ).translatedText
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    translatedText =
                                        if (result.toString() == "kotlin.Unit") "" else result.toString()
                                    showPlaceholder = false
                                }
                            } else translatedText = sentence.second
                            BasicText(
                                text = translatedText,
                                style = when (fontSize) {
                                    LyricsFontSize.Light ->
                                        typography.m.center.medium.color(if (index == synchronizedLyrics.index) PureBlackColorPalette.text else PureBlackColorPalette.textDisabled)
                                    LyricsFontSize.Medium ->
                                        typography.l.center.medium.color(if (index == synchronizedLyrics.index) PureBlackColorPalette.text else PureBlackColorPalette.textDisabled)
                                    LyricsFontSize.Heavy ->
                                        typography.xl.center.medium.color(if (index == synchronizedLyrics.index) PureBlackColorPalette.text else PureBlackColorPalette.textDisabled)
                                },
                                modifier = Modifier
                                    .padding(vertical = 4.dp, horizontal = 32.dp)
                                    .clickable {
                                        if (enableClick)
                                            binder?.player?.seekTo(sentence.first)
                                    }
                            )
                        }
                        item(key = "footer", contentType = 0) {
                            Spacer(modifier = Modifier.height(thumbnailSize))
                        }
                    }
                } else {
                    var translatedText by remember { mutableStateOf("") }
                    if (translateEnabled == true) {
                        LaunchedEffect(Unit) {
                            val result = withContext(Dispatchers.IO) {
                                try {
                                    translator.translate(
                                        text,
                                        languageDestination,
                                        Language.AUTO
                                    ).translatedText
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            translatedText =
                                if (result.toString() == "kotlin.Unit") "" else result.toString()
                            showPlaceholder = false
                        }
                    } else translatedText = text

                    BasicText(
                        text = translatedText,
                        style = when (fontSize) {
                            LyricsFontSize.Light ->
                                typography.m.center.medium.color(PureBlackColorPalette.text)
                            LyricsFontSize.Medium ->
                                typography.l.center.medium.color(PureBlackColorPalette.text)
                            LyricsFontSize.Heavy ->
                                typography.xl.center.medium.color(PureBlackColorPalette.text)
                        },
                        modifier = Modifier
                            .verticalFadingEdge()
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth()
                            .padding(vertical = size / 4, horizontal = 32.dp)
                    )
                }
            }

            if ((text == null && !isError) || showPlaceholder) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .shimmer()
                ) {
                    repeat(4) {
                        TextPlaceholder(
                            color = colorPalette.onOverlayShimmer,
                            modifier = Modifier
                                .alpha(1f - it * 0.1f)
                        )
                    }
                }
            }

            /**********/
            if (trailingContent != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.4f)
                ) {
                        trailingContent()
                  }
            }
            /*********/


            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(if (trailingContent == null) 0.30f else 0.22f)
            ) {
                IconButton(
                    icon = R.drawable.minmax,
                    color = DefaultDarkColorPalette.text,
                    enabled = true,
                    onClick = onMaximize,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .align(Alignment.BottomStart)
                        .size(20.dp)
                )

                IconButton(
                    icon = R.drawable.text,
                    color = DefaultDarkColorPalette.text,
                    enabled = true,
                    onClick = {
                            menuState.display {
                            Menu {
                                MenuEntry(
                                    icon = R.drawable.text,
                                    text = stringResource(R.string.light),
                                    secondaryText = "",
                                    onClick = {
                                        menuState.hide()
                                        fontSize = LyricsFontSize.Light
                                    }
                                )
                                MenuEntry(
                                    icon = R.drawable.text,
                                    text = stringResource(R.string.medium),
                                    secondaryText = "",
                                    onClick = {
                                        menuState.hide()
                                        fontSize = LyricsFontSize.Medium
                                    }
                                )
                                MenuEntry(
                                    icon = R.drawable.text,
                                    text = stringResource(R.string.heavy),
                                    secondaryText = "",
                                    onClick = {
                                        menuState.hide()
                                        fontSize = LyricsFontSize.Heavy
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth(0.2f)
            ) {

                IconButton(
                    icon = R.drawable.translate,
                    color = if (translateEnabled == true) colorPalette.text else colorPalette.textDisabled,
                    enabled = true,
                    onClick = {
                        translateEnabled = !translateEnabled
                        showPlaceholder = if (!translateEnabled) false else true
                    },
                    modifier = Modifier
                        //.padding(horizontal = 8.dp)
                        .padding(bottom = 10.dp)
                        .align(Alignment.BottomStart)
                        .size(24.dp)
                )
                Image(
                    painter = painterResource(R.drawable.ellipsis_vertical),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(DefaultDarkColorPalette.text),
                    modifier = Modifier
                        .padding(all = 4.dp)
                        .clickable(
                            indication = rememberRipple(bounded = false),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                menuState.display {
                                    Menu {
                                        MenuEntry(
                                            icon = R.drawable.time,
                                            text = stringResource(R.string.show) + " ${
                                                if (isShowingSynchronizedLyrics) stringResource(
                                                    R.string.unsynchronized_lyrics
                                                ) else stringResource(R.string.synchronized_lyrics)
                                            }",
                                            secondaryText = if (isShowingSynchronizedLyrics) null else stringResource(
                                                R.string.provided_by
                                            ) + " kugou.com and LrcLib.net",
                                            onClick = {
                                                menuState.hide()
                                                isShowingSynchronizedLyrics =
                                                    !isShowingSynchronizedLyrics
                                            }
                                        )

                                        MenuEntry(
                                            icon = R.drawable.title_edit,
                                            text = stringResource(R.string.edit_lyrics),
                                            onClick = {
                                                menuState.hide()
                                                isEditing = true
                                            }
                                        )

                                        MenuEntry(
                                            icon = R.drawable.copy,
                                            text = stringResource(R.string.copy_lyrics),
                                            onClick = {
                                                menuState.hide()
                                                copyToClipboard = true
                                            }
                                        )

                                        MenuEntry(
                                            icon = R.drawable.search,
                                            text = stringResource(R.string.search_lyrics_online),
                                            onClick = {
                                                menuState.hide()
                                                val mediaMetadata = mediaMetadataProvider()

                                                try {
                                                    context.startActivity(
                                                        Intent(Intent.ACTION_WEB_SEARCH).apply {
                                                            putExtra(
                                                                SearchManager.QUERY,
                                                                "${mediaMetadata.title} ${mediaMetadata.artist} lyrics"
                                                            )
                                                        }
                                                    )
                                                } catch (e: ActivityNotFoundException) {
                                                    SmartToast(
                                                        context.getString(R.string.info_not_find_app_browse_internet),
                                                        type = PopupType.Warning
                                                    )
                                                }
                                            }
                                        )

                                        MenuEntry(
                                            icon = R.drawable.sync,
                                            text = stringResource(R.string.fetch_lyrics_again),
                                            enabled = lyrics != null,
                                            onClick = {
                                                menuState.hide()
                                                query {
                                                    Database.upsert(
                                                        Lyrics(
                                                            songId = mediaId,
                                                            fixed = if (isShowingSynchronizedLyrics) lyrics?.fixed else null,
                                                            synced = if (isShowingSynchronizedLyrics) null else lyrics?.synced,
                                                        )
                                                    )
                                                }
                                            }
                                        )

                                        if (isShowingSynchronizedLyrics) {
                                            MenuEntry(
                                                icon = R.drawable.sync,
                                                text = stringResource(R.string.pick_from) + " LrcLib.net",
                                                onClick = {
                                                    menuState.hide()
                                                    isPicking = true
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                        .padding(all = 8.dp)
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }
    }
}


@Composable
fun SelectLyricFromTrack (
    tracks: List<Track>,
    mediaId: String,
    lyrics: Lyrics?
) {
    val menuState = LocalMenuState.current

    menuState.display {
        Menu {
            MenuEntry(
                icon = R.drawable.chevron_back,
                text = stringResource(R.string.cancel),
                onClick = { menuState.hide() }
            )
            tracks.forEach {
                MenuEntry(
                    icon = R.drawable.text,
                    text = "${it.artistName} - ${it.trackName}",
                    secondaryText = "(${stringResource(R.string.sort_duration)} ${
                        it.duration.seconds.toComponents { minutes, seconds, _ ->
                            "$minutes:${seconds.toString().padStart(2, '0')}"
                        }
                    } ${stringResource(R.string.id)} ${it.id}) ",
                    onClick = {
                        menuState.hide()
                        transaction {
                            Database.upsert(
                                Lyrics(
                                    songId = mediaId,
                                    fixed = lyrics?.fixed,
                                    synced = it.syncedLyrics.orEmpty()
                                )
                            )
                        }
                    }
                )
            }
            MenuEntry(
                icon = R.drawable.chevron_back,
                text = stringResource(R.string.cancel),
                onClick = { menuState.hide() }
            )
        }
    }
}