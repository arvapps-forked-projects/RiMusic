package it.fast4x.rimusic.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.TransitionEffect
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.ui.components.NavigationRail
import it.fast4x.rimusic.ui.components.ScaffoldTB
import it.fast4x.rimusic.ui.components.themed.appBar
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.utils.UiTypeKey
import it.fast4x.rimusic.utils.bold
import it.fast4x.rimusic.utils.getCurrentRoute
import it.fast4x.rimusic.utils.menuItemColors
import it.fast4x.rimusic.utils.navigationBarPositionKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.transitionEffectKey

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalAnimationApi
@Composable
fun Scaffold(
    navController: NavController,
    topIconButtonId: Int,
    onTopIconButtonClick: () -> Unit,
    showButton1: Boolean = false,
    topIconButton2Id: Int,
    onTopIconButton2Click: () -> Unit,
    showButton2: Boolean,
    bottomIconButtonId: Int? = R.drawable.search,
    onBottomIconButtonClick: (() -> Unit)? = {},
    showBottomButton: Boolean? = false,
    hideTabs: Boolean? = false,
    tabIndex: Int,
    onTabChanged: (Int) -> Unit,
    showTopActions: Boolean? = false,
    tabColumnContent: @Composable ColumnScope.(@Composable (Int, String, Int) -> Unit) -> Unit,
    onHomeClick: () -> Unit,
    onSettingsClick: (() -> Unit)? = {},
    onStatisticsClick: (() -> Unit)? = {},
    onHistoryClick: (() -> Unit)? = {},
    onSearchClick: (() -> Unit)? = {},
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.(Int) -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current
    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Left)
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
    val transitionEffect by rememberPreference(transitionEffectKey, TransitionEffect.Scale)

    if (navigationBarPosition == NavigationBarPosition.Top || navigationBarPosition == NavigationBarPosition.Bottom) {
            ScaffoldTB(
                navController = navController,
                topIconButtonId = topIconButtonId,
                onTopIconButtonClick = onTopIconButtonClick,
                showButton1 = showButton1,
                topIconButton2Id = topIconButton2Id,
                onTopIconButton2Click = onTopIconButton2Click,
                showButton2 = showButton2,
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                tabColumnContent = tabColumnContent,
                showBottomButton = showBottomButton,
                bottomIconButtonId = bottomIconButtonId,
                onBottomIconButtonClick = onBottomIconButtonClick ?: {},
                showTopActions = showTopActions,
                content = content,
                hideTabs = hideTabs,
                onHomeClick = onHomeClick,
                onStatisticsClick = onStatisticsClick,
                onSettingsClick = onSettingsClick,
                onHistoryClick = onHistoryClick,
                onSearchClick = onSearchClick
            )
    } else {
        //val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val customModifier = if(uiType == UiType.RiMusic)
            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        else Modifier

        //var expanded by remember { mutableStateOf(false) }

        androidx.compose.material3.Scaffold(
            modifier = customModifier,
            containerColor = colorPalette.background0,
            topBar = {
                if(uiType == UiType.RiMusic) {
                    appBar(navController)
                }
            },

            bottomBar = {

            }

        ) {
            //it.calculateTopPadding()
            //**

            Row(
                //horizontalArrangement = Arrangement.spacedBy(0.dp),
                modifier = modifier
                    //.border(BorderStroke(1.dp, Color.Red))
                    //.padding(top = 50.dp)
                    .padding(it)
                    .background(colorPalette.background0)
                    .fillMaxSize()
            ) {
                val navigationRail: @Composable () -> Unit = {
                    NavigationRail(
                        topIconButtonId = topIconButtonId,
                        onTopIconButtonClick = onTopIconButtonClick,
                        showButton1 = showButton1,
                        topIconButton2Id = topIconButton2Id,
                        onTopIconButton2Click = onTopIconButton2Click,
                        showButton2 = showButton2,
                        bottomIconButtonId = bottomIconButtonId,
                        onBottomIconButtonClick = onBottomIconButtonClick ?: {},
                        showBottomButton = showBottomButton,
                        tabIndex = tabIndex,
                        onTabIndexChanged = onTabChanged,
                        content = tabColumnContent,
                        hideTabs = hideTabs
                    )
                }

                if (navigationBarPosition == NavigationBarPosition.Left)
                    navigationRail()

                val topPadding = if (uiType == UiType.ViMusic) 30.dp else 0.dp

                AnimatedContent(
                    targetState = tabIndex,
                    transitionSpec = {
                        when (transitionEffect) {
                            TransitionEffect.None-> EnterTransition.None togetherWith ExitTransition.None
                            TransitionEffect.Expand -> expandIn(animationSpec = tween(350, easing = LinearOutSlowInEasing), expandFrom = Alignment.BottomStart).togetherWith(
                                shrinkOut(animationSpec = tween(350, easing = FastOutSlowInEasing),shrinkTowards = Alignment.CenterStart)
                            )
                            TransitionEffect.Fade -> fadeIn(animationSpec = tween(350)).togetherWith(fadeOut(animationSpec = tween(350)))
                            TransitionEffect.Scale -> scaleIn(animationSpec = tween(350)).togetherWith(scaleOut(animationSpec = tween(350)))
                            TransitionEffect.SlideHorizontal, TransitionEffect.SlideVertical -> {
                                val slideDirection = when (targetState > initialState) {
                                    true -> {
                                        if (transitionEffect == TransitionEffect.SlideHorizontal)
                                            AnimatedContentTransitionScope.SlideDirection.Left
                                        else AnimatedContentTransitionScope.SlideDirection.Up
                                    }

                                    false -> {
                                        if (transitionEffect == TransitionEffect.SlideHorizontal)
                                            AnimatedContentTransitionScope.SlideDirection.Right
                                        else AnimatedContentTransitionScope.SlideDirection.Down
                                    }
                                }

                                val animationSpec = spring(
                                    dampingRatio = 0.9f,
                                    stiffness = Spring.StiffnessLow,
                                    visibilityThreshold = IntOffset.VisibilityThreshold
                                )

                                slideIntoContainer(slideDirection, animationSpec) togetherWith
                                        slideOutOfContainer(slideDirection, animationSpec)
                            }
                        }
                    },
                    content = content, label = "",
                    modifier = Modifier
                        //.fillMaxWidth()
                        .fillMaxHeight()
                        .padding(top = topPadding)
                )

                if (navigationBarPosition == NavigationBarPosition.Right)
                    navigationRail()

            }
            //**
        }
    }

}
