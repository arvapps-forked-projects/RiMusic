package it.fast4x.rimusic.utils

/*
fun Density.calculateDistanceToDesiredSnapPosition(
    layoutInfo: LazyGridLayoutInfo,
    item: LazyGridItemInfo,
    positionInLayout: Density.(layoutSize: Float, itemSize: Float) -> Float
): Float {
    val containerSize =
        with(layoutInfo) { singleAxisViewportSize - beforeContentPadding - afterContentPadding }

    val desiredDistance = positionInLayout(containerSize.toFloat(), item.size.width.toFloat())
    val itemCurrentPosition = item.offset.x.toFloat()

    return itemCurrentPosition - desiredDistance
}

private val LazyGridLayoutInfo.singleAxisViewportSize: Int
    get() = if (orientation == Orientation.Vertical) viewportSize.height else viewportSize.width

@ExperimentalFoundationApi
fun SnapLayoutInfoProvider(
    lazyGridState: LazyGridState,
    positionInLayout: Density.(layoutSize: Float, itemSize: Float) -> Float =
        { layoutSize, itemSize -> (layoutSize / 2f - itemSize / 2f) }
): SnapLayoutInfoProvider = object : SnapLayoutInfoProvider {
    private val layoutInfo: LazyGridLayoutInfo
        get() = lazyGridState.layoutInfo

    // Single page snapping is the default
    override fun Density.calculateApproachOffset(initialVelocity: Float) = 0f
    override fun Density.calculateSnapStepSize(): Float = with(layoutInfo) {
        if (visibleItemsInfo.isNotEmpty())
            visibleItemsInfo.fastSumBy { it.size.width } / visibleItemsInfo.size.toFloat()
        else 0f
    }

    // ignoring the velocity for now since there is no animation spec in this provider
    override fun Density.calculateSnappingOffset(currentVelocity: Float): Float {
        var lowerBoundOffset = Float.NEGATIVE_INFINITY
        var upperBoundOffset = Float.POSITIVE_INFINITY

        layoutInfo.visibleItemsInfo.fastForEach { item ->
            val offset = calculateDistanceToDesiredSnapPosition(layoutInfo, item, positionInLayout)
            // Find item that is closest to the center
            if (offset <= 0 && offset > lowerBoundOffset) lowerBoundOffset = offset
            // Find item that is closest to center, but after it
            if (offset >= 0 && offset < upperBoundOffset) upperBoundOffset = offset
        }

        return if ((lowerBoundOffset * -1f) > upperBoundOffset) upperBoundOffset else lowerBoundOffset
    }
}
 */