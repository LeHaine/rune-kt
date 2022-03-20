package com.lehaine.rune.engine.util

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.util.Scaler
import com.lehaine.littlekt.util.viewport.Viewport
import kotlin.math.roundToInt

/**
 * @author Colton Daily
 * @date 3/19/2022
 */
class PixelPerfectViewport(val minWidth: Int, val minHeight: Int) : Viewport(0, 0, minWidth, minHeight) {

    override fun update(width: Int, height: Int, context: Context) {
        var worldWidth = minWidth.toFloat()
        var worldHeight = minHeight.toFloat()

        val scaled = Scaler.Fit().apply(minWidth, minHeight, width, height)
        var viewportWidth = scaled.x.roundToInt()
        var viewportHeight = scaled.y.roundToInt()
        if (viewportWidth < width) {
            val toViewportSpace = viewportHeight / worldHeight
            val toWorldSpace = worldHeight / viewportHeight
            val lengthen = (width - viewportWidth) * toWorldSpace
            worldWidth += lengthen
            viewportWidth += (lengthen * toViewportSpace).roundToInt()
        } else if (viewportHeight < height) {
            val toViewportSpace = viewportWidth / worldWidth
            val toWorldSpace = worldWidth / viewportWidth
            val lengthen = (height - viewportHeight) * toWorldSpace
            worldHeight += lengthen
            viewportHeight += (lengthen * toViewportSpace).roundToInt()
        }
        while ((worldWidth.toInt().toFloat() / 2) % 2 != 0f) worldWidth++
        while ((worldHeight.toInt().toFloat() / 2) % 2 != 0f) worldHeight++

        virtualWidth = worldWidth.toInt()
        virtualHeight = worldHeight.toInt()
        set((width - viewportWidth) / 2, (height - viewportHeight) / 2, viewportWidth, viewportHeight)
        apply(context)
    }
}