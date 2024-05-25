package it.fast4x.rimusic.utils

import androidx.compose.animation.core.DecayAnimation
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.FloatDecayAnimationSpec
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.fast4x.rimusic.ui.items.DragAnchors
import it.fast4x.rimusic.ui.items.DraggableItem


@UnstableApi
@ExperimentalFoundationApi
@Composable
fun BehindMotionSwipe(
    content: @Composable () -> Unit,
    leftActionsContent: @Composable () -> Unit,
    rightActionsContent: @Composable () -> Unit,
    onHorizontalSwipeWhenActionDisabled: () -> Unit
) {

    /*
    val density = LocalDensity.current
    //val (colorPalette) = LocalAppearance.current

    val defaultActionSize = 80.dp

    val endActionSizePx = with(density) { (defaultActionSize * 2).toPx() }
    val startActionSizePx = with(density) { defaultActionSize.toPx() }

    val state =
        AnchoredDraggableState(
            initialValue = DragAnchors.Center,
            anchors = DraggableAnchors {
                DragAnchors.Start at -startActionSizePx
                DragAnchors.Center at 0f
                DragAnchors.End at endActionSizePx
            },
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            decayAnimationSpec =  DecayAnimation(
                animationSpec = ,
                initialVelocity = 0.0f
            ),
            snapAnimationSpec =  spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            //animationSpec = tween(),
            confirmValueChange = { true }
        )


    val isSwipeToActionEnabled by rememberPreference(isSwipeToActionEnabledKey, true)

    DraggableItem(
        draggableActive = isSwipeToActionEnabled,
        onHorizontalSwipeWhenActionDisabled = onHorizontalSwipeWhenActionDisabled,
        state = state,
        content = {
            content()
        },

        startAction = {
            leftActionsContent()
            /*
            LeftAction(
                icon = R.drawable.enqueue,
                backgroundColor = colorPalette.background4,
                onClick = {}
            )
             */
        },
        endAction = {
            rightActionsContent()
            /*
            RightActions(
                iconAction1 = R.drawable.pencil,
                backgroundColorAction1 = colorPalette.background4,
                onClickAction1 = {},
                iconAction2 = R.drawable.trash,
                backgroundColorAction2 = colorPalette.red,
                onClickAction2 = {}
            )
             */
        }
    )

     */
}

