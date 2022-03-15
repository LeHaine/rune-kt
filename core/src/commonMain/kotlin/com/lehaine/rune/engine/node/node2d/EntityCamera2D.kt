package com.lehaine.rune.engine.node.node2d

import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.annotation.SceneGraphDslMarker
import com.lehaine.littlekt.graph.node.node2d.Camera2D
import com.lehaine.littlekt.math.*
import com.lehaine.littlekt.math.geom.Angle
import com.lehaine.littlekt.math.geom.cosine
import com.lehaine.littlekt.math.geom.radians
import com.lehaine.littlekt.math.geom.sine
import com.lehaine.rune.engine.Cooldown
import com.lehaine.rune.engine.node.FixedUpdatable
import com.lehaine.rune.engine.node.FixedUpdaterNode
import com.lehaine.rune.engine.node.node2d.renderable.entity.EntityNode
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.*
import kotlin.time.Duration

@OptIn(ExperimentalContracts::class)
inline fun Node.entityCamera2d(
    callback: @SceneGraphDslMarker EntityCamera2D.() -> Unit = {}
): EntityCamera2D {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return EntityCamera2D().also(callback).addTo(this)
}

@OptIn(ExperimentalContracts::class)
inline fun SceneGraph<*>.entityCamera2d(
    callback: @SceneGraphDslMarker EntityCamera2D.() -> Unit = {}
): EntityCamera2D {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return root.entityCamera2d(callback)
}

class EntityCamera2D : Camera2D(), FixedUpdatable {
    val viewBounds: Rect = Rect()
    val snapToPixel: Boolean = true
    var clampToBounds = true
    var brakeDistanceNearBounds = 0.1f
    var following: EntityNode? = null
        private set

    var deadZonePctX = 0.04f
    var deadZonePctY = 0.1f

    var friction = 0.89f

    private var shakePower = 1f
    private var shakeFrames = 0

    private val width get() = virtualWidth * zoom
    private val height get() = virtualHeight * zoom

    private var dx = 0f
    private var dy = 0f

    private val rawFocus = MutableVec2f()
    private val clampedFocus = MutableVec2f()

    private var bumpX = 0f
    private var bumpY = 0f

    private val cd = Cooldown()

    private val virtualWidth: Int get() = viewport?.virtualWidth ?: 0
    private val virtualHeight: Int get() = viewport?.virtualHeight ?: 0

    private var fixedUpdater: FixedUpdaterNode? = null
    private val fixedProgressionRatio: Float get() = fixedUpdater?.fixedProgressionRatio ?: 1f
    private var lastX = 0f
    private var lastY = 0f

    override fun update(dt: Duration) {
        cd.update(dt)
        sync()
    }


    override fun onAddedToScene() {
        fixedUpdater = findClosestFixedUpdater()
    }

    override fun fixedUpdate() {
        lastX = x
        lastY = y
        val following = following
        if (following != null) {
            val angle = atan2(following.centerY - rawFocus.y, following.centerX - rawFocus.x).radians
            val distX = abs(following.centerX - rawFocus.x)
            if (distX >= deadZonePctX * width) {
                val speedX = 0.03f / zoom
                dx += angle.cosine * (0.8f * distX - deadZonePctX * width) * speedX
            }
            val distY = abs(following.centerY - rawFocus.y)
            if (distY >= deadZonePctY * height) {
                val speedY = 0.046f / zoom
                dy += angle.sine * (0.8f * distY - deadZonePctY * height) * speedY
            }
        }

        var frictX = friction - zoom * 0.054f * friction
        var frictY = frictX

        if (clampToBounds) {
            val brakeDistX = brakeDistanceNearBounds * width
            if (dx <= 0) {
                val brakeRatio = 1 - ((rawFocus.x - width * 0.5f) / brakeDistX).clamp(0f, 1f)
                frictX *= 1 - 1 * brakeRatio
            } else if (dx > 0) {
                val brakeRatio = 1 - (((viewBounds.width - width * 0.5f) - rawFocus.x) / brakeDistX).clamp(0f, 1f)
                frictY *= 1 - 0.9f * brakeRatio
            }

            val brakeDistY = brakeDistanceNearBounds * height
            if (dy < 0) {
                val brakeRatio = 1 - ((rawFocus.y - height * 0.5f) / brakeDistY).clamp(0f, 1f)
                frictX *= 1 - 0.9f * brakeRatio
            } else if (dy > 0) {
                val brakeRatio = 1 - (((viewBounds.height - height * 0.5f) - rawFocus.y) / brakeDistY).clamp(0f, 1f)
                frictY *= 1 - 0.9f * brakeRatio
            }
        }

        rawFocus.x += dx
        rawFocus.y += dy
        dx *= frictX.pow(2)
        dy *= frictY.pow(2)

        bumpX *= 0.75f
        bumpY *= 0.75f

        if (clampToBounds) {
            clampedFocus.x = if (viewBounds.width < width) {
                viewBounds.width * 0.5f
            } else {
                rawFocus.x.clamp(width * 0.5f, viewBounds.width - width * 0.5f)
            }

            clampedFocus.y = if (viewBounds.height < height) {
                viewBounds.height * 0.5f
            } else {
                rawFocus.y.clamp(height * 0.5f, viewBounds.height - height * 0.5f)
            }
        } else {
            clampedFocus.x = rawFocus.x
            clampedFocus.y = rawFocus.y
        }
    }

    private fun sync() {
        x = fixedProgressionRatio.interpolate(x, clampedFocus.x)
        y = fixedProgressionRatio.interpolate(y, clampedFocus.y)
        if (cd.has(SHAKE)) {
            x += cos(shakeFrames * 1.1f) * 2.5f * shakePower * cd.ratio(SHAKE)
            y += sin(0.3f + shakeFrames * 1.7f) * 2.5f * shakePower * cd.ratio(SHAKE)
            shakeFrames++
        } else {
            shakeFrames = 0
        }

        if (snapToPixel) {
            x = x.floor()
            y = y.floor()
        }
    }

    fun shake(time: Duration, power: Float = 1f) {
        cd.timeout(SHAKE, time)
        shakePower = power
    }

    fun bump(x: Float = 0f, y: Float = 0f) {
        bumpX += x
        bumpY += y
    }

    fun bump(x: Int = 0, y: Int = 0) = bump(x.toFloat(), y.toFloat())

    fun bump(angle: Angle, distance: Int) {
        bumpX += angle.cosine * distance
        bumpY += angle.radians * distance
    }

    fun follow(entity: EntityNode?, setImmediately: Boolean = false) {
        following = entity
        if (setImmediately) {
            entity ?: error("Target entity not set!!")
            position(entity.px, entity.py)
        }
    }

    fun unfollow() {
        following = null
    }

    companion object {
        private const val SHAKE = "shake"
    }
}