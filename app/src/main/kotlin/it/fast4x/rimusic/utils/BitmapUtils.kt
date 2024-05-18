package it.fast4x.rimusic.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import it.fast4x.rimusic.ui.styling.ColorPalette
import it.fast4x.rimusic.ui.styling.DefaultDarkColorPalette
import it.fast4x.rimusic.ui.styling.DefaultLightColorPalette
import it.fast4x.rimusic.ui.styling.dynamicColorPaletteOf
import it.fast4x.rimusic.ui.styling.hsl

suspend fun getBitmapFromUrl(context: Context, url: String): Bitmap {
    val loading = ImageLoader(context)
    val request = ImageRequest.Builder(context).data(url).allowHardware(false).build()
    val result = (loading.execute(request) as SuccessResult).drawable
    return (result as BitmapDrawable).bitmap
}
