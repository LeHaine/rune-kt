package com.lehaine.rune.engine.node.node2d

import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.annotation.SceneGraphDslMarker
import com.lehaine.littlekt.graph.node.node2d.Node2D
import com.lehaine.littlekt.math.floor
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Duration
import com.lehaine.littlekt.util.Scaler as KtScaler

@OptIn(ExperimentalContracts::class)
inline fun Node.scaler(
    callback: @SceneGraphDslMarker Scaler.() -> Unit = {}
): Scaler {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return Scaler().also(callback).addTo(this)
}

@OptIn(ExperimentalContracts::class)
inline fun SceneGraph<*>.scaler(
    callback: @SceneGraphDslMarker Scaler.() -> Unit = {}
): Scaler {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return root.scaler(callback)
}

/**
 * @author Colton Daily
 * @date 3/15/2022
 */
class Scaler : Node2D() {

    var targetWidth = 960f
    var targetHeight = 540f

    var integerScale = true

    private var _scale: Float = 1f
    val combinedScale: Float get() = _scale

    override fun update(dt: Duration) {
        val graphics = scene?.context?.graphics ?: return
            var sx = graphics.width / targetWidth
            var sy = graphics.height / targetHeight
            if (integerScale) {
                sx = sx.floor()
                sy = sy.floor()
            }
            _scale = max(1f, min(sx, sy))

            scaleX = _scale
            scaleY = _scale
    }
}