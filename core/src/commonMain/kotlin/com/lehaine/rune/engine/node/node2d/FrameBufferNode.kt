package com.lehaine.rune.engine.node.node2d

import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.annotation.SceneGraphDslMarker
import com.lehaine.littlekt.graphics.Batch
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.FrameBuffer
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun Node.frameBuffer(
    callback: @SceneGraphDslMarker FrameBufferNode.() -> Unit = {}
): FrameBufferNode {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return FrameBufferNode().also(callback).addTo(this)
}

@OptIn(ExperimentalContracts::class)
inline fun SceneGraph<*>.frameBuffer(
    callback: @SceneGraphDslMarker FrameBufferNode.() -> Unit = {}
): FrameBufferNode {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return root.frameBuffer(callback)
}

/**
 * @author Colton Daily
 * @date 3/14/2022
 */
class FrameBufferNode : Node() {

    var width: Int = 0
    var height: Int = 0
    var flipY: Boolean = false
    var upscale = 1f


    private var fbo: FrameBuffer? = null

    override fun onAddedToScene() {
        super.onAddedToScene()
        scene?.let { scene ->
            fbo = FrameBuffer(width, height).also { it.prepare(scene.context) }
        }
    }

    override fun _render(batch: Batch, camera: Camera, renderCallback: ((Node, Batch, Camera) -> Unit)?) {
        val fbo = fbo ?: return
        val context = scene?.context ?: return
        val gl = context.gl
        if (width == 0 || height == 0) return
        batch.end()
        gl.clearColor(Color.CLEAR)
        gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)
        fbo.begin()
        batch.begin()
        super._render(batch, camera, renderCallback)
        batch.end()
        fbo.end()


        batch.begin()
        batch.draw(fbo.colorBufferTexture, 0f, 0f, scaleX = upscale, scaleY = upscale, flipY = flipY)
        batch.end()
        batch.begin()
    }
}