package it.fast4x.rimusic.extensions.visualizer

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.enums.PlayerVisualizerType
import it.fast4x.rimusic.extensions.visualizer.audio.VisualizerComputer
import it.fast4x.rimusic.extensions.visualizer.audio.VisualizerData
import it.fast4x.rimusic.extensions.visualizer.ui.CircularStackedBarVisualizer
import it.fast4x.rimusic.extensions.visualizer.ui.DoubleSidedCircularPathVisualizer
import it.fast4x.rimusic.extensions.visualizer.ui.DoubleSidedPathVisualizer
import it.fast4x.rimusic.extensions.visualizer.ui.FancyTubularStackedBarVisualizer
import it.fast4x.rimusic.extensions.visualizer.ui.FullBarVisualizer
import it.fast4x.rimusic.extensions.visualizer.ui.OneSidedPathVisualizer
import it.fast4x.rimusic.extensions.visualizer.ui.StackedBarVisualizer
import it.fast4x.rimusic.extensions.visualizer.ui.ext.repeat
import it.fast4x.rimusic.ui.styling.LocalAppearance

@UnstableApi
@Composable
fun Visualizer(
    showInPage: Boolean? = true,
    playerVisualizerType: PlayerVisualizerType = PlayerVisualizerType.Disabled
) {
        val visualizerData = remember { mutableStateOf(VisualizerData()) }

        if (showInPage == true)
            Content(
                //isPlaying,
                //setPlaying,
                visualizerData
             )
        else
                ContentType(
                playerVisualizerType,
                visualizerData
            )

}

@UnstableApi
@Composable
fun Content(
    visualizerData: MutableState<VisualizerData>
) {

    val binder = LocalPlayerServiceBinder.current

    //VisualizerComputer.setupPermissions( LocalContext.current as Activity )
    val audioComputer = VisualizerComputer()

    binder?.player?.audioSessionId?.let {
        audioComputer.start(audioSessionId = it, onData = { data ->
        visualizerData.value = data
    })
    }

    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
    ) {

        val someColors =
            listOf(Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Red, Color.Cyan)
        val displayAllItems = false
        val selectItemIndex = 1

        if (displayAllItems || (selectItemIndex == 0))
            item {
                FancyTubularStackedBarVisualizer(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(all = 2.dp),
                    data = visualizerData.value,
                    barCount = 48,
                    maxStackCount = 16,
                )
            }

        if (displayAllItems || (selectItemIndex == 1))
            item {
                CircularStackedBarVisualizer(
                    Modifier
                        .fillMaxWidth()
                        //.height(300.dp)
                        .aspectRatio(1f),
                       // .background(Color(0xff111111)),
                    data = visualizerData.value,
                    barCount = 48,
                    maxStackCount = 16
                )
            }

        if (displayAllItems || (selectItemIndex == 2))
            item {
                StackedBarVisualizer(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(vertical = 4.dp)
                        .background(Color(0x50000000)),
                    data = visualizerData.value,
                    barCount = 64
                )
            }

        if (displayAllItems || (selectItemIndex == 3))
            item {
                FullBarVisualizer(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = 4.dp)
                        .background(Color(0x50000000)),
                    barModifier = { i, m -> m.background(someColors[i % someColors.size]) },
                    data = visualizerData.value,
                    barCount = 64
                )
            }

        if (displayAllItems || (selectItemIndex == 4))
            item {
                OneSidedPathVisualizer(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = 4.dp)
                        .background(Color(0x60000000)),
                    data = visualizerData.value,
                    segmentCount = 32,
                    fillBrush = Brush.linearGradient(
                        start = Offset.Zero,
                        end = Offset.Infinite,
                        colors = listOf(
                            Color.Red,
                            Color.Yellow,
                            Color.Green,
                            Color.Cyan,
                            Color.Blue,
                            Color.Magenta,
                        ).repeat(3)
                    )
                )
            }

        if (displayAllItems || (selectItemIndex == 5))
            item {
                DoubleSidedPathVisualizer(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = 4.dp)
                        .background(Color(0x70000000)),
                    data = visualizerData.value,
                    segmentCount = 128,
                    fillBrush = Brush.linearGradient(
                        start = Offset.Zero,
                        end = Offset(0f, Float.POSITIVE_INFINITY),
                        colors = listOf(Color.White, Color.Red, Color.White)
                    )
                )
            }

        if (displayAllItems || (selectItemIndex == 6))
            item {
                DoubleSidedCircularPathVisualizer(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(vertical = 4.dp)
                        .background(Color(0xE0000000)),
                    data = visualizerData.value,
                    segmentCount = 128,
                    fillBrush = Brush.radialGradient(
                        listOf(
                            Color.Red,
                            Color.Red,
                            Color.Yellow,
                            Color.Green
                        )
                    )
                )
            }
    }
}

