package it.fast4x.rimusic.ui.components.themed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.ColorPaletteMode
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.extensions.games.pacman.Pacman
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.utils.UiTypeKey
import it.fast4x.rimusic.utils.colorPaletteModeKey
import it.fast4x.rimusic.utils.getCurrentRoute
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.menuItemColors
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun appBar(
    navController: NavController
) {
    val (colorPalette, typography) = LocalAppearance.current
    var colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.System)
    val uiType  by rememberPreference(UiTypeKey, UiType.RiMusic)
    var expanded by remember { mutableStateOf(false) }
    var countForReveal by remember { mutableStateOf(0) }
    var showGames by remember { mutableStateOf(false) }
    //val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val customModifier = if(uiType == UiType.RiMusic)
        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    else Modifier

    if (showGames) {
        Pacman()
    }

    val appBar =
    TopAppBar(
        navigationIcon = {
            //val currentRoute = navController.currentBackStackEntry?.destination?.route
            //println("navController current destination and route ${navController.currentDestination} $currentRoute")
            if (getCurrentRoute(navController) != NavRoutes.home.name)
                androidx.compose.material3.IconButton(
                    onClick = {
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
                            navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.chevron_back),
                        tint = colorPalette.favoritesIcon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
        },
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.app_icon),
                    colorFilter = ColorFilter.tint(colorPalette.favoritesIcon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            countForReveal++
                            if (countForReveal == 3) {
                                SmartToast("Do you like clicking? Then continue...", durationLong = true)
                            }
                            if (countForReveal == 6) {
                                SmartToast("Okay, you’re looking for something, keep...", durationLong = true)
                            }
                            if (countForReveal == 9) {
                                SmartToast("You are a number one, click and enjoy the surprise", durationLong = true)
                            }
                            if (countForReveal == 10) {
                                countForReveal = 0
                                navController.navigate(NavRoutes.games.name)
                            }


                            //if (navController.currentDestination?.route != NavRoutes.home.name)
                            //    navController.navigate(NavRoutes.home.name)
                        }
                )
                Image(
                    painter = painterResource(R.drawable.app_logo_text),
                    colorFilter = ColorFilter.tint(
                        when(colorPaletteMode) {
                            ColorPaletteMode.Light, ColorPaletteMode.System -> colorPalette.text
                            else -> Color.White
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clickable {
                            if (navController.currentDestination?.route != NavRoutes.home.name)
                                navController.navigate(NavRoutes.home.name)
                        }
                )
                /*
                BasicText(
                    text = "Music",
                    style = TextStyle(
                        fontSize = typography.xxxl.semiBold.fontSize,
                        //fontWeight = typography.xxl.semiBold.fontWeight,
                        fontFamily = typography.xxxl.semiBold.fontFamily,
                        color = colorPalette.text
                    ),
                    modifier = Modifier
                        .clickable {
                            //onHomeClick()
                            if (navController.currentDestination?.route != NavRoutes.home.name)
                                navController.navigate(NavRoutes.home.name)
                        }
                )
                 */
            }
        },
        actions = {
            //if (showTopActions == true) {
            //println("mediaItem nav ${navController.currentDestination?.route}")
            androidx.compose.material3.IconButton(
                //enabled = navController.currentDestination?.route?.startsWith(NavRoutes.search.name) == false,
                onClick = {
                    //if (onSearchClick != null) {
                    //onSearchClick()
                    navController.navigate(NavRoutes.search.name)
                    //}
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.search),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            androidx.compose.material3.IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.burger),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(
                    colorPalette.background0.copy(
                        0.85f
                    )
                )
            ) {
                DropdownMenuItem(
                    enabled = navController.currentDestination?.route != NavRoutes.history.name,
                    colors = menuItemColors(),
                    text = { Text(stringResource(R.string.history)) },
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.history),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = {
                        expanded = false
                        //if (onHistoryClick != null) {
                        //onHistoryClick()
                        navController.navigate(NavRoutes.history.name)
                        //}
                    }
                )
                DropdownMenuItem(
                    enabled = navController.currentDestination?.route != NavRoutes.statistics.name,
                    colors = menuItemColors(),
                    text = { Text(stringResource(R.string.statistics)) },
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.stats_chart),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = {
                        expanded = false
                        //if (onStatisticsClick != null) {
                        //onStatisticsClick()
                        navController.navigate(NavRoutes.statistics.name)
                        //}
                    }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    enabled = navController.currentDestination?.route != NavRoutes.settings.name,
                    colors = menuItemColors(),
                    text = { Text(stringResource(R.string.settings)) },
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.settings),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = {
                        expanded = false
                        //if (onSettingsClick != null) {
                        //onSettingsClick()
                        navController.navigate(NavRoutes.settings.name)
                        //}
                    }
                )
            }

            /*
        IconButton(onClick = { }) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.stats_chart),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        if (onSettingsClick != null) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.settings),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
         */
            //}
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarColors(
            containerColor = colorPalette.background0,
            titleContentColor = colorPalette.text,
            scrolledContainerColor = colorPalette.background0,
            navigationIconContentColor = colorPalette.background0,
            actionIconContentColor = colorPalette.text
        )
    )

    return appBar
}