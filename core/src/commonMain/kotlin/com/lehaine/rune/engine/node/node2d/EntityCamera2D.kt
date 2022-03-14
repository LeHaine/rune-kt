package com.lehaine.rune.engine.node.node2d

import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.annotation.SceneGraphDslMarker
import com.lehaine.littlekt.graph.node.node2d.Camera2D
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.math.clamp
import com.lehaine.littlekt.math.dist
import com.lehaine.littlekt.math.geom.Angle
import com.lehaine.littlekt.math.geom.cosine
import com.lehaine.littlekt.math.interpolate
import com.lehaine.rune.engine.Cooldown
import com.lehaine.rune.engine.node.node2d.renderable.entity.EntityNode
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
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

class EntityCamera2D : Camera2D() {
    val viewBounds: Rect = Rect()
    val snapToPixel: Boolean = true
    var deadZone: Int = 5
    var clampToBounds = true
    var following: EntityNode? = null
        private set

    private var shakePower = 1f
    private var shakeFrames = 0

    private val width get() = virtualWidth * zoom
    private val height get() = virtualHeight * zoom

    private var bumpX = 0f
    private var bumpY = 0f

    private val cd = Cooldown()

    val virtualWidth: Int get() = viewport?.virtualWidth ?: 0
    val virtualHeight: Int get() = viewport?.virtualHeight ?: 0

    override fun update(dt: Duration) {
        cd.update(dt)

        val following = following
        if (following != null) {
            val dist = dist(x, y, following.globalX, following.globalY)
            if (dist >= deadZone) {
                val speedX = 0.015f / zoom
                val speedY = 0.023f / zoom
                x = speedX.interpolate(x, following.globalX)
                y = speedY.interpolate(y, following.globalY)
            }
        }

        if (clampToBounds) {
            x = if (viewBounds.width < width) {
                viewBounds.width * 0.5f
            } else {
                x.clamp(width * 0.5f, viewBounds.width - width * 0.5f)
            }

            y = if (viewBounds.height < height) {
                viewBounds.height * 0.5f
            } else {
                y.clamp(height * 0.5f, viewBounds.height - height * 0.5f)
            }

        }
        bumpX *= 0.75f
        bumpY *= 0.75f

        x += bumpX
        y += bumpY

        if (cd.has(SHAKE)) {
            x += cos(shakeFrames * 1.1f) * 2.5f * shakePower * cd.ratio(SHAKE)
            y += sin(0.3f + shakeFrames * 1.7f) * 2.5f * shakePower * cd.ratio(SHAKE)
            shakeFrames++
        } else {
            shakeFrames = 0
        }

        if (snapToPixel) {
            x = x.roundToInt().toFloat()
            y = y.roundToInt().toFloat()
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