package com.lehaine.rune.engine

import com.lehaine.littlekt.graphics.OrthographicCamera
import com.lehaine.littlekt.math.MutableVec2f
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.math.clamp
import com.lehaine.littlekt.math.floor
import com.lehaine.littlekt.math.geom.Angle
import com.lehaine.littlekt.math.geom.cosine
import com.lehaine.littlekt.math.geom.radians
import com.lehaine.littlekt.math.geom.sine
import com.lehaine.rune.engine.node.FixedUpdaterNode
import com.lehaine.rune.engine.renderable.entity.Entity
import kotlin.math.*
import kotlin.time.Duration

class EntityCamera2D : OrthographicCamera(0f, 0f) {
    var viewWidth: Float = 0f
    var viewHeight: Float = 0f
    private val viewBounds: Rect = Rect()
    var clampToBounds = true
    var brakeDistanceNearBounds = 0.1f
    var following: Entity? = null
        private set

    var snapToPixel = true
    var deadZonePctX = 0.04f
    var deadZonePctY = 0.1f

    var friction = 0.89f
    var trackingSpeed = 1f

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

    private var lastX = 0f
    private var lastY = 0f

    init {
        snapToPixel = true
    }

    fun update(dt: Duration) {
        cd.update(dt)
        viewBounds.width = viewWidth
        viewBounds.height = viewHeight
        sync()
    }

    fun fixedUpdate() {
        lastX = position.x
        lastY = position.y
        val following = following
        if (following != null) {
            val angle = atan2(following.py.floor() - rawFocus.y, following.px.floor() - rawFocus.x).radians
            val distX = abs(following.px.floor() - rawFocus.x)
            if (distX >= deadZonePctX * width) {
                val speedX = 0.06f / zoom
                dx += angle.cosine * (0.8f * distX - deadZonePctX * width) * speedX * trackingSpeed
            }
            val distY = abs(following.py.floor() - rawFocus.y)
            if (distY >= deadZonePctY * height) {
                val speedY = 0.09f / zoom
                dy += angle.sine * (0.8f * distY - deadZonePctY * height) * speedY * trackingSpeed
            }
        }

        var frictX = friction - zoom * 0.054f * friction
        var frictY = frictX

        if (clampToBounds) {
            val brakeDistX = brakeDistanceNearBounds * width
            if (dx <= 0) {
                val brakeRatio = 1 - ((rawFocus.x - width * 0.5f) / brakeDistX).clamp(0f, 1f)
                frictX *= 1 - 0.9f * brakeRatio
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
        var targetX = clampedFocus.x//fixedProgressionRatio.interpolate(lastX, clampedFocus.x)
        var targetY = clampedFocus.y//fixedProgressionRatio.interpolate(lastY, clampedFocus.y)
        if (cd.has(SHAKE)) {
            targetX += cos(shakeFrames * 1.1f) * 2.5f * shakePower * cd.ratio(SHAKE)
            targetY += sin(0.3f + shakeFrames * 1.7f) * 2.5f * shakePower * cd.ratio(SHAKE)
            shakeFrames++
        } else {
            shakeFrames = 0
        }

        val finalX = targetX - position.x
        val finalY = targetY - position.y

        position.x += if (snapToPixel) finalX.roundToInt().toFloat() else finalX
        position.y += if (snapToPixel) finalY.roundToInt().toFloat() else finalY
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

    fun follow(entity: Entity?, setImmediately: Boolean = false) {
        following = entity
        if (setImmediately) {
            entity ?: error("Target entity not set!!")
            position.set(entity.px, entity.py, 0f)
        }
    }

    fun unfollow() {
        following = null
    }

    companion object {
        private const val SHAKE = "shake"
    }

}