@SuppressLint("SuspiciousIndentation")
@UnstableApi
@Composable
fun ContentType(
    visualizerType: PlayerVisualizerType,
    visualizerData: MutableState<VisualizerData>
) {

    val (colorPalette,) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val context = LocalContext.current

    //VisualizerComputer.setupPermissions( LocalContext.current as Activity )
    val audioComputer = VisualizerComputer()
    //Log.d("mediaItemEqualizer","EXTERNAL audioSession ${binder?.player?.audioSessionId}")
    binder?.player?.audioSessionId?.let {
        //Log.d("mediaItemEqualizer","internal audioSession ${it}")
        audioComputer.start(audioSessionId = it, onData = { data ->
            //Log.d("mediaItemEqualizer","onData amplitude ${data.amplitude} captureSize ${data.captureSize} rawWaveform ${data.rawWaveform} samplingRate ${data.samplingRate}")
            visualizerData.value = data
        })
    }

        val someColors =
            //listOf(Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Red, Color.Cyan)
            listOf(colorPalette.text, colorPalette.textDisabled, colorPalette.textSecondary)


        if (visualizerType == PlayerVisualizerType.Fancy)
                FancyTubularStackedBarVisualizer(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        //.padding(all = 2.dp)
                        //.background(colorPalette.overlay),
                        .background(Color.Black.copy(0.8f)),
                    data = visualizerData.value,
                    barCount = 48,
                    maxStackCount = 16,
                )


    if (visualizerType == PlayerVisualizerType.Circular)
                CircularStackedBarVisualizer(
                    Modifier
                        .fillMaxWidth()
                        //.height(300.dp)
                        .aspectRatio(1f)
                        //.background(colorPalette.overlay),
                        .background(Color.Black.copy(0.8f)),
                    data = visualizerData.value,
                    barCount = 32,
                    maxStackCount = 16
                )


    if (visualizerType == PlayerVisualizerType.Stacked)
                StackedBarVisualizer(
                    Modifier
                        .fillMaxWidth()
                        //.height(300.dp)
                        .aspectRatio(1f)
                        //.padding(vertical = 4.dp)
                        //.background(colorPalette.overlay),
                        .background(Color.Black.copy(0.8f)),
                    data = visualizerData.value,
                    barCount = 16
                )


    if (visualizerType == PlayerVisualizerType.Full)
                FullBarVisualizer(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        //.aspectRatio(1f)
                        //.padding(vertical = 4.dp)
                        //.background(colorPalette.overlay),
                        .background(Color.Black.copy(0.8f)),
                    barModifier = { i, m -> m.background(someColors[i % someColors.size]) },
                    data = visualizerData.value,
                    barCount = 16
                )


    if (visualizerType == PlayerVisualizerType.Oneside)
                OneSidedPathVisualizer(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .aspectRatio(1f)
                        //.padding(vertical = 4.dp)
                        //.background(colorPalette.overlay),
                        .background(Color.Black.copy(0.8f)),
                    data = visualizerData.value,
                    segmentCount = 32,
                    fillBrush = Brush.linearGradient(
                        start = Offset.Zero,
                        end = Offset.Infinite,
                        colors = someColors.repeat(3)
                        /*
                        colors = listOf(
                            Color.Red,
                            Color.Yellow,
                            Color.Green,
                            Color.Cyan,
                            Color.Blue,
                            Color.Magenta,
                        ).repeat(3)

                         */
                    )
                )


    if (visualizerType == PlayerVisualizerType.Doubleside)
                DoubleSidedPathVisualizer(
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .aspectRatio(1f)
                        //.padding(vertical = 4.dp)
                        //.background(colorPalette.overlay),
                        .background(Color.Black.copy(0.8f)),
                    data = visualizerData.value,
                    segmentCount = 64,
                    fillBrush = Brush.linearGradient(
                        start = Offset.Zero,
                        end = Offset(0f, Float.POSITIVE_INFINITY),
                        colors =  someColors //listOf(Color.White, Color.Red, Color.White)
                    )
                )


    if (visualizerType == PlayerVisualizerType.DoublesideCircular)
                DoubleSidedCircularPathVisualizer(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        //.padding(vertical = 4.dp)
                        //.background(colorPalette.overlay),
                        .background(Color.Black.copy(0.8f)),
                    data = visualizerData.value,
                    segmentCount = 64,
                    fillBrush = Brush.radialGradient(
                        someColors
                        /*
                        listOf(
                            Color.Red,
                            Color.Red,
                            Color.Yellow,
                            Color.Green
                        )

                         */
                    )
                )

    }
