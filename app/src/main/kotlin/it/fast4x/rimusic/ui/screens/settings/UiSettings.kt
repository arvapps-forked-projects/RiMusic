package it.fast4x.rimusic.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.AudioQualityFormat
import it.fast4x.rimusic.enums.ColorPaletteMode
import it.fast4x.rimusic.enums.ColorPaletteName
import it.fast4x.rimusic.enums.DurationInSeconds
import it.fast4x.rimusic.enums.ExoPlayerMinTimeForEvent
import it.fast4x.rimusic.enums.FontType
import it.fast4x.rimusic.enums.Languages
import it.fast4x.rimusic.enums.MaxStatisticsItems
import it.fast4x.rimusic.enums.HomeScreenTabs
import it.fast4x.rimusic.enums.MaxSongs
import it.fast4x.rimusic.enums.MaxTopPlaylistItems
import it.fast4x.rimusic.enums.MenuStyle
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.NavigationBarType
import it.fast4x.rimusic.enums.PauseBetweenSongs
import it.fast4x.rimusic.enums.PlayerPlayButtonType
import it.fast4x.rimusic.enums.PlayerPosition
import it.fast4x.rimusic.enums.PlayerThumbnailSize
import it.fast4x.rimusic.enums.PlayerTimelineType
import it.fast4x.rimusic.enums.PlayerVisualizerType
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.RecommendationsNumber
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.enums.TransitionEffect
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.query
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.SmartToast
import it.fast4x.rimusic.ui.components.themed.Title
import it.fast4x.rimusic.ui.styling.DefaultDarkColorPalette
import it.fast4x.rimusic.ui.styling.DefaultLightColorPalette
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.utils.MaxTopPlaylistItemsKey
import it.fast4x.rimusic.utils.UiTypeKey
import it.fast4x.rimusic.utils.applyFontPaddingKey
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.audioQualityFormatKey
import it.fast4x.rimusic.utils.closeWithBackButtonKey
import it.fast4x.rimusic.utils.closebackgroundPlayerKey
import it.fast4x.rimusic.utils.colorPaletteModeKey
import it.fast4x.rimusic.utils.colorPaletteNameKey
import it.fast4x.rimusic.utils.customThemeDark_Background0Key
import it.fast4x.rimusic.utils.customThemeDark_Background1Key
import it.fast4x.rimusic.utils.customThemeDark_Background2Key
import it.fast4x.rimusic.utils.customThemeDark_Background3Key
import it.fast4x.rimusic.utils.customThemeDark_Background4Key
import it.fast4x.rimusic.utils.customThemeDark_TextKey
import it.fast4x.rimusic.utils.customThemeDark_accentKey
import it.fast4x.rimusic.utils.customThemeDark_iconButtonPlayerKey
import it.fast4x.rimusic.utils.customThemeDark_textDisabledKey
import it.fast4x.rimusic.utils.customThemeDark_textSecondaryKey
import it.fast4x.rimusic.utils.customThemeLight_Background0Key
import it.fast4x.rimusic.utils.customThemeLight_Background1Key
import it.fast4x.rimusic.utils.customThemeLight_Background2Key
import it.fast4x.rimusic.utils.customThemeLight_Background3Key
import it.fast4x.rimusic.utils.customThemeLight_Background4Key
import it.fast4x.rimusic.utils.customThemeLight_TextKey
import it.fast4x.rimusic.utils.customThemeLight_accentKey
import it.fast4x.rimusic.utils.customThemeLight_iconButtonPlayerKey
import it.fast4x.rimusic.utils.customThemeLight_textDisabledKey
import it.fast4x.rimusic.utils.customThemeLight_textSecondaryKey
import it.fast4x.rimusic.utils.disableClosingPlayerSwipingDownKey
import it.fast4x.rimusic.utils.disableIconButtonOnTopKey
import it.fast4x.rimusic.utils.disablePlayerHorizontalSwipeKey
import it.fast4x.rimusic.utils.enableCreateMonthlyPlaylistsKey
import it.fast4x.rimusic.utils.exoPlayerMinTimeForEventKey
import it.fast4x.rimusic.utils.fontTypeKey
import it.fast4x.rimusic.utils.indexNavigationTabKey
import it.fast4x.rimusic.utils.isAtLeastAndroid6
import it.fast4x.rimusic.utils.isSwipeToActionEnabledKey
import it.fast4x.rimusic.utils.keepPlayerMinimizedKey
import it.fast4x.rimusic.utils.languageAppKey
import it.fast4x.rimusic.utils.lastPlayerPlayButtonTypeKey
import it.fast4x.rimusic.utils.lastPlayerThumbnailSizeKey
import it.fast4x.rimusic.utils.lastPlayerTimelineTypeKey
import it.fast4x.rimusic.utils.lastPlayerVisualizerTypeKey
import it.fast4x.rimusic.utils.manageDownload
import it.fast4x.rimusic.utils.maxSongsInQueueKey
import it.fast4x.rimusic.utils.maxStatisticsItemsKey
import it.fast4x.rimusic.utils.menuStyleKey
import it.fast4x.rimusic.utils.navigationBarPositionKey
import it.fast4x.rimusic.utils.navigationBarTypeKey
import it.fast4x.rimusic.utils.pauseBetweenSongsKey
import it.fast4x.rimusic.utils.persistentQueueKey
import it.fast4x.rimusic.utils.playbackFadeDurationKey
import it.fast4x.rimusic.utils.playerPlayButtonTypeKey
import it.fast4x.rimusic.utils.playerPositionKey
import it.fast4x.rimusic.utils.playerThumbnailSizeKey
import it.fast4x.rimusic.utils.playerTimelineTypeKey
import it.fast4x.rimusic.utils.playerVisualizerTypeKey
import it.fast4x.rimusic.utils.recommendationsNumberKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.resumePlaybackWhenDeviceConnectedKey
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.shakeEventEnabledKey
import it.fast4x.rimusic.utils.showCachedPlaylistKey
import it.fast4x.rimusic.utils.showDownloadedPlaylistKey
import it.fast4x.rimusic.utils.showFavoritesPlaylistKey
import it.fast4x.rimusic.utils.showFloatingIconKey
import it.fast4x.rimusic.utils.showMonthlyPlaylistInLibraryKey
import it.fast4x.rimusic.utils.showMyTopPlaylistKey
import it.fast4x.rimusic.utils.showOnDevicePlaylistKey
import it.fast4x.rimusic.utils.showPlaylistsKey
import it.fast4x.rimusic.utils.showSearchTabKey
import it.fast4x.rimusic.utils.showStatsInNavbarKey
import it.fast4x.rimusic.utils.showStatsListeningTimeKey
import it.fast4x.rimusic.utils.skipSilenceKey
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import it.fast4x.rimusic.utils.thumbnailTapEnabledKey
import it.fast4x.rimusic.utils.transitionEffectKey
import it.fast4x.rimusic.utils.useSystemFontKey
import it.fast4x.rimusic.utils.volumeNormalizationKey



