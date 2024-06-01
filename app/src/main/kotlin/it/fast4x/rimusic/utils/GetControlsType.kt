package it.fast4x.rimusic.utils

import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import it.fast4x.rimusic.enums.PlayerControlsType
import it.fast4x.rimusic.enums.PlayerPlayButtonType
import it.fast4x.rimusic.models.ui.UiMedia
import it.fast4x.rimusic.service.PlayerService
import it.fast4x.rimusic.ui.components.themed.PlaybackParamsDialog
import it.fast4x.rimusic.ui.screens.player.components.controls.ControlsEssential
import it.fast4x.rimusic.ui.screens.player.components.controls.ControlsModern

@OptIn(UnstableApi::class)
@Composable
fun GetControls(
    binder: PlayerService.Binder,
    position: Long,
    shouldBePlaying: Boolean,
    likedAt: Long?,
    mediaId: String,
) {
    val playerControlsType by rememberPreference(playerControlsTypeKey, PlayerControlsType.Modern)
    val playerPlayButtonType by rememberPreference(
        playerPlayButtonTypeKey,
        PlayerPlayButtonType.Rectangular
    )
    var isRotated by rememberSaveable { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 360F else 0f,
        animationSpec = tween(durationMillis = 200), label = ""
    )
    val isGradientBackgroundEnabled by rememberPreference(isGradientBackgroundEnabledKey, false)

    var playbackSpeed by rememberPreference(playbackSpeedKey, 1f)
    var showSpeedPlayerDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSpeedPlayerDialog) {
        PlaybackParamsDialog(
            onDismiss = { showSpeedPlayerDialog = false },
            speedValue = { playbackSpeed = it },
            pitchValue = {}
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
    ) {

        if (playerControlsType == PlayerControlsType.Essential)
            ControlsEssential(
                binder = binder,
                position = position,
                playbackSpeed = playbackSpeed,
                shouldBePlaying = shouldBePlaying,
                likedAt = likedAt,
                mediaId = mediaId,
                playerPlayButtonType = playerPlayButtonType,
                rotationAngle = rotationAngle,
                isGradientBackgroundEnabled = isGradientBackgroundEnabled,
                onShowSpeedPlayerDialog = { showSpeedPlayerDialog = true }
            )

        if (playerControlsType == PlayerControlsType.Modern)
            ControlsModern(
                binder = binder,
                position = position,
                playbackSpeed = playbackSpeed,
                shouldBePlaying = shouldBePlaying,
                playerPlayButtonType = playerPlayButtonType,
                rotationAngle = rotationAngle,
                isGradientBackgroundEnabled = isGradientBackgroundEnabled,
                onShowSpeedPlayerDialog = { showSpeedPlayerDialog = true }
            )
    }
}