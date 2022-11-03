package com.lehaine.rune.engine.node

import com.lehaine.littlekt.graph.node.FrameBufferNode
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.annotation.SceneGraphDslMarker
import com.lehaine.littlekt.math.MutableVec2f
import com.lehaine.littlekt.math.nextPowerOfTwo
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.min
import kotlin.time.Duration


@OptIn(ExperimentalContracts::class)
fun Node.pixelSmoothFrameBuffer(
    callback: @SceneGraphDslMarker PixelSmoothFrameBuffer.() -> Unit = {}
): PixelSmoothFrameBuffer {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return PixelSmoothFrameBuffer().also(callback).addTo(this)
}

/**
 * @author Colton Daily
 * @date 3/23/2022
 */
class PixelSmoothFrameBuffer : FrameBufferNode() {

    var targetHeight = 160
    var pxWidth = 0
    var pxHeight = 0

    var maxWidth = 0
    var maxHeight = 0

    /**
     * Mouse x-coordinate in world units.
     */
    val mouseX: Float get() = mouseWorldCoords.x

    /**
     * Mouse y-coordinate in world units.
     */
    val mouseY: Float get() = mouseWorldCoords.y

    private val mouseWorldCoords = MutableVec2f()

    override fun resize(width: Int, height: Int) {
        pxHeight = height / (height / targetHeight)
        pxWidth = (width / (height / pxHeight))
        if (maxWidth > 0) {
            pxWidth = min(pxWidth, maxWidth)
        }
        if (maxHeight > 0) {
            pxHeight = min(pxHeight, maxHeight)
        }
        resizeFbo(pxWidth.nextPowerOfTwo, pxHeight.nextPowerOfTwo)
        canvasCamera.ortho(this.width * ppuInv, this.height * ppuInv)
        canvasCamera.update()
    }

    override fun preUpdate(dt: Duration) {
        super.preUpdate(dt)
        val context = scene?.context ?: return
        val input = context.input
        val graphics = context.graphics
        mouseWorldCoords.x = (pxWidth / 100f) * ((100f / graphics.width) * input.x)
        mouseWorldCoords.y = (pxHeight / 100f) * ((100f / graphics.height) * input.y)
        mouseWorldCoords.x *= ppuInv
        mouseWorldCoords.y *= ppuInv
        mouseWorldCoords.x = mouseWorldCoords.x - width * ppuInv * 0.5f + canvasCamera.position.x
        mouseWorldCoords.y = mouseWorldCoords.y - height * ppuInv * 0.5f + canvasCamera.position.y
    }
}