@OptIn(ExperimentalFoundationApi::class)
@ExperimentalAnimationApi
@UnstableApi
@Composable
fun  UiSettings() {
    val binder = LocalPlayerServiceBinder.current
    val context = LocalContext.current

    var languageApp  by rememberPreference(languageAppKey, Languages.English)
    val systemLocale = LocaleListCompat.getDefault().get(0).toString()

    var exoPlayerMinTimeForEvent by rememberPreference(
        exoPlayerMinTimeForEventKey,
        ExoPlayerMinTimeForEvent.`20s`
    )
    var persistentQueue by rememberPreference(persistentQueueKey, false)
    var closebackgroundPlayer by rememberPreference(closebackgroundPlayerKey, false)
    var closeWithBackButton by rememberPreference(closeWithBackButtonKey, true)
    var resumePlaybackWhenDeviceConnected by rememberPreference(
        resumePlaybackWhenDeviceConnectedKey,
        false
    )

    var skipSilence by rememberPreference(skipSilenceKey, false)
    var volumeNormalization by rememberPreference(volumeNormalizationKey, false)
    var audioQualityFormat by rememberPreference(audioQualityFormatKey, AudioQualityFormat.Auto)

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }


    var recommendationsNumber by rememberPreference(recommendationsNumberKey,   RecommendationsNumber.`5`)

    var keepPlayerMinimized by rememberPreference(keepPlayerMinimizedKey,   true)

    var disableIconButtonOnTop by rememberPreference(disableIconButtonOnTopKey, false)
    var lastPlayerVisualizerType by rememberPreference(lastPlayerVisualizerTypeKey, PlayerVisualizerType.Disabled)
    var lastPlayerTimelineType by rememberPreference(lastPlayerTimelineTypeKey, PlayerTimelineType.Default)
    var lastPlayerThumbnailSize by rememberPreference(lastPlayerThumbnailSizeKey, PlayerThumbnailSize.Medium)
    var uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
    var disablePlayerHorizontalSwipe by rememberPreference(disablePlayerHorizontalSwipeKey, false)
    var playerVisualizerType by rememberPreference(playerVisualizerTypeKey, PlayerVisualizerType.Disabled)
    var playerTimelineType by rememberPreference(playerTimelineTypeKey, PlayerTimelineType.Default)
    var playerThumbnailSize by rememberPreference(playerThumbnailSizeKey, PlayerThumbnailSize.Medium)
    var thumbnailTapEnabled by rememberPreference(thumbnailTapEnabledKey, false)
    var playerPlayButtonType by rememberPreference(playerPlayButtonTypeKey, PlayerPlayButtonType.Rectangular)
    var lastPlayerPlayButtonType by rememberPreference(lastPlayerPlayButtonTypeKey, PlayerPlayButtonType.Rectangular)

    var colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.ModernBlack)
    var colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.System)
    var indexNavigationTab by rememberPreference(
        indexNavigationTabKey,
        HomeScreenTabs.Default
    )
    var fontType by rememberPreference(fontTypeKey, FontType.Rubik)
    var useSystemFont by rememberPreference(useSystemFontKey, false)
    var applyFontPadding by rememberPreference(applyFontPaddingKey, false)
    var isSwipeToActionEnabled by rememberPreference(isSwipeToActionEnabledKey, true)
    var disableClosingPlayerSwipingDown by rememberPreference(disableClosingPlayerSwipingDownKey, true)
    var showSearchTab by rememberPreference(showSearchTabKey, false)
    var showStatsInNavbar by rememberPreference(showStatsInNavbarKey, false)

    var maxStatisticsItems by rememberPreference(
        maxStatisticsItemsKey,
        MaxStatisticsItems.`10`
    )

    var showStatsListeningTime by rememberPreference(showStatsListeningTimeKey,   true)

    var maxTopPlaylistItems by rememberPreference(
        MaxTopPlaylistItemsKey,
        MaxTopPlaylistItems.`10`
    )

    var navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Left)
    var navigationBarType by rememberPreference(navigationBarTypeKey, NavigationBarType.IconAndText)
    var pauseBetweenSongs  by rememberPreference(pauseBetweenSongsKey, PauseBetweenSongs.`0`)
    var maxSongsInQueue  by rememberPreference(maxSongsInQueueKey, MaxSongs.`500`)

    val (colorPalette, typography) = LocalAppearance.current
    var searching by rememberSaveable { mutableStateOf(false) }
    var filter: String? by rememberSaveable { mutableStateOf(null) }
   // var filterCharSequence: CharSequence
    var filterCharSequence: CharSequence = filter.toString()
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )

    var showFavoritesPlaylist by rememberPreference(showFavoritesPlaylistKey, true)
    var showCachedPlaylist by rememberPreference(showCachedPlaylistKey, true)
    var showMyTopPlaylist by rememberPreference(showMyTopPlaylistKey, true)
    var showDownloadedPlaylist by rememberPreference(showDownloadedPlaylistKey, true)
    var showOnDevicePlaylist by rememberPreference(showOnDevicePlaylistKey, true)
    var showPlaylists by rememberPreference(showPlaylistsKey, true)
    var shakeEventEnabled by rememberPreference(shakeEventEnabledKey, false)
    var showFloatingIcon by rememberPreference(showFloatingIconKey, false)
    var menuStyle by rememberPreference(menuStyleKey, MenuStyle.List)
    var transitionEffect by rememberPreference(transitionEffectKey, TransitionEffect.Scale)
    var enableCreateMonthlyPlaylists by rememberPreference(enableCreateMonthlyPlaylistsKey, true)
    var showMonthlyPlaylistInLibrary by rememberPreference(showMonthlyPlaylistInLibraryKey, true)

    var customThemeLight_Background0 by rememberPreference(customThemeLight_Background0Key, DefaultLightColorPalette.background0.hashCode())
    var customThemeLight_Background1 by rememberPreference(customThemeLight_Background1Key, DefaultLightColorPalette.background1.hashCode())
    var customThemeLight_Background2 by rememberPreference(customThemeLight_Background2Key, DefaultLightColorPalette.background2.hashCode())
    var customThemeLight_Background3 by rememberPreference(customThemeLight_Background3Key, DefaultLightColorPalette.background3.hashCode())
    var customThemeLight_Background4 by rememberPreference(customThemeLight_Background4Key, DefaultLightColorPalette.background4.hashCode())
    var customThemeLight_Text by rememberPreference(customThemeLight_TextKey, DefaultLightColorPalette.text.hashCode())
    var customThemeLight_TextSecondary by rememberPreference(customThemeLight_textSecondaryKey, DefaultLightColorPalette.textSecondary.hashCode())
    var customThemeLight_TextDisabled by rememberPreference(customThemeLight_textDisabledKey, DefaultLightColorPalette.textDisabled.hashCode())
    var customThemeLight_IconButtonPlayer by rememberPreference(customThemeLight_iconButtonPlayerKey, DefaultLightColorPalette.iconButtonPlayer.hashCode())
    var customThemeLight_Accent by rememberPreference(customThemeLight_accentKey, DefaultLightColorPalette.accent.hashCode())

    var customThemeDark_Background0 by rememberPreference(customThemeDark_Background0Key, DefaultDarkColorPalette.background0.hashCode())
    var customThemeDark_Background1 by rememberPreference(customThemeDark_Background1Key, DefaultDarkColorPalette.background1.hashCode())
    var customThemeDark_Background2 by rememberPreference(customThemeDark_Background2Key, DefaultDarkColorPalette.background2.hashCode())
    var customThemeDark_Background3 by rememberPreference(customThemeDark_Background3Key, DefaultDarkColorPalette.background3.hashCode())
    var customThemeDark_Background4 by rememberPreference(customThemeDark_Background4Key, DefaultDarkColorPalette.background4.hashCode())
    var customThemeDark_Text by rememberPreference(customThemeDark_TextKey, DefaultDarkColorPalette.text.hashCode())
    var customThemeDark_TextSecondary by rememberPreference(customThemeDark_textSecondaryKey, DefaultDarkColorPalette.textSecondary.hashCode())
    var customThemeDark_TextDisabled by rememberPreference(customThemeDark_textDisabledKey, DefaultDarkColorPalette.textDisabled.hashCode())
    var customThemeDark_IconButtonPlayer by rememberPreference(customThemeDark_iconButtonPlayerKey, DefaultDarkColorPalette.iconButtonPlayer.hashCode())
    var customThemeDark_Accent by rememberPreference(customThemeDark_accentKey, DefaultDarkColorPalette.accent.hashCode())

    var resetCustomLightThemeDialog by rememberSaveable { mutableStateOf(false) }
    var resetCustomDarkThemeDialog by rememberSaveable { mutableStateOf(false) }
    var playbackFadeDuration by rememberPreference(playbackFadeDurationKey, DurationInSeconds.Disabled)
    var playerPosition by rememberPreference(playerPositionKey, PlayerPosition.Bottom)


    Column(
        modifier = Modifier
            .background(colorPalette.background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(
                if (navigationBarPosition == NavigationBarPosition.Left ||
                    navigationBarPosition == NavigationBarPosition.Top ||
                    navigationBarPosition == NavigationBarPosition.Bottom
                ) 1f
                else Dimensions.contentWidthRightBar
            )
            .verticalScroll(rememberScrollState())
            /*
            .padding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues()
            )
             */
    ) {
        HeaderWithIcon(
            title = stringResource(R.string.user_interface),
            iconId = R.drawable.ui,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )

        HeaderIconButton(
            modifier = Modifier.padding(horizontal = 5.dp),
            onClick = { searching = !searching },
            icon = R.drawable.search_circle,
            color = colorPalette.text,
            iconSize = 24.dp
        )
        /*   Search   */
        Row (
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .padding(all = 10.dp)
                .fillMaxWidth()
        ) {
            AnimatedVisibility(visible = searching) {
                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current

                LaunchedEffect(searching) {
                    focusRequester.requestFocus()
                }

                BasicTextField(
                    value = filter ?: "",
                    onValueChange = { filter = it },
                    textStyle = typography.xs.semiBold,
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (filter.isNullOrBlank()) filter = ""
                        focusManager.clearFocus()
                    }),
                    cursorBrush = SolidColor(colorPalette.text),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                        ) {
                            IconButton(
                                onClick = {},
                                icon = R.drawable.search,
                                color = colorPalette.favoritesIcon,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .size(16.dp)
                            )
                        }
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 30.dp)
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = filter?.isEmpty() ?: true,
                                enter = fadeIn(tween(100)),
                                exit = fadeOut(tween(100)),
                            ) {
                                BasicText(
                                    text = stringResource(R.string.search),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = typography.xs.semiBold.secondary.copy(color = colorPalette.textDisabled),
                                )
                            }

                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth()
                        .background(
                            colorPalette.background4,
                            shape = thumbnailRoundness.shape()
                        )
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            if (!it.hasFocus) {
                                keyboardController?.hide()
                                if (filter?.isBlank() == true) {
                                    filter = null
                                    searching = false
                                }
                            }
                        }
                )
            }
        }
        /*  Search  */


        if (resetCustomLightThemeDialog) {
            ConfirmationDialog(
                text = stringResource(R.string.do_you_really_want_to_reset_the_custom_light_theme_colors),
                onDismiss = { resetCustomLightThemeDialog = false },
                onConfirm = {
                    resetCustomLightThemeDialog = false
                    customThemeLight_Background0 = DefaultLightColorPalette.background0.hashCode()
                    customThemeLight_Background1 = DefaultLightColorPalette.background1.hashCode()
                    customThemeLight_Background2 = DefaultLightColorPalette.background2.hashCode()
                    customThemeLight_Background3 = DefaultLightColorPalette.background3.hashCode()
                    customThemeLight_Background4 = DefaultLightColorPalette.background4.hashCode()
                    customThemeLight_Text = DefaultLightColorPalette.text.hashCode()
                    customThemeLight_TextSecondary = DefaultLightColorPalette.textSecondary.hashCode()
                    customThemeLight_TextDisabled = DefaultLightColorPalette.textDisabled.hashCode()
                    customThemeLight_IconButtonPlayer = DefaultLightColorPalette.iconButtonPlayer.hashCode()
                    customThemeLight_Accent = DefaultLightColorPalette.accent.hashCode()
                }
            )
        }

        if (resetCustomDarkThemeDialog) {
            ConfirmationDialog(
                text = stringResource(R.string.do_you_really_want_to_reset_the_custom_dark_theme_colors),
                onDismiss = { resetCustomDarkThemeDialog = false },
                onConfirm = {
                    resetCustomDarkThemeDialog = false
                    customThemeDark_Background0 = DefaultDarkColorPalette.background0.hashCode()
                    customThemeDark_Background1 = DefaultDarkColorPalette.background1.hashCode()
                    customThemeDark_Background2 = DefaultDarkColorPalette.background2.hashCode()
                    customThemeDark_Background3 = DefaultDarkColorPalette.background3.hashCode()
                    customThemeDark_Background4 = DefaultDarkColorPalette.background4.hashCode()
                    customThemeDark_Text = DefaultDarkColorPalette.text.hashCode()
                    customThemeDark_TextSecondary = DefaultDarkColorPalette.textSecondary.hashCode()
                    customThemeDark_TextDisabled = DefaultDarkColorPalette.textDisabled.hashCode()
                    customThemeDark_IconButtonPlayer = DefaultDarkColorPalette.iconButtonPlayer.hashCode()
                    customThemeDark_Accent = DefaultDarkColorPalette.accent.hashCode()
                }
            )
        }



        //SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.languages))

        SettingsDescription(text = stringResource(R.string.system_language)+": $systemLocale")

        if (filter.isNullOrBlank() || stringResource(R.string.app_language).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.app_language),
                selectedValue = languageApp,
                onValueSelected = {languageApp = it },
                valueText = {
                    when (it){
                        Languages.System -> stringResource(R.string.system_language)
                        Languages.Afrikaans -> stringResource(R.string.lang_afrikaans)
                        Languages.Arabic -> stringResource(R.string.arabic)
                        Languages.Bashkir -> stringResource(R.string.bashkir)
                        Languages.Catalan -> stringResource(R.string.catalan)
                        Languages.ChineseSimplified -> stringResource(R.string.chinese_simplified)
                        Languages.ChineseTraditional -> stringResource(R.string.chinese_traditional)
                        Languages.Czech -> stringResource(R.string.czech)
                        Languages.Danish -> stringResource(R.string.lang_danish)
                        Languages.Dutch -> stringResource(R.string.lang_dutch)
                        Languages.English -> stringResource(R.string.english)
                        Languages.Esperanto -> stringResource(R.string.esperanto)
                        Languages.Estonian -> stringResource(R.string.lang_estonian)
                        Languages.Filipino -> stringResource(R.string.lang_filipino)
                        Languages.Finnish -> stringResource(R.string.lang_finnish)
                        Languages.French -> stringResource(R.string.french)
                        Languages.German -> stringResource(R.string.german)
                        Languages.Greek -> stringResource(R.string.greek)
                        Languages.Hebrew -> stringResource(R.string.lang_hebrew)
                        Languages.Hindi -> stringResource(R.string.lang_hindi)
                        Languages.Hungarian -> stringResource(R.string.hungarian)
                        Languages.Indonesian -> stringResource(R.string.indonesian)
                        Languages.Japanese -> stringResource(R.string.lang_japanese)
                        Languages.Korean -> stringResource(R.string.korean)
                        Languages.Italian -> stringResource(R.string.italian)
                        Languages.Odia -> stringResource(R.string.odia)
                        Languages.Persian -> stringResource(R.string.persian)
                        Languages.Polish -> stringResource(R.string.polish)
                        Languages.PortugueseBrazilian -> stringResource(R.string.portuguese_brazilian)
                        Languages.Portuguese -> stringResource(R.string.portuguese)
                        Languages.Romanian -> stringResource(R.string.romanian)
                        //Languages.RomanianEmo -> stringResource(R.string.romanian_emoticons_rom_n)
                        Languages.Russian -> stringResource(R.string.russian)
                        Languages.SerbianCyrillic -> stringResource(R.string.lang_serbian_cyrillic)
                        Languages.SerbianLatin -> stringResource(R.string.lang_serbian_latin)
                        Languages.Sinhala -> stringResource(R.string.lang_sinhala)
                        Languages.Spanish -> stringResource(R.string.spanish)
                        Languages.Swedish -> stringResource(R.string.lang_swedish)
                        Languages.Telugu -> stringResource(R.string.lang_telugu)
                        Languages.Turkish -> stringResource(R.string.turkish)
                        Languages.Ukrainian -> stringResource(R.string.lang_ukrainian)
                        Languages.Vietnamese -> "Vietnamese"
                    }
                }
            )



        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.player))

        if (filter.isNullOrBlank() || stringResource(R.string.audio_quality_format).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.audio_quality_format),
                selectedValue = audioQualityFormat,
                onValueSelected = { audioQualityFormat = it },
                valueText = {
                    when (it) {
                        AudioQualityFormat.Auto -> stringResource(R.string.audio_quality_automatic)
                        AudioQualityFormat.High -> stringResource(R.string.audio_quality_format_high)
                        AudioQualityFormat.Medium -> stringResource(R.string.audio_quality_format_medium)
                        AudioQualityFormat.Low -> stringResource(R.string.audio_quality_format_low)
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.min_listening_time).contains(filterCharSequence,true)) {
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.min_listening_time),
                selectedValue = exoPlayerMinTimeForEvent,
                onValueSelected = { exoPlayerMinTimeForEvent = it },
                valueText = {
                    when (it) {
                        ExoPlayerMinTimeForEvent.`10s` -> "10s"
                        ExoPlayerMinTimeForEvent.`15s` -> "15s"
                        ExoPlayerMinTimeForEvent.`20s` -> "20s"
                        ExoPlayerMinTimeForEvent.`30s` -> "30s"
                        ExoPlayerMinTimeForEvent.`40s` -> "40s"
                        ExoPlayerMinTimeForEvent.`60s` -> "60s"
                    }
                }
            )
            SettingsDescription(text = stringResource(R.string.is_min_list_time_for_tips_or_quick_pics))
        }

        if (filter.isNullOrBlank() || stringResource(R.string.pause_between_songs).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.pause_between_songs),
                selectedValue = pauseBetweenSongs,
                onValueSelected = { pauseBetweenSongs = it },
                valueText = {
                    when (it) {
                        PauseBetweenSongs.`0` -> "0s"
                        PauseBetweenSongs.`5` -> "5s"
                        PauseBetweenSongs.`10` -> "10s"
                        PauseBetweenSongs.`15` -> "15s"
                        PauseBetweenSongs.`20` -> "20s"
                        PauseBetweenSongs.`30` -> "30s"
                        PauseBetweenSongs.`40` -> "40s"
                        PauseBetweenSongs.`50` -> "50s"
                        PauseBetweenSongs.`60` -> "60s"
                    }
                }
            )


        if (filter.isNullOrBlank() || stringResource(R.string.effect_fade_songs).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.effect_fade_songs),
                selectedValue = playbackFadeDuration,
                onValueSelected = { playbackFadeDuration = it },
                valueText = {
                    when (it) {
                        DurationInSeconds.Disabled -> stringResource(R.string.vt_disabled)
                        DurationInSeconds.`3` -> "3 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`4` -> "4 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`5` -> "5 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`6` -> "6 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`7` -> "7 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`8` -> "8 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`9` -> "9 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`10` -> "10 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`11` -> "11 %s".format(stringResource(R.string.time_seconds))
                        DurationInSeconds.`12` -> "12 %s".format(stringResource(R.string.time_seconds))
                    }
                }
            )


        if (filter.isNullOrBlank() || stringResource(R.string.player_keep_minimized).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.player_keep_minimized),
                text = stringResource(R.string.when_click_on_a_song_player_start_minimized),
                isChecked = keepPlayerMinimized,
                onCheckedChange = {
                    keepPlayerMinimized = it
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.player_collapsed_disable_swiping_down).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.player_collapsed_disable_swiping_down),
                text = stringResource(R.string.avoid_closing_the_player_cleaning_queue_by_swiping_down),
                isChecked = disableClosingPlayerSwipingDown,
                onCheckedChange = {
                    disableClosingPlayerSwipingDown = it
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.max_songs_in_queue).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.max_songs_in_queue),
                selectedValue = maxSongsInQueue,
                onValueSelected = { maxSongsInQueue = it },
                valueText = {
                    when (it) {
                        MaxSongs.Unlimited -> stringResource(R.string.unlimited)
                        MaxSongs.`50` -> MaxSongs.`50`.name
                        MaxSongs.`100` -> MaxSongs.`100`.name
                        MaxSongs.`200` -> MaxSongs.`200`.name
                        MaxSongs.`300` -> MaxSongs.`300`.name
                        MaxSongs.`500` -> MaxSongs.`500`.name
                        MaxSongs.`1000` -> MaxSongs.`1000`.name
                        MaxSongs.`2000` -> MaxSongs.`2000`.name
                        MaxSongs.`3000` -> MaxSongs.`3000`.name
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.persistent_queue).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.persistent_queue),
                text = stringResource(R.string.save_and_restore_playing_songs),
                isChecked = persistentQueue,
                onCheckedChange = {
                    persistentQueue = it
                }
            )


        if (filter.isNullOrBlank() || stringResource(R.string.resume_playback).contains(filterCharSequence,true))
            if (isAtLeastAndroid6) {
                SwitchSettingEntry(
                    title = stringResource(R.string.resume_playback),
                    text = stringResource(R.string.when_device_is_connected),
                    isChecked = resumePlaybackWhenDeviceConnected,
                    onCheckedChange = {
                        resumePlaybackWhenDeviceConnected = it
                    }
                )
            }

        if (filter.isNullOrBlank() || stringResource(R.string.close_app_with_back_button).contains(filterCharSequence,true)) {
            SwitchSettingEntry(
                isEnabled = Build.VERSION.SDK_INT >= 33,
                title = stringResource(R.string.close_app_with_back_button),
                text = stringResource(R.string.when_you_use_the_back_button_from_the_home_page),
                isChecked = closeWithBackButton,
                onCheckedChange = {
                    closeWithBackButton = it
                }
            )
            SettingsDescription(text = stringResource(R.string.restarting_rimusic_is_required))
        }

        if (filter.isNullOrBlank() || stringResource(R.string.close_background_player).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.close_background_player),
                text = stringResource(R.string.when_app_swipe_out_from_task_manager),
                isChecked = closebackgroundPlayer,
                onCheckedChange = {
                    closebackgroundPlayer = it
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.skip_silence).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.skip_silence),
                text = stringResource(R.string.skip_silent_parts_during_playback),
                isChecked = skipSilence,
                onCheckedChange = {
                    skipSilence = it
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.loudness_normalization).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.loudness_normalization),
                text = stringResource(R.string.autoadjust_the_volume),
                isChecked = volumeNormalization,
                onCheckedChange = {
                    volumeNormalization = it
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.event_shake).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.event_shake),
                text = stringResource(R.string.shake_to_change_song),
                isChecked = shakeEventEnabled,
                onCheckedChange = {
                    shakeEventEnabled = it
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.equalizer).contains(filterCharSequence,true))
            SettingsEntry(
                title = stringResource(R.string.equalizer),
                text = stringResource(R.string.interact_with_the_system_equalizer),
                onClick = {
                    val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                        putExtra(AudioEffect.EXTRA_AUDIO_SESSION, binder?.player?.audioSessionId)
                        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                    }

                    try {
                        activityResultLauncher.launch(intent)
                    } catch (e: ActivityNotFoundException) {
                        SmartToast(context.resources.getString(R.string.info_not_find_application_audio), type = PopupType.Warning)
                    }
                }
            )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.user_interface))

        if (filter.isNullOrBlank() || stringResource(R.string.interface_in_use).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.interface_in_use),
                selectedValue = uiType,
                onValueSelected = {
                    uiType = it
                    if (uiType == UiType.ViMusic) {
                        disablePlayerHorizontalSwipe = true
                        disableIconButtonOnTop = true
                        playerTimelineType = PlayerTimelineType.Default
                        playerVisualizerType = PlayerVisualizerType.Disabled
                        playerThumbnailSize = PlayerThumbnailSize.Medium
                        thumbnailTapEnabled = true
                        showSearchTab = true
                        showStatsInNavbar = true
                        navigationBarPosition = NavigationBarPosition.Left
                    } else {
                        disablePlayerHorizontalSwipe = false
                        disableIconButtonOnTop = false
                        playerTimelineType = lastPlayerTimelineType
                        playerVisualizerType = lastPlayerVisualizerType
                        playerThumbnailSize = lastPlayerThumbnailSize
                        playerPlayButtonType = lastPlayerPlayButtonType
                    }

                },
                valueText = {
                    when(it) {
                        UiType.RiMusic -> UiType.RiMusic.name
                        UiType.ViMusic -> UiType.ViMusic.name
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.theme).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.theme),
                selectedValue = colorPaletteName,
                onValueSelected = {
                    colorPaletteName = it
                   when (it) {
                       ColorPaletteName.PureBlack,
                       ColorPaletteName.ModernBlack -> colorPaletteMode = ColorPaletteMode.System
                       else -> {}
                   }
                },
                valueText = {
                    when (it) {
                        ColorPaletteName.Default -> stringResource(R.string._default)
                        ColorPaletteName.Dynamic -> stringResource(R.string.dynamic)
                        ColorPaletteName.PureBlack -> stringResource(R.string.theme_pure_black)
                        ColorPaletteName.ModernBlack -> stringResource(R.string.theme_modern_black)
                        ColorPaletteName.MaterialYou -> stringResource(R.string.theme_material_you)
                        ColorPaletteName.Customized -> stringResource(R.string.theme_customized)
                    }
                }
            )

        AnimatedVisibility(visible = colorPaletteName == ColorPaletteName.Customized) {
            Column {
                SettingsEntryGroupText(stringResource(R.string.title_customized_light_theme_colors))
                ButtonBarSettingEntry(
                    title = stringResource(R.string.title_reset_customized_light_colors),
                    text = stringResource(R.string.info_click_to_reset_default_light_colors),
                    icon = R.drawable.trash,
                    onClick = { resetCustomLightThemeDialog = true }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_1),
                    text = "",
                    color = Color(customThemeLight_Background0),
                    onColorSelected = {
                        customThemeLight_Background0 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_2),
                    text = "",
                    color = Color(customThemeLight_Background1),
                    onColorSelected = {
                        customThemeLight_Background1 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_3),
                    text = "",
                    color = Color(customThemeLight_Background2),
                    onColorSelected = {
                        customThemeLight_Background2 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_4),
                    text = "",
                    color = Color(customThemeLight_Background3),
                    onColorSelected = {
                        customThemeLight_Background3 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_5),
                    text = "",
                    color = Color(customThemeLight_Background4),
                    onColorSelected = {
                        customThemeLight_Background4 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text),
                    text = "",
                    color = Color(customThemeLight_Text),
                    onColorSelected = {
                        customThemeLight_Text= it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text_secondary),
                    text = "",
                    color = Color(customThemeLight_TextSecondary),
                    onColorSelected = {
                        customThemeLight_TextSecondary = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text_disabled),
                    text = "",
                    color = Color(customThemeLight_TextDisabled),
                    onColorSelected = {
                        customThemeLight_TextDisabled = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_icon_button_player),
                    text = "",
                    color = Color(customThemeLight_IconButtonPlayer),
                    onColorSelected = {
                        customThemeLight_IconButtonPlayer = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_accent),
                    text = "",
                    color = Color(customThemeLight_Accent),
                    onColorSelected = {
                        customThemeLight_Accent = it.hashCode()
                    }
                )

                SettingsEntryGroupText(stringResource(R.string.title_customized_dark_theme_colors))
                ButtonBarSettingEntry(
                    title = stringResource(R.string.title_reset_customized_dark_colors),
                    text = stringResource(R.string.click_to_reset_default_dark_colors),
                    icon = R.drawable.trash,
                    onClick = { resetCustomDarkThemeDialog = true }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_1),
                    text = "",
                    color = Color(customThemeDark_Background0),
                    onColorSelected = {
                        customThemeDark_Background0 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_2),
                    text = "",
                    color = Color(customThemeDark_Background1),
                    onColorSelected = {
                        customThemeDark_Background1 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_3),
                    text = "",
                    color = Color(customThemeDark_Background2),
                    onColorSelected = {
                        customThemeDark_Background2 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_4),
                    text = "",
                    color = Color(customThemeDark_Background3),
                    onColorSelected = {
                        customThemeDark_Background3 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_background_5),
                    text = "",
                    color = Color(customThemeDark_Background4),
                    onColorSelected = {
                        customThemeDark_Background4 = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text),
                    text = "",
                    color = Color(customThemeDark_Text),
                    onColorSelected = {
                        customThemeDark_Text= it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text_secondary),
                    text = "",
                    color = Color(customThemeDark_TextSecondary),
                    onColorSelected = {
                        customThemeDark_TextSecondary = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_text_disabled),
                    text = "",
                    color = Color(customThemeDark_TextDisabled),
                    onColorSelected = {
                        customThemeDark_TextDisabled = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_icon_button_player),
                    text = "",
                    color = Color(customThemeDark_IconButtonPlayer),
                    onColorSelected = {
                        customThemeDark_IconButtonPlayer = it.hashCode()
                    }
                )
                ColorSettingEntry(
                    title = stringResource(R.string.color_accent),
                    text = "",
                    color = Color(customThemeDark_Accent),
                    onColorSelected = {
                        customThemeDark_Accent = it.hashCode()
                    }
                )
            }
        }

        if (filter.isNullOrBlank() || stringResource(R.string.theme_mode).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.theme_mode),
                selectedValue = colorPaletteMode,
                isEnabled = when (colorPaletteName) {
                    ColorPaletteName.PureBlack -> false
                    ColorPaletteName.ModernBlack -> false
                    else -> { true }
                },
                onValueSelected = {
                    colorPaletteMode = it
                    //if (it == ColorPaletteMode.PitchBlack) colorPaletteName = ColorPaletteName.ModernBlack
                },
                valueText = {
                    when (it) {
                        ColorPaletteMode.Dark -> stringResource(R.string.dark)
                        ColorPaletteMode.Light -> stringResource(R.string._light)
                        ColorPaletteMode.System -> stringResource(R.string.system)
                        ColorPaletteMode.PitchBlack -> stringResource(R.string.theme_mode_pitch_black)
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.navigation_bar_position).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.navigation_bar_position),
                selectedValue = navigationBarPosition,
                onValueSelected = { navigationBarPosition = it },
                valueText = {
                    when (it) {
                        NavigationBarPosition.Left -> stringResource(R.string.direction_left)
                        NavigationBarPosition.Right -> stringResource(R.string.direction_right)
                        NavigationBarPosition.Top -> stringResource(R.string.direction_top)
                        NavigationBarPosition.Bottom -> stringResource(R.string.direction_bottom)
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.navigation_bar_type).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.navigation_bar_type),
                selectedValue = navigationBarType,
                onValueSelected = { navigationBarType = it },
                valueText = {
                    when (it) {
                        NavigationBarType.IconAndText -> stringResource(R.string.icon_and_text)
                        NavigationBarType.IconOnly -> stringResource(R.string.only_icon)
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.player_position).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.player_position),
                selectedValue = playerPosition,
                onValueSelected = { playerPosition = it },
                valueText = {
                    when (it) {
                        PlayerPosition.Top -> stringResource(R.string.position_top)
                        PlayerPosition.Bottom -> stringResource(R.string.position_bottom)
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.menu_style).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.menu_style),
                selectedValue = menuStyle,
                onValueSelected = { menuStyle = it },
                valueText = {
                    when (it) {
                        MenuStyle.Grid -> stringResource(R.string.style_grid)
                        MenuStyle.List -> stringResource(R.string.style_list)
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.default_page).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.default_page),
                selectedValue = indexNavigationTab,
                onValueSelected = {indexNavigationTab = it},
                valueText = {
                    when (it) {
                        HomeScreenTabs.Default -> stringResource(R.string._default)
                        HomeScreenTabs.QuickPics -> stringResource(R.string.quick_picks)
                        HomeScreenTabs.Songs -> stringResource(R.string.songs)
                        HomeScreenTabs.Albums -> stringResource(R.string.albums)
                        HomeScreenTabs.Artists -> stringResource(R.string.artists)
                        HomeScreenTabs.Library -> stringResource(R.string.library)
                        //HomeScreenTabs.Discovery -> stringResource(R.string.discovery)
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.transition_effect).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.transition_effect),
                selectedValue = transitionEffect,
                onValueSelected = { transitionEffect = it },
                valueText = {
                    when (it) {
                        TransitionEffect.None -> stringResource(R.string.none)
                        TransitionEffect.Expand -> stringResource(R.string.te_expand)
                        TransitionEffect.Fade -> stringResource(R.string.te_fade)
                        TransitionEffect.Scale -> stringResource(R.string.te_scale)
                        TransitionEffect.SlideVertical -> stringResource(R.string.te_slide_vertical)
                        TransitionEffect.SlideHorizontal -> stringResource(R.string.te_slide_horizontal)
                    }
                }
            )

        if (uiType == UiType.ViMusic) {
            if (filter.isNullOrBlank() || stringResource(R.string.vimusic_show_search_button_in_navigation_bar).contains(
                    filterCharSequence,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.vimusic_show_search_button_in_navigation_bar),
                    text = stringResource(R.string.vismusic_only_in_left_right_navigation_bar),
                    isChecked = showSearchTab,
                    onCheckedChange = { showSearchTab = it }
                )



            if (filter.isNullOrBlank() || stringResource(R.string.show_statistics_in_navigation_bar).contains(
                    filterCharSequence,
                    true
                )
            )
                SwitchSettingEntry(
                    title = stringResource(R.string.show_statistics_in_navigation_bar),
                    text = "",
                    isChecked = showStatsInNavbar,
                    onCheckedChange = { showStatsInNavbar = it }
                )
        }

        if (filter.isNullOrBlank() || stringResource(R.string.show_floating_icon).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.show_floating_icon),
                text = "",
                isChecked = showFloatingIcon,
                onCheckedChange = { showFloatingIcon = it }
            )



        if (filter.isNullOrBlank() || stringResource(R.string.settings_use_font_type).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.settings_use_font_type),
                selectedValue = fontType,
                onValueSelected = { fontType = it },
                valueText = {
                    when (it) {
                        FontType.Rubik -> FontType.Rubik.name
                        FontType.Poppins -> FontType.Poppins.name
                    }
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.use_system_font).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.use_system_font),
                text = stringResource(R.string.use_font_by_the_system),
                isChecked = useSystemFont,
                onCheckedChange = { useSystemFont = it }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.apply_font_padding).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.apply_font_padding),
                text = stringResource(R.string.add_spacing_around_texts),
                isChecked = applyFontPadding,
                onCheckedChange = { applyFontPadding = it }
            )

        /*
        if (filter.isNullOrBlank() || stringResource(R.string.swipe_to_action).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.swipe_to_action),
                text = stringResource(R.string.activate_the_action_menu_by_swiping_the_song_left_or_right),
                isChecked = isSwipeToActionEnabled,
                onCheckedChange = { isSwipeToActionEnabled = it }
            )
         */

        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.songs).uppercase())

        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.favorites)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.favorites)}",
                text = "",
                isChecked = showFavoritesPlaylist,
                onCheckedChange = { showFavoritesPlaylist = it }
            )
        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.cached)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.cached)}",
                text = "",
                isChecked = showCachedPlaylist,
                onCheckedChange = { showCachedPlaylist = it }
            )
        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.downloaded)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.downloaded)}",
                text = "",
                isChecked = showDownloadedPlaylist,
                onCheckedChange = { showDownloadedPlaylist = it }
            )
        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.my_playlist_top)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.my_playlist_top).format(maxTopPlaylistItems)}",
                text = "",
                isChecked = showMyTopPlaylist,
                onCheckedChange = { showMyTopPlaylist = it }
            )
        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.on_device)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.on_device)}",
                text = "",
                isChecked = showOnDevicePlaylist,
                onCheckedChange = { showOnDevicePlaylist = it }
            )

        /*
        SettingsGroupSpacer()
        SettingsEntryGroupText(title = stringResource(R.string.playlists).uppercase())

        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.playlists)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.playlists)}",
                text = "",
                isChecked = showPlaylists,
                onCheckedChange = { showPlaylists = it }
            )
        if (filter.isNullOrBlank() || "${stringResource(R.string.show)} ${stringResource(R.string.monthly_playlists)}".contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = "${stringResource(R.string.show)} ${stringResource(R.string.monthly_playlists)}",
                text = "",
                isChecked = showMonthlyPlaylistInLibrary,
                onCheckedChange = { showMonthlyPlaylistInLibrary = it }
            )
         */

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.monthly_playlists).uppercase())

        if (filter.isNullOrBlank() || stringResource(R.string.monthly_playlists).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.enable_monthly_playlists_creation),
                text = "",
                isChecked = enableCreateMonthlyPlaylists,
                onCheckedChange = {
                    enableCreateMonthlyPlaylists = it
                }
            )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.smart_recommendations))

        if (filter.isNullOrBlank() || stringResource(R.string.statistics_max_number_of_items).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.statistics_max_number_of_items),
                selectedValue = recommendationsNumber,
                onValueSelected = { recommendationsNumber = it },
                valueText = {
                    it.number.toString()
                }
            )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.statistics))

        if (filter.isNullOrBlank() || stringResource(R.string.statistics_max_number_of_items).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.statistics_max_number_of_items),
                selectedValue = maxStatisticsItems,
                onValueSelected = { maxStatisticsItems = it },
                valueText = {
                    it.number.toString()
                }
            )

        if (filter.isNullOrBlank() || stringResource(R.string.listening_time).contains(filterCharSequence,true))
            SwitchSettingEntry(
                title = stringResource(R.string.listening_time),
                text = stringResource(R.string.shows_the_number_of_songs_heard_and_their_listening_time),
                isChecked = showStatsListeningTime,
                onCheckedChange = {
                    showStatsListeningTime = it
                }
            )

        SettingsGroupSpacer()
        SettingsEntryGroupText(stringResource(R.string.playlist_top))

        if (filter.isNullOrBlank() || stringResource(R.string.statistics_max_number_of_items).contains(filterCharSequence,true))
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.statistics_max_number_of_items),
                selectedValue = maxTopPlaylistItems,
                onValueSelected = { maxTopPlaylistItems = it },
                valueText = {
                    it.number.toString()
                }
            )

        SettingsGroupSpacer(
            modifier = Modifier.height(Dimensions.bottomSpacer)
        )

    }
}
