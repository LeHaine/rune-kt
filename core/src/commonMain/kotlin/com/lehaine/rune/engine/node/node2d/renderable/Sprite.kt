package com.lehaine.rune.engine.node.node2d.renderable

import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.annotation.SceneGraphDslMarker
import com.lehaine.littlekt.graphics.Batch
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.TextureSlice
import com.lehaine.littlekt.graphics.toFloatBits
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun Node.sprite(callback: @SceneGraphDslMarker Sprite.() -> Unit = {}): Sprite {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return Sprite().also(callback).addTo(this)
}

@OptIn(ExperimentalContracts::class)
inline fun SceneGraph<*>.sprite(callback: @SceneGraphDslMarker Sprite.() -> Unit = {}): Sprite {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return root.sprite(callback)
}

/**
 * @author Colton Daily
 * @date 3/11/2022
 */
open class Sprite : Renderable2D() {

    override val renderWidth: Float
        get() = textureSlice?.width?.toFloat() ?: 0f

    override val renderHeight: Float
        get() = textureSlice?.height?.toFloat() ?: 0f

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
                globalX + localOffsetX,
                globalY + localOffsetY,
                anchorX * it.width,
                anchorY * it.width,
                scaleX = globalScaleX,
                scaleY = globalScaleY,
                flipX = flipX,
                flipY = flipY,
                colorBits = color.toFloatBits()
            )
        }
    }
}