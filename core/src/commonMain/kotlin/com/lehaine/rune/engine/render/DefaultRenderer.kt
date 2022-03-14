package com.lehaine.rune.engine.render

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graphics.Batch
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.util.calculateViewBounds
import com.lehaine.rune.engine.node.node2d.renderable.Renderable2D

/**
 * @author Colton Daily
 */
class DefaultRenderer(context: Context) : Renderer(context) {
    private val viewBounds: Rect = Rect()

    override fun render(batch: Batch, scene: SceneGraph<*>) {
        begin(batch)
        scene.render(::onRenderNode)
        end(batch)
    }

    private fun onRenderNode(node: Node, batch: Batch, camera: Camera) {
        viewBounds.calculateViewBounds(camera)
        if (node is Renderable2D && viewBounds.intersects(node.renderBounds)) {
            renderAfterStateCheck(node, batch)
        }
    }
}