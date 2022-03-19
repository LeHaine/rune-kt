package com.lehaine.rune.engine.node.node2d.renderable.entity

import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.annotation.SceneGraphDslMarker
import com.lehaine.littlekt.graphics.Batch
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.TextureSlice
import com.lehaine.littlekt.graphics.Textures
import com.lehaine.littlekt.math.interpolate
import com.lehaine.littlekt.util.*
import com.lehaine.rune.engine.distPxTo
import com.lehaine.rune.engine.node.CooldownNode
import com.lehaine.rune.engine.node.FixedUpdatable
import com.lehaine.rune.engine.node.FixedUpdaterNode
import com.lehaine.rune.engine.node.PostUpdatable
import com.lehaine.rune.engine.node.node2d.renderable.AnimatedSprite
import com.lehaine.rune.engine.toPixelPosition
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration

@OptIn(ExperimentalContracts::class)
inline fun Node.entity(gridCellSize: Float, callback: @SceneGraphDslMarker EntityNode.() -> Unit = {}): EntityNode {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return EntityNode(gridCellSize).also(callback).addTo(this)
}

@OptIn(ExperimentalContracts::class)
inline fun SceneGraph<*>.entity(
    gridCellSize: Float,
    callback: @SceneGraphDslMarker EntityNode.() -> Unit = {}
): EntityNode {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return root.entity(gridCellSize, callback)
}

open class EntityNode(val gridCellSize: Float) : FixedUpdatable, PostUpdatable, AnimatedSprite() {
    var cx: Int = 0
    var cy: Int = 0
    var xr: Float = 0.5f
    var yr: Float = 1f

    var gravityX: Float = 0f
    var gravityY: Float = 0f
    var gravityMultiplier: Float = 1f
    var velocityX: Float = 0f
    var velocityY: Float = 0f
    var frictionX: Float = 0.82f
    var frictionY: Float = 0.82f
    var maxGridMovementPercent: Float = 0.33f

    var width: Float = gridCellSize
    var height: Float = gridCellSize

    val innerRadius get() = min(width, height) * 0.5
    val outerRadius get() = max(width, height) * 0.5

    var interpolatePixelPosition: Boolean = true
    var lastPx: Float = 0f
    var lastPy: Float = 0f

    private var _stretchX = 1f
    private var _stretchY = 1f

    var stretchX: Float
        get() = _stretchX
        set(value) {
            _stretchX = value
            _stretchY = 2 - value
        }
    var stretchY: Float
        get() = _stretchY
        set(value) {
            _stretchX = 2 - value
            _stretchY = value
        }

    /**
     * The current entity x-scaling.
     */
    var entityScaleX = 1f

    /**
     * The current entity y-scaling.
     */
    var entityScaleY = 1f

    var restoreSpeed: Float = 12f

    var dir: Int = 1

    val px: Float
        get() {
            return if (interpolatePixelPosition) {
                fixedProgressionRatio.interpolate(lastPx, attachX)
            } else {
                attachX
            }
        }

    val py: Float
        get() {
            return if (interpolatePixelPosition) {
                fixedProgressionRatio.interpolate(lastPy, attachY)
            } else {
                attachY
            }
        }
    val attachX get() = (cx + xr) * gridCellSize
    val attachY get() = (cy + yr) * gridCellSize
    val centerX get() = attachX + (0.5f - anchorX) * gridCellSize
    val centerY get() = attachY + (0.5f - anchorY) * gridCellSize
    val top get() = attachY - anchorY * height
    val right get() = attachX + (1 - anchorX) * width
    val bottom get() = attachY + (1 - anchorY) * height
    val left get() = attachX - anchorX * width

    private var fixedUpdater: FixedUpdaterNode? = null

    /**
     * The ratio to interpolate the last position to the new position.
     */
    val fixedProgressionRatio: Float get() = fixedUpdater?.fixedProgressionRatio ?: 1f

    val cooldown by lazy { CooldownNode().addTo(this) }

    val onFixedUpdate: Signal = signal()
    val onPostUpdate: SingleSignal<Duration> = signal1v()

    init {
        anchorX = 0.5f
        anchorY = 1f
    }

    override fun onPositionChanged() {
        super.onPositionChanged()
        toPixelPosition(x, y)
    }

    override fun onAddedToScene() {
        fixedUpdater = findClosestFixedUpdater()
    }

    override fun render(batch: Batch, camera: Camera) {
        textureSlice?.let {
            batch.draw(
                it, globalX, globalY,
                anchorX * it.originalWidth,
                anchorY * it.originalHeight,
                scaleX = entityScaleX,
                scaleY = entityScaleY,
                rotation = globalRotation
            )
        }
    }

    override fun fixedUpdate() {
        updateGridPosition()
        onFixedUpdate.emit()
    }

    override fun postUpdate(dt: Duration) {
        position(px, py, false)
        entityScaleX = globalScaleX * dir * stretchX
        entityScaleY = globalScaleY * stretchY
        _stretchX += (1 - _stretchX) * min(1f, restoreSpeed * dt.seconds)
        _stretchY += (1 - _stretchY) * min(1f, restoreSpeed * dt.seconds)

        onPostUpdate.emit(dt)
    }


    /**
     * AABB check
     */
    fun isCollidingWith(from: EntityNode): Boolean {
        val lx = left
        val lx2 = from.left
        val rx = right
        val rx2 = from.right

        if (lx >= rx2 || lx2 >= rx) {
            return false
        }

        val ly = top
        val ry = bottom
        val ly2 = from.top
        val ry2 = from.bottom

        if (ly >= ry2 || ly2 >= ry) {
            return false
        }

        return true
    }

    fun isCollidingWithInnerCircle(from: EntityNode) = distPxTo(from) <= innerRadius
    fun isCollidingWithOuterCircle(from: EntityNode) = distPxTo(from) <= outerRadius

    fun onPositionManuallyChanged() {
        lastPx = attachX
        lastPy = attachY
    }

    open fun updateGridPosition() {
        lastPx = attachX
        lastPy = attachY

        velocityX += calculateDeltaXGravity()
        velocityY += calculateDeltaYGravity()

        /**
         * Any movement greater than [maxGridMovementPercent] will increase the number of steps here.
         * The steps will break down the movement into smaller iterators to avoid jumping over grid collisions
         */
        val steps = ceil(abs(velocityX) + abs(velocityY) / maxGridMovementPercent)
        if (steps > 0) {
            var i = 0
            while (i < steps) {
                xr += velocityX / steps

                if (velocityX != 0f) {
                    preXCheck()
                    checkXCollision()
                }

                while (xr > 1) {
                    xr--
                    cx++
                }
                while (xr < 0) {
                    xr++
                    cx--
                }

                yr += velocityY / steps

                if (velocityY != 0f) {
                    preYCheck()
                    checkYCollision()
                }

                while (yr > 1) {
                    yr--
                    cy++
                }

                while (yr < 0) {
                    yr++
                    cy--
                }
                i++
            }
        }
        velocityX *= frictionX
        if (abs(velocityX) <= 0.0005f) {
            velocityX = 0f
        }

        velocityY *= frictionY
        if (abs(velocityY) <= 0.0005f) {
            velocityY = 0f
        }
    }

    open fun calculateDeltaXGravity(): Float {
        return 0f
    }

    open fun calculateDeltaYGravity(): Float {
        return 0f
    }

    open fun preXCheck() = Unit
    open fun preYCheck() = Unit

    open fun checkXCollision() = Unit
    open fun checkYCollision() = Unit
}