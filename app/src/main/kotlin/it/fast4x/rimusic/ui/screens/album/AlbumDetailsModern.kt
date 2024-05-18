package it.fast4x.rimusic.ui.screens.album

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import it.fast4x.compose.persist.persist
import it.fast4x.compose.persist.persistList
import it.fast4x.innertube.Innertube
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerAwareWindowInsets
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Album
import it.fast4x.rimusic.models.Info
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.query
import it.fast4x.rimusic.service.isLocal
import it.fast4x.rimusic.transaction
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.ShimmerHost
import it.fast4x.rimusic.ui.components.themed.AlbumsItemMenu
import it.fast4x.rimusic.ui.components.themed.AutoResizeText
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.components.themed.FontSizeRange
import it.fast4x.rimusic.ui.components.themed.Header
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.InputTextDialog
import it.fast4x.rimusic.ui.components.themed.ItemsList
import it.fast4x.rimusic.ui.components.themed.LayoutWithAdaptiveThumbnail
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.components.themed.NonQueuedMediaItemMenu
import it.fast4x.rimusic.ui.components.themed.NowPlayingShow
import it.fast4x.rimusic.ui.components.themed.SelectorDialog
import it.fast4x.rimusic.ui.components.themed.SmartToast
import it.fast4x.rimusic.ui.items.AlbumItem
import it.fast4x.rimusic.ui.items.AlbumItemPlaceholder
import it.fast4x.rimusic.ui.items.SongItem
import it.fast4x.rimusic.ui.items.SongItemPlaceholder
import it.fast4x.rimusic.ui.screens.searchresult.ItemsPage
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.UiTypeKey
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.align
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.center
import it.fast4x.rimusic.utils.color
import it.fast4x.rimusic.utils.downloadedStateMedia
import it.fast4x.rimusic.utils.durationTextToMillis
import it.fast4x.rimusic.utils.enqueue
import it.fast4x.rimusic.utils.fadingEdge
import it.fast4x.rimusic.utils.forcePlayAtIndex
import it.fast4x.rimusic.utils.forcePlayFromBeginning
import it.fast4x.rimusic.utils.formatAsTime
import it.fast4x.rimusic.utils.getDownloadState
import it.fast4x.rimusic.utils.getHttpClient
import it.fast4x.rimusic.utils.isLandscape
import it.fast4x.rimusic.utils.languageDestination
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.navigationBarPositionKey
import it.fast4x.rimusic.utils.preferences
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.resize
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.showFloatingIconKey
import it.fast4x.rimusic.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.bush.translator.Language
import me.bush.translator.Translator
import java.text.SimpleDateFormat
import java.util.Date

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@UnstableApi
@Composable
fun AlbumDetailsModern(
    navController: NavController,
    browseId: String,
    headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    thumbnailContent: @Composable () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    var songs by persistList<Song>("album/$browseId/songs")
    var album by persist<Album?>("album/$browseId")
    val albumPage by persist<Innertube.PlaylistOrAlbumPage?>("album/$browseId/albumPage")

    LaunchedEffect(Unit) {
        Database.albumSongs(browseId).collect { songs = it }
    }
    LaunchedEffect(Unit) {
        Database.album(browseId).collect { album = it }
    }

    /*
    val playlistPreviews by remember {
        Database.playlistPreviews(PlaylistSortBy.Name, SortOrder.Ascending)
    }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

    var showPlaylistSelectDialog by remember {
        mutableStateOf(false)
    }
     */

    var showConfirmDeleteDownloadDialog by remember {
        mutableStateOf(false)
    }

    var showConfirmDownloadAllDialog by remember {
        mutableStateOf(false)
    }

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailAlbumSizeDp = Dimensions.thumbnails.album

    val thumbnailAlbumSizePx = thumbnailAlbumSizeDp.px

    val lazyListState = rememberLazyListState()

    val context = LocalContext.current
    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }

    var listMediaItems = remember {
        mutableListOf<MediaItem>()
    }

    var selectItems by remember {
        mutableStateOf(false)
    }

    var showSelectDialog by remember {
        mutableStateOf(false)
    }

    /*
    var showAddPlaylistSelectDialog by remember {
        mutableStateOf(false)
    }
     */

    var showSelectCustomizeAlbumDialog by remember {
        mutableStateOf(false)
    }
    var showDialogChangeAlbumTitle by remember {
        mutableStateOf(false)
    }
    var showDialogChangeAlbumAuthors by remember {
        mutableStateOf(false)
    }
    var showDialogChangeAlbumCover by remember {
        mutableStateOf(false)
    }
    var isCreatingNewPlaylist by rememberSaveable {
        mutableStateOf(false)
    }
    var totalPlayTimes = 0L
    songs.forEach {
        totalPlayTimes += it.durationText?.let { it1 ->
            durationTextToMillis(it1) }?.toLong() ?: 0
    }
    var position by remember {
        mutableIntStateOf(0)
    }

    var scrollToNowPlaying by remember {
        mutableStateOf(false)
    }

    var nowPlayingItem by remember {
        mutableStateOf(-1)
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(album?.thumbnailUrl)
            .size(Size.ORIGINAL)
            .build()
    )

    var bitmap = remember<Bitmap?> {
        null
    }
    val imageState = painter.state

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            context.applicationContext.contentResolver.openOutputStream(uri)
                ?.use { outputStream ->
                    if (imageState is AsyncImagePainter.State.Success) {
                        bitmap = imageState.result.drawable.toBitmap()
                        try {
                            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.flush()
                            outputStream.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            SmartToast(context.getString(R.string.info_error), type = PopupType.Error)
                        }
                    } else SmartToast(context.getString(R.string.info_error), type = PopupType.Error)
                }

        }

    if (showSelectCustomizeAlbumDialog)
        SelectorDialog(
            title = stringResource(R.string.customize_album),
            onDismiss = { showSelectCustomizeAlbumDialog = false },
            values = listOf(
                Info("t", stringResource(R.string.update_title)),
                Info("a", stringResource(R.string.update_authors)),
                Info("c", stringResource(R.string.update_cover))
            ),
            onValueSelected = {
                when (it) {
                    "t" -> showDialogChangeAlbumTitle = true
                    "a" -> showDialogChangeAlbumAuthors = true
                    "c" -> showDialogChangeAlbumCover = true
                }
                showSelectCustomizeAlbumDialog = false
            }
        )

    if (showDialogChangeAlbumTitle)
        InputTextDialog(
            onDismiss = { showDialogChangeAlbumTitle = false },
            title = stringResource(R.string.update_title),
            value = album?.title.toString(),
            placeholder = stringResource(R.string.title),
            setValue = {
                if (it.isNotEmpty()) {
                    query {
                        Database.updateAlbumTitle(browseId, it)
                    }
                    //context.toast("Album Saved $it")
                }
            }
        )
    if (showDialogChangeAlbumAuthors)
        InputTextDialog(
            onDismiss = { showDialogChangeAlbumAuthors = false },
            title = stringResource(R.string.update_authors),
            value = album?.authorsText.toString(),
            placeholder = stringResource(R.string.authors),
            setValue = {
                if (it.isNotEmpty()) {
                    query {
                        Database.updateAlbumAuthors(browseId, it)
                    }
                    //context.toast("Album Saved $it")
                }
            }
        )

    if (showDialogChangeAlbumCover)
        InputTextDialog(
            onDismiss = { showDialogChangeAlbumCover = false },
            title = stringResource(R.string.update_cover),
            value = album?.thumbnailUrl.toString(),
            placeholder = stringResource(R.string.cover),
            setValue = {
                if (it.isNotEmpty()) {
                    query {
                        Database.updateAlbumCover(browseId, it)
                    }
                    //context.toast("Album Saved $it")
                }
            }
        )

    if (isCreatingNewPlaylist)
        InputTextDialog(
            onDismiss = { isCreatingNewPlaylist = false },
            title = stringResource(R.string.new_playlist),
            value = "",
            placeholder = stringResource(R.string.new_playlist),
            setValue = {
                if (it.isNotEmpty()) {
                    query {
                        Database.insert(Playlist(name = it))
                    }
                    //context.toast("Song Saved $it")
                }
            }
        )

    if (showConfirmDeleteDownloadDialog) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_delete_download),
            onDismiss = { showConfirmDeleteDownloadDialog = false },
            onConfirm = {
                showConfirmDeleteDownloadDialog = false
                downloadState = Download.STATE_DOWNLOADING
                if (songs.isNotEmpty() == true)
                    songs.forEach {
                        binder?.cache?.removeResource(it.asMediaItem.mediaId)
                        manageDownload(
                            context = context,
                            songId = it.asMediaItem.mediaId,
                            songTitle = it.asMediaItem.mediaMetadata.title.toString(),
                            downloadState = true
                        )
                    }
            }
        )
    }

    if (showConfirmDownloadAllDialog) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_download_all),
            onDismiss = { showConfirmDownloadAllDialog = false },
            onConfirm = {
                showConfirmDownloadAllDialog = false
                downloadState = Download.STATE_DOWNLOADING
                if (songs.isNotEmpty() == true)
                    songs.forEach {
                        binder?.cache?.removeResource(it.asMediaItem.mediaId)
                        query {
                            Database.insert(
                                Song(
                                    id = it.asMediaItem.mediaId,
                                    title = it.asMediaItem.mediaMetadata.title.toString(),
                                    artistsText = it.asMediaItem.mediaMetadata.artist.toString(),
                                    thumbnailUrl = it.thumbnailUrl,
                                    durationText = null
                                )
                            )
                        }
                        manageDownload(
                            context = context,
                            songId = it.asMediaItem.mediaId,
                            songTitle = it.asMediaItem.mediaMetadata.title.toString(),
                            downloadState = false
                        )
                    }
            }
        )
    }

    if (showSelectDialog)
        SelectorDialog(
            title = stringResource(R.string.enqueue),
            onDismiss = { showSelectDialog = false },
            values = listOf(
                Info("a", stringResource(R.string.enqueue_all)),
                Info("s", stringResource(R.string.enqueue_selected))
            ),
            onValueSelected = {
                if (it == "a") {
                    binder?.player?.enqueue(songs.map(Song::asMediaItem))
                } else selectItems = true

                showSelectDialog = false
            }
        )

    LaunchedEffect(scrollToNowPlaying) {
        if (scrollToNowPlaying)
            lazyListState.scrollToItem(nowPlayingItem, 1)
        scrollToNowPlaying = false
    }

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)

    var translateEnabled by remember {
        mutableStateOf(false)
    }

    val translator = Translator(getHttpClient())
    val languageDestination = languageDestination()

    LayoutWithAdaptiveThumbnail(thumbnailContent = thumbnailContent) {
        Box(
            modifier = Modifier
                .background(colorPalette.background0)
                //.fillMaxSize()
                .fillMaxHeight()
                //.fillMaxWidth(if (navigationBarPosition == NavigationBarPosition.Left) 1f else contentWidth)
                .fillMaxWidth()
        ) {

            LazyColumn(
                    state = lazyListState,
                    //contentPadding = LocalPlayerAwareWindowInsets.current
                    //    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
                    modifier = Modifier
                        .background(colorPalette.background0)
                        .fillMaxSize()
                ) {
                    item(
                        key = "header"
                    ) {

                        val modifierArt = if (isLandscape) Modifier.fillMaxWidth() else Modifier.fillMaxWidth().aspectRatio(4f / 3)

                        Box(
                            modifier = modifierArt
                        ) {
                            if (album != null) {
                                if(!isLandscape)
                                    AsyncImage(
                                        model = album?.thumbnailUrl?.resize(1200, 900),
                                        contentDescription = "loading...",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.Center)
                                            .fadingEdge(
                                                top = WindowInsets.systemBars
                                                    .asPaddingValues()
                                                    .calculateTopPadding() + Dimensions.fadeSpacingTop,
                                                bottom = Dimensions.fadeSpacingBottom
                                            )
                                    )

                                AutoResizeText(
                                    text = album?.title ?: "",
                                    style = typography.l.semiBold,
                                    fontSizeRange = FontSizeRange(32.sp, 38.sp),
                                    fontWeight = typography.l.semiBold.fontWeight,
                                    fontFamily = typography.l.semiBold.fontFamily,
                                    color = typography.l.semiBold.color,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(horizontal = 30.dp)
                                        .padding(bottom = 20.dp)
                                )

                                BasicText(
                                    text = songs.size.toString() + " "
                                            + stringResource(R.string.songs)
                                            + " - " + formatAsTime(totalPlayTimes),
                                    style = typography.xs.medium,
                                    maxLines = 1,
                                    modifier = Modifier
                                        //.padding(top = 10.dp)
                                        .align(Alignment.BottomCenter)
                                )

                                HeaderIconButton(
                                    icon = R.drawable.share_social,
                                    color = colorPalette.text,
                                    iconSize = 24.dp,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 5.dp, end= 5.dp),
                                    onClick = {
                                        album?.shareUrl?.let { url ->
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, url)
                                            }

                                            context.startActivity(
                                                Intent.createChooser(
                                                    sendIntent,
                                                    null
                                                )
                                            )
                                        }
                                    }
                                )

                            } else {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(4f / 3)
                                ) {
                                    ShimmerHost {
                                        AlbumItemPlaceholder(
                                            thumbnailSizeDp = 200.dp,
                                            alternative = true
                                        )
                                        BasicText(
                                            text = stringResource(R.string.info_wait_it_may_take_a_few_minutes),
                                            style = typography.xs.medium,
                                            maxLines = 1,
                                            modifier = Modifier
                                            //.padding(top = 10.dp)

                                        )
                                    }
                                }
                            }
                        }

                    }

                    item(
                        key = "actions",
                        contentType = 0
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .fillMaxWidth()
                        ) {
                            //headerContent {
                            HeaderIconButton(
                                icon = if (album?.bookmarkedAt == null) {
                                    R.drawable.bookmark_outline
                                } else {
                                    R.drawable.bookmark
                                },
                                color = colorPalette.accent,
                                modifier = Modifier
                                    .padding(horizontal = 25.dp)
                                    .combinedClickable(
                                        onClick = {
                                            val bookmarkedAt =
                                                if (album?.bookmarkedAt == null) System.currentTimeMillis() else null

                                            query {
                                                album
                                                    ?.copy(bookmarkedAt = bookmarkedAt)
                                                    ?.let(Database::update)
                                            }
                                        },
                                        onLongClick = {
                                            SmartToast(context.getString(R.string.info_bookmark_album))
                                        }
                                    ),
                                onClick = {}
                            )
                                HeaderIconButton(
                                    icon = R.drawable.downloaded,
                                    color = colorPalette.text,
                                    onClick = {},
                                    modifier = Modifier
                                        .padding(horizontal = 5.dp)
                                        .combinedClickable(
                                            onClick = {
                                                showConfirmDownloadAllDialog = true
                                            },
                                            onLongClick = {
                                                SmartToast(context.getString(R.string.info_download_all_songs))
                                            }
                                        )
                                )

                                HeaderIconButton(
                                    icon = R.drawable.download,
                                    color = colorPalette.text,
                                    onClick = {},
                                    modifier = Modifier
                                        .padding(horizontal = 5.dp)
                                        .combinedClickable(
                                            onClick = {
                                                showConfirmDeleteDownloadDialog = true
                                            },
                                            onLongClick = {
                                                SmartToast(context.getString(R.string.info_remove_all_downloaded_songs))
                                            }
                                        )
                                )


                                /*
                            HeaderIconButton(
                                icon = R.drawable.enqueue,
                                enabled = songs.isNotEmpty(),
                                color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                                onClick = {
                                    if (!selectItems)
                                    showSelectDialog = true else {
                                        binder?.player?.enqueue(listMediaItems)
                                        listMediaItems.clear()
                                        selectItems = false
                                    }

                                }
                            )
                             */



                                HeaderIconButton(
                                    icon = R.drawable.shuffle,
                                    enabled = songs.isNotEmpty(),
                                    color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                                    onClick = {},
                                    modifier = Modifier
                                        .padding(horizontal = 5.dp)
                                        .combinedClickable(
                                            onClick = {
                                                if (songs.isNotEmpty()) {
                                                    binder?.stopRadio()
                                                    binder?.player?.forcePlayFromBeginning(
                                                        songs.shuffled().map(Song::asMediaItem)
                                                    )
                                                }
                                            },
                                            onLongClick = {
                                                SmartToast(context.getString(R.string.info_shuffle))
                                            }
                                        )
                                )

                                HeaderIconButton(
                                    modifier = Modifier
                                        .padding(horizontal = 5.dp)
                                        .combinedClickable(
                                            onClick = {
                                                nowPlayingItem = -1
                                                scrollToNowPlaying = false
                                                songs
                                                    .forEachIndexed { index, song ->
                                                        if (song.asMediaItem.mediaId == binder?.player?.currentMediaItem?.mediaId)
                                                            nowPlayingItem = index
                                                    }

                                                if (nowPlayingItem > -1)
                                                    scrollToNowPlaying = true
                                            },
                                            onLongClick = {
                                                SmartToast(context.getString(R.string.info_find_the_song_that_is_playing))
                                            }
                                        ),
                                    icon = R.drawable.locate,
                                    enabled = songs.isNotEmpty(),
                                    color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                                    onClick = {}


                                )


                                HeaderIconButton(
                                    modifier = Modifier
                                        .padding(horizontal = 5.dp),
                                    icon = R.drawable.ellipsis_horizontal,
                                    enabled = songs.isNotEmpty(),
                                    color = if (songs.isNotEmpty()) colorPalette.text else colorPalette.textDisabled,
                                    onClick = {
                                        menuState.display {
                                            album?.let {
                                                AlbumsItemMenu(
                                                    onDismiss = menuState::hide,
                                                    onSelectUnselect = {
                                                        selectItems = !selectItems
                                                        if (!selectItems) {
                                                            listMediaItems.clear()
                                                        }
                                                    },
                                                    /*
                                                onSelect = { selectItems = true },
                                                onUncheck = {
                                                    selectItems = false
                                                    listMediaItems.clear()
                                                },
                                                 */
                                                    onChangeAlbumTitle = {
                                                        showDialogChangeAlbumTitle = true
                                                    },
                                                    onChangeAlbumAuthors = {
                                                        showDialogChangeAlbumAuthors = true
                                                    },
                                                    onChangeAlbumCover = {
                                                        showDialogChangeAlbumCover = true
                                                    },
                                                    onDownloadAlbumCover = {
                                                        try {
                                                            @SuppressLint("SimpleDateFormat")
                                                            val dateFormat =
                                                                SimpleDateFormat("yyyyMMddHHmmss")
                                                            exportLauncher.launch(
                                                                "ImageCover_${
                                                                    dateFormat.format(
                                                                        Date()
                                                                    )
                                                                }"
                                                            )
                                                        } catch (e: ActivityNotFoundException) {
                                                            SmartToast(
                                                                "Couldn't find an application to create documents",
                                                                type = PopupType.Warning
                                                            )
                                                        }
                                                    },
                                                    onPlayNext = {
                                                        if (listMediaItems.isEmpty()) {
                                                            binder?.player?.addNext(songs.map(Song::asMediaItem))
                                                        } else {
                                                            binder?.player?.addNext(listMediaItems)
                                                            listMediaItems.clear()
                                                            selectItems = false
                                                        }
                                                    },
                                                    onEnqueue = {
                                                        if (listMediaItems.isEmpty()) {
                                                            binder?.player?.enqueue(songs.map(Song::asMediaItem))
                                                        } else {
                                                            binder?.player?.enqueue(listMediaItems)
                                                            listMediaItems.clear()
                                                            selectItems = false
                                                        }
                                                    },
                                                    album = it,
                                                    onAddToPlaylist = { playlistPreview ->
                                                        position =
                                                            playlistPreview.songCount.minus(1) ?: 0
                                                        //Log.d("mediaItem", " maxPos in Playlist $it ${position}")
                                                        if (position > 0) position++ else position =
                                                            0
                                                        //Log.d("mediaItem", "next initial pos ${position}")
                                                        if (listMediaItems.isEmpty()) {
                                                            songs.forEachIndexed { index, song ->
                                                                transaction {
                                                                    Database.insert(song.asMediaItem)
                                                                    Database.insert(
                                                                        SongPlaylistMap(
                                                                            songId = song.asMediaItem.mediaId,
                                                                            playlistId = playlistPreview.playlist.id,
                                                                            position = position + index
                                                                        )
                                                                    )
                                                                }
                                                                //Log.d("mediaItemPos", "added position ${position + index}")
                                                            }
                                                        } else {
                                                            listMediaItems.forEachIndexed { index, song ->
                                                                //Log.d("mediaItemMaxPos", position.toString())
                                                                transaction {
                                                                    Database.insert(song)
                                                                    Database.insert(
                                                                        SongPlaylistMap(
                                                                            songId = song.mediaId,
                                                                            playlistId = playlistPreview.playlist.id,
                                                                            position = position + index
                                                                        )
                                                                    )
                                                                }
                                                                //Log.d("mediaItemPos", "add position $position")
                                                            }
                                                            listMediaItems.clear()
                                                            selectItems = false
                                                        }
                                                    },
                                                )
                                            }
                                        }

                                    }
                                )

                        }
                    }

                    item (
                        key = "songsTitle"
                    ) {
                        BasicText(
                            text = stringResource(R.string.songs),
                            style = typography.m.semiBold.align(TextAlign.Start),
                            modifier = sectionTextModifier
                                .fillMaxWidth()
                        )
                    }
                    itemsIndexed(
                        items = songs,
                        key = { _, song -> song.id }
                    ) { index, song ->
                        val isLocal by remember { derivedStateOf { song.asMediaItem.isLocal } }
                        downloadState = getDownloadState(song.asMediaItem.mediaId)
                        val isDownloaded =
                            if (!isLocal) downloadedStateMedia(song.asMediaItem.mediaId) else true
                        val checkedState = remember { mutableStateOf(false) }
                        SongItem(
                            title = song.title,
                            totalPlayTimeMs = 1,
                            isDownloaded = isDownloaded,
                            downloadState = downloadState,
                            onDownloadClick = {
                                binder?.cache?.removeResource(song.asMediaItem.mediaId)
                                query {
                                    Database.insert(
                                        Song(
                                            id = song.asMediaItem.mediaId,
                                            title = song.asMediaItem.mediaMetadata.title.toString(),
                                            artistsText = song.asMediaItem.mediaMetadata.artist.toString(),
                                            thumbnailUrl = song.thumbnailUrl,
                                            durationText = null
                                        )
                                    )
                                }
                                if (!isLocal)
                                    manageDownload(
                                        context = context,
                                        songId = song.asMediaItem.mediaId,
                                        songTitle = song.asMediaItem.mediaMetadata.title.toString(),
                                        downloadState = isDownloaded
                                    )
                            },
                            authors = song.artistsText,
                            duration = song.durationText,
                            thumbnailSizeDp = thumbnailSizeDp,
                            thumbnailContent = {
                                /*
                                AsyncImage(
                                    model = song.thumbnailUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .clip(LocalAppearance.current.thumbnailShape)
                                        .fillMaxSize()
                                )
                                 */
                                BasicText(
                                    text = "${index + 1}",
                                    style = typography.s.semiBold.center.color(colorPalette.textDisabled),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .width(thumbnailSizeDp)
                                        .align(Alignment.Center)
                                )

                                if (nowPlayingItem > -1)
                                    NowPlayingShow(song.asMediaItem.mediaId)
                            },
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        menuState.display {
                                            NonQueuedMediaItemMenu(
                                                navController = navController,
                                                onDismiss = menuState::hide,
                                                mediaItem = song.asMediaItem,
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (!selectItems) {
                                            binder?.stopRadio()
                                            binder?.player?.forcePlayAtIndex(
                                                songs.map(Song::asMediaItem),
                                                index
                                            )
                                        } else checkedState.value = !checkedState.value
                                    }
                                ),
                            trailingContent = {
                                if (selectItems)
                                    Checkbox(
                                        checked = checkedState.value,
                                        onCheckedChange = {
                                            checkedState.value = it
                                            if (it) listMediaItems.add(song.asMediaItem) else
                                                listMediaItems.remove(song.asMediaItem)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = colorPalette.accent,
                                            uncheckedColor = colorPalette.text
                                        ),
                                        modifier = Modifier
                                            .scale(0.7f)
                                    )
                                else checkedState.value = false
                            },
                            mediaId = song.asMediaItem.mediaId
                        )
                    }


                    item(key = "alternateVersionsTitle") {
                        BasicText(
                            text = stringResource(R.string.album_alternative_versions),
                            style = typography.m.semiBold,
                            maxLines = 1,
                            modifier = Modifier
                                .padding(all = 16.dp)

                        )

                    }

                    item (key ="alternateVersions"){
                        ItemsList(
                            tag = "album/$browseId/alternatives",
                            headerContent = {},
                            initialPlaceholderCount = 1,
                            continuationPlaceholderCount = 1,
                            emptyItemsText = stringResource(R.string.album_no_alternative_version),
                            itemsPageProvider = albumPage?.let {
                                ({
                                    Result.success(
                                        Innertube.ItemsPage(
                                            items = albumPage?.otherVersions,
                                            continuation = null
                                        )
                                    )
                                })
                            },
                            itemContent = { album ->
                                AlbumItem(
                                    alternative = true,
                                    album = album,
                                    thumbnailSizePx = thumbnailAlbumSizePx,
                                    thumbnailSizeDp = thumbnailAlbumSizeDp,
                                    modifier = Modifier
                                        .clickable {
                                            //albumRoute(album.key)
                                            navController.navigate(route = "${NavRoutes.album.name}/${album.key}")
                                        }
                                )
                            },
                            itemPlaceholderContent = {
                                AlbumItemPlaceholder(thumbnailSizeDp = thumbnailSizeDp)
                            }
                        )

                        /**********/
                    }

                albumPage?.description?.let { description ->
                    item (
                        key = "albumInfo"
                    ) {

                        val attributionsIndex = description.lastIndexOf("\n\nFrom Wikipedia")

                        BasicText(
                            text = stringResource(R.string.information),
                            style = typography.m.semiBold.align(TextAlign.Start),
                            modifier = sectionTextModifier
                                .fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier
                                //.padding(top = 16.dp)
                                .padding(vertical = 16.dp, horizontal = 8.dp)
                            //.padding(endPaddingValues)
                            //.padding(end = Dimensions.bottomSpacer)
                        ) {
                            IconButton(
                                icon = R.drawable.translate,
                                color = if (translateEnabled == true) colorPalette.text else colorPalette.textDisabled,
                                enabled = true,
                                onClick = {},
                                modifier = Modifier
                                    .padding(all = 8.dp)
                                    .size(18.dp)
                                    .combinedClickable(
                                        onClick = {
                                            translateEnabled = !translateEnabled
                                        },
                                        onLongClick = {
                                            SmartToast(context.getString(R.string.info_translation))
                                        }
                                    )
                            )
                            BasicText(
                                text = "“",
                                style = typography.xxl.semiBold,
                                modifier = Modifier
                                    .offset(y = (-8).dp)
                                    .align(Alignment.Top)
                            )

                            var translatedText by remember { mutableStateOf("") }
                            val nonTranslatedText by remember { mutableStateOf(
                                if (attributionsIndex == -1) {
                                    description
                                } else {
                                    description.substring(0, attributionsIndex)
                                }
                            )
                            }


                            if (translateEnabled == true) {
                                LaunchedEffect(Unit) {
                                    val result = withContext(Dispatchers.IO) {
                                        try {
                                            translator.translate(
                                                nonTranslatedText,
                                                languageDestination,
                                                Language.AUTO
                                            ).translatedText
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    translatedText =
                                        if (result.toString() == "kotlin.Unit") "" else result.toString()
                                }
                            } else translatedText = nonTranslatedText

                            BasicText(
                                text = translatedText,
                                style = typography.xxs.secondary.align(TextAlign.Justify),
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .weight(1f)
                            )

                            BasicText(
                                text = "„",
                                style = typography.xxl.semiBold,
                                modifier = Modifier
                                    .offset(y = 4.dp)
                                    .align(Alignment.Bottom)
                            )
                        }

                        if (attributionsIndex != -1) {
                            BasicText(
                                text = stringResource(R.string.from_wikipedia_cca),
                                style = typography.xxs.color(colorPalette.textDisabled).align(
                                    TextAlign.Start),
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp)
                                //.padding(endPaddingValues)
                            )
                        }

                    }
                }

                    item(key = "bottom") {
                        Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
                    }

                    if (songs.isEmpty()) {
                        item(key = "loading") {
                            ShimmerHost(
                                modifier = Modifier
                                    .fillParentMaxSize()
                            ) {
                                repeat(1) {
                                    AlbumItemPlaceholder(thumbnailSizeDp = Dimensions.thumbnails.album)
                                }
                                repeat(4) {
                                    SongItemPlaceholder(thumbnailSizeDp = Dimensions.thumbnails.song)
                                }
                            }
                        }
                    }


                }






            val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
            if(uiType == UiType.ViMusic || showFloatingIcon)
                MultiFloatingActionsContainer(
                    iconId = R.drawable.shuffle,
                    onClick = {
                        if (songs.isNotEmpty()) {
                            binder?.stopRadio()
                            binder?.player?.forcePlayFromBeginning(
                                songs.shuffled().map(Song::asMediaItem)
                            )
                        }
                    },
                    onClickSettings = onSettingsClick,
                    onClickSearch = onSearchClick
                )

                /*
                FloatingActionsContainerWithScrollToTop(
                    lazyListState = lazyListState,
                    iconId = R.drawable.shuffle,
                    onClick = {
                        if (songs.isNotEmpty()) {
                            binder?.stopRadio()
                            binder?.player?.forcePlayFromBeginning(
                                songs.shuffled().map(Song::asMediaItem)
                            )
                        }
                    }
                )

                 */





        }


    }


}
