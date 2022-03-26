package com.lehaine.rune.engine.node

import com.lehaine.littlekt.graph.node.FrameBufferNode
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.math.nextPowerOfTwo
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
fun Node.pixelSmoothFrameBuffer(
    callback: PixelSmoothFrameBuffer.() -> Unit = {}
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

    override fun resize(width: Int, height: Int) {
        pxHeight = height / (height / targetHeight)
        pxWidth = (width / (height / pxHeight))
        resizeFbo(pxWidth.nextPowerOfTwo, pxHeight.nextPowerOfTwo)
        canvasCamera.ortho(this.width * ppuInv, this.height * ppuInv)
        canvasCamera.update()
    }
}