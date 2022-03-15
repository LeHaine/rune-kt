package com.lehaine.rune.engine.node.node2d.renderable.entity

import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.annotation.SceneGraphDslMarker
import com.lehaine.rune.engine.GameLevel
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun Node.platformEntity(
    level: GameLevel<*>,
    gridCellSize: Float,
    callback: @SceneGraphDslMarker PlatformEntityNode.() -> Unit = {}
): PlatformEntityNode {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return PlatformEntityNode(level, gridCellSize).also(callback).addTo(this)
}

@OptIn(ExperimentalContracts::class)
inline fun SceneGraph<*>.platformEntity(
    level: GameLevel<*>,
    gridCellSize: Float,
    callback: @SceneGraphDslMarker PlatformEntityNode.() -> Unit = {}
): PlatformEntityNode {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return root.platformEntity(level, gridCellSize, callback)
}

open class PlatformEntityNode(level: GameLevel<*>, gridCellSize: Float) : LevelEntityNode(level, gridCellSize) {
    val onGround
        get() = velocityY == 0f && level.hasCollision(
            cx,
            cy + 1
        ) && yr == bottomCollisionRatio

    var hasGravity: Boolean = true

    private val gravityPulling get() = !onGround && hasGravity

    init {
        gravityY = 0.075f
    }

    override fun calculateDeltaYGravity(): Float {
        return if (gravityPulling) {
            gravityMultiplier * gravityY
        } else {
            0f
        }
    }
}