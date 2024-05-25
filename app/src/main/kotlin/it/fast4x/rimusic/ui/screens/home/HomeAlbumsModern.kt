package it.fast4x.rimusic.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.fast4x.compose.persist.persist
import it.fast4x.compose.persist.persistList
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerAwareWindowInsets
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.AlbumSortBy
import it.fast4x.rimusic.enums.ArtistSortBy
import it.fast4x.rimusic.enums.LibraryItemSize
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.PlaylistSortBy
import it.fast4x.rimusic.enums.SortOrder
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Album
import it.fast4x.rimusic.models.Artist
import it.fast4x.rimusic.models.Info
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.query
import it.fast4x.rimusic.transaction
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.themed.AlbumsItemMenu
import it.fast4x.rimusic.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.fast4x.rimusic.ui.components.themed.Header
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.HeaderInfo
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.InputTextDialog
import it.fast4x.rimusic.ui.components.themed.Menu
import it.fast4x.rimusic.ui.components.themed.MenuEntry
import it.fast4x.rimusic.ui.components.themed.MultiFloatingActionsContainer
import it.fast4x.rimusic.ui.components.themed.SelectorDialog
import it.fast4x.rimusic.ui.components.themed.SortMenu
import it.fast4x.rimusic.ui.components.themed.TitleSection
import it.fast4x.rimusic.ui.items.AlbumItem
import it.fast4x.rimusic.ui.items.ArtistItem
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.UiTypeKey
import it.fast4x.rimusic.utils.addNext
import it.fast4x.rimusic.utils.albumSortByKey
import it.fast4x.rimusic.utils.albumSortOrderKey
import it.fast4x.rimusic.utils.albumsItemSizeKey
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.enqueue
import it.fast4x.rimusic.utils.libraryItemSizeKey
import it.fast4x.rimusic.utils.navigationBarPositionKey
import it.fast4x.rimusic.utils.preferences
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.showFloatingIconKey
import it.fast4x.rimusic.utils.showSearchTabKey
import kotlinx.coroutines.coroutineScope
import kotlin.random.Random

@ExperimentalTextApi
@UnstableApi
@SuppressLint("SuspiciousIndentation")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeAlbumsModern(
    onAlbumClick: (Album) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val menuState = LocalMenuState.current
    val binder = LocalPlayerServiceBinder.current
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)

    var sortBy by rememberPreference(albumSortByKey, AlbumSortBy.DateAdded)
    var sortOrder by rememberPreference(albumSortOrderKey, SortOrder.Descending)

    var items by persist<List<Album>>(tag = "home/albums", emptyList())

    LaunchedEffect(sortBy, sortOrder) {
        Database.albums(sortBy, sortOrder).collect { items = it }
    }

    var itemSize by rememberPreference(albumsItemSizeKey, LibraryItemSize.Small.size)
    val thumbnailSizeDp = itemSize.dp + 24.dp
    val thumbnailSizePx = thumbnailSizeDp.px

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing), label = ""
    )

    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Left)
