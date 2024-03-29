package com.lehaine.rune.engine.node.renderable

import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.annotation.SceneGraphDslMarker
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.toFloatBits
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
fun Node.sprite(
    callback: @SceneGraphDslMarker Sprite.() -> Unit = {}
): Sprite {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return Sprite().also(callback).addTo(this)
}

/**
 * @author Colton Daily
 * @date 3/11/2022
 */
open class Sprite : Renderable2D() {

    override val renderWidth: Float
        get() = if (overrideWidth) overriddenWidth else slice?.width?.toFloat() ?: 0f

    override val renderHeight: Float
        get() = if (overrideHeight) overriddenHeight else slice?.height?.toFloat() ?: 0f

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

    var slice: TextureSlice? = null

    override fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        slice?.let {
            batch.draw(
                it,
                globalX + localOffsetX,
                globalY + localOffsetY,
                anchorX * it.originalWidth,
                anchorY * it.originalHeight,
                width = renderWidth,
                height = renderHeight,
                scaleX = globalScaleX * ppuInv,
                scaleY = globalScaleY * ppuInv,
                flipX = flipX,
                flipY = flipY,
                rotation = globalRotation,
                colorBits = color.toFloatBits()
            )
        }
    }
}