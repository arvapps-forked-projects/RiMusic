package it.fast4x.rimusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.kieronquinn.monetcompat.core.MonetCompat
import it.fast4x.rimusic.enums.CoilDiskCacheMaxSize
import it.fast4x.rimusic.utils.coilDiskCacheMaxSizeKey
import it.fast4x.rimusic.utils.getEnum
import it.fast4x.rimusic.utils.preferences

class MainApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        DatabaseInitializer()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .placeholder(R.drawable.loader)
            .respectCacheHeaders(false)
            .memoryCache(
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            )
            .diskCache(
                DiskCache.Builder()
                    .directory(filesDir.resolve("coil"))
                    .maxSizeBytes(
                        preferences.getEnum(
                            coilDiskCacheMaxSizeKey,
                            CoilDiskCacheMaxSize.`128MB`
                        ).bytes
                    )
                    .build()
            )
            .build()
    }
}