/*
    var showSortTypeSelectDialog by remember {
        mutableStateOf(false)
    }

 */

    val lazyListState = rememberLazyListState()

    val showSearchTab by rememberPreference(showSearchTabKey, false)
    //val effectRotationEnabled by rememberPreference(effectRotationKey, true)
    var isRotated by rememberSaveable { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 360F else 0f,
        animationSpec = tween(durationMillis = 300), label = ""
    )

    val lazyGridState = rememberLazyGridState()




    Box (
        modifier = Modifier
        .background(colorPalette.background0)
        //.fillMaxSize()
        .fillMaxHeight()
        .fillMaxWidth(if (navigationBarPosition == NavigationBarPosition.Left ||
            navigationBarPosition == NavigationBarPosition.Top ||
            navigationBarPosition == NavigationBarPosition.Bottom) 1f
        else Dimensions.contentWidthRightBar)
    ) {


        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Adaptive(itemSize.dp + 24.dp),
            //contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
        ) {

            item(
                key = "header",
                contentType = 0,
                span = { GridItemSpan(maxLineSpan) }
            ) {
                if (uiType == UiType.ViMusic)
                    HeaderWithIcon(
                        title = stringResource(R.string.albums),
                        iconId = R.drawable.search,
                        enabled = true,
                        showIcon = !showSearchTab,
                        modifier = Modifier,
                        onClick = onSearchClick
                    )

            }

            item(
                key = "headerNew",
                contentType = 0,
                span = { GridItemSpan(maxLineSpan) }
            ) {

                Row (
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 10.dp, bottom = 16.dp)
                        .fillMaxWidth()
                ){
                    if (uiType == UiType.RiMusic)
                        TitleSection(title = stringResource(R.string.albums))

                    HeaderInfo(
                        title = "${items.size}",
                        icon = painterResource(R.drawable.album),
                        spacer = 0
                    )

                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )

                    HeaderIconButton(
                        icon = R.drawable.arrow_up,
                        color = colorPalette.text,
                        onClick = {},
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .graphicsLayer { rotationZ = sortOrderIconRotation }
                            .combinedClickable(
                                onClick = { sortOrder = !sortOrder },
                                onLongClick = {
                                    menuState.display{
                                        SortMenu(
                                            title = stringResource(R.string.sorting_order),
                                            onDismiss = menuState::hide,
                                            onTitle = { sortBy = AlbumSortBy.Title },
                                            onYear = { sortBy = AlbumSortBy.Year },
                                            onDateAdded = { sortBy = AlbumSortBy.DateAdded },
                                        )
                                    }
                                }
                            )
                    )

                    HeaderIconButton(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .rotate(rotationAngle),
                        icon = R.drawable.dice,
                        enabled = items.isNotEmpty() ,
                        color = colorPalette.text,
                        onClick = {
                            isRotated = !isRotated
                            //onAlbumClick(items.get((0..<items.size).random()))
                            onAlbumClick(items.get(
                                Random(System.currentTimeMillis()).nextInt(0, items.size-1)
                            ))
                        },
                        iconSize = 16.dp
                    )

                    HeaderIconButton(
                        onClick = {
                            menuState.display {
                                Menu {
                                    MenuEntry(
                                        icon = R.drawable.arrow_forward,
                                        text = stringResource(R.string.small),
                                        onClick = {
                                            itemSize = LibraryItemSize.Small.size
                                            menuState.hide()
                                        }
                                    )
                                    MenuEntry(
                                        icon = R.drawable.arrow_forward,
                                        text = stringResource(R.string.medium),
                                        onClick = {
                                            itemSize = LibraryItemSize.Medium.size
                                            menuState.hide()
                                        }
                                    )
                                    MenuEntry(
                                        icon = R.drawable.arrow_forward,
                                        text = stringResource(R.string.big),
                                        onClick = {
                                            itemSize = LibraryItemSize.Big.size
                                            menuState.hide()
                                        }
                                    )
                                }
                            }
                        },
                        icon = R.drawable.resize,
                        color = colorPalette.text,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )


                    /*
                    BasicText(
                        text = when (sortBy) {
                            AlbumSortBy.Title -> stringResource(R.string.sort_title)
                            AlbumSortBy.Year -> stringResource(R.string.sort_year)
                            AlbumSortBy.DateAdded -> stringResource(R.string.sort_date_added)
                        },
                        style = typography.xs.semiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clickable {
                                menuState.display{
                                    SortMenu(
                                        title = stringResource(R.string.sorting_order),
                                        onDismiss = menuState::hide,
                                        onTitle = { sortBy = AlbumSortBy.Title },
                                        onYear = { sortBy = AlbumSortBy.Year },
                                        onDateAdded = { sortBy = AlbumSortBy.DateAdded },
                                    )
                                }
                                //showSortTypeSelectDialog = true
                            }
                    )
                    */


                }
            }

            items(
                items = items,
                key = Album::id
            ) { album ->
                var songs = remember { listOf<Song>() }
                query {
                    songs = Database.albumSongsList(album.id)
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

                if (showDialogChangeAlbumTitle)
                    InputTextDialog(
                        onDismiss = { showDialogChangeAlbumTitle = false },
                        title = stringResource(R.string.update_title),
                        value = album.title.toString(),
                        placeholder = stringResource(R.string.title),
                        setValue = {
                            if (it.isNotEmpty()) {
                                query {
                                    Database.updateAlbumTitle(album.id, it)
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
                                    Database.updateAlbumAuthors(album.id, it)
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
                                    Database.updateAlbumCover(album.id, it)
                                }
                                //context.toast("Album Saved $it")
                            }
                        }
                    )

                var position by remember {
                    mutableIntStateOf(0)
                }


                AlbumItem(
                    alternative = true,
                    album = album,
                    thumbnailSizePx = thumbnailSizePx,
                    thumbnailSizeDp = thumbnailSizeDp,
                    modifier = Modifier
                        .combinedClickable(

                            onLongClick = {
                                menuState.display {
                                    AlbumsItemMenu(
                                        onDismiss = menuState::hide,
                                        album = album,
                                        onChangeAlbumTitle = {
                                            showDialogChangeAlbumTitle = true
                                        },
                                        onChangeAlbumAuthors = {
                                            showDialogChangeAlbumAuthors = true
                                        },
                                        onChangeAlbumCover = {
                                            showDialogChangeAlbumCover = true
                                        },
                                        onPlayNext = {
                                            println("mediaItem ${songs}")
                                            binder?.player?.addNext(
                                                songs.map(Song::asMediaItem)
                                            )

                                        },
                                        onEnqueue = {
                                            println("mediaItem ${songs}")
                                            binder?.player?.enqueue(
                                                songs.map(Song::asMediaItem)
                                            )

                                        },
                                        onAddToPlaylist = { playlistPreview ->
                                            position =
                                                playlistPreview.songCount.minus(1) ?: 0
                                            //Log.d("mediaItem", " maxPos in Playlist $it ${position}")
                                            if (position > 0) position++ else position =
                                                0
                                            //Log.d("mediaItem", "next initial pos ${position}")
                                            //if (listMediaItems.isEmpty()) {
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
                                            //}
                                        }
                                    )
                                }
                            },
                            onClick = {
                                onAlbumClick(album)
                            }
                        )
                        .clip(thumbnailShape)
                        .animateItemPlacement()
                )
            }

            item(
                key = "footer",
                contentType = 0,
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Spacer(modifier = Modifier.height(Dimensions.bottomSpacer))
            }
        }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)

        val showFloatingIcon by rememberPreference(showFloatingIconKey, false)
        if(uiType == UiType.ViMusic || showFloatingIcon)
            MultiFloatingActionsContainer(
                iconId = R.drawable.search,
                onClick = onSearchClick,
                onClickSettings = onSettingsClick,
                onClickSearch = onSearchClick
            )

        /*
        FloatingActionsContainerWithScrollToTop(
                lazyListState = lazyListState,
                iconId = R.drawable.search,
                onClick = onSearchClick
            )
         */


    }
}
