package com.lehaine.rune.engine.renderable

import com.lehaine.littlekt.graphics.Batch
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.TextureSlice
import com.lehaine.littlekt.graphics.toFloatBits

/**
 * @author Colton Daily
 * @date 3/11/2022
 */
open class Sprite : Renderable2D() {

    override val renderWidth: Float
        get() = if(overrideWidth) overriddenWidth  else textureSlice?.width?.toFloat() ?: 0f

    override val renderHeight: Float
        get() = if(overrideHeight) overriddenHeight else textureSlice?.height?.toFloat() ?: 0f

    var overrideWidth = false
    var overrideHeight = false

    var overriddenWidth = 0f
    var overriddenHeight = 0f

    /**
     * Flips the current rendering of the [Sprite] horizontally.
     */
    var flipX = false

    /**
     * Flips the current rendering of the [Sprite] vertically.
     */
    var flipY = false

    var textureSlice: TextureSlice? = null

    override fun render(batch: Batch, camera: Camera) {
        textureSlice?.let {
            batch.draw(
                it,
                x + localOffsetX,
                y + localOffsetY,
                anchorX * it.width,
                anchorY * it.height,
                width = renderWidth,
                height = renderHeight,
                scaleX = scaleX,
                scaleY = scaleY,
                flipX = flipX,
                flipY = flipY,
                rotation = rotation,
                colorBits = color.toFloatBits()
            )
        }
    }
}