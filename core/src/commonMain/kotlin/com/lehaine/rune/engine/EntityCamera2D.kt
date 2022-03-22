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
import com.lehaine.rune.engine.renderable.entity.Entity
import kotlin.math.*
import kotlin.time.Duration

class EntityCamera2D : OrthographicCamera(0f, 0f) {
    val viewBounds: Rect = Rect()
    val offset = MutableVec2f()
    var clampToBounds = true
    var brakeDistanceNearBounds = 0.1f
    var following: Entity? = null
        private set

    var deadZonePctX = 0.04f
    var deadZonePctY = 0.1f

    var friction = 0.89f
    var bumpFrict = 0.85f
    var trackingSpeed = 1f
    var ppu = 1f

    var shakePower = 1f
    var shakeFrames = 0
    var dx = 0f
    var dy = 0f
    var dz = 0f
    var bumpX = 0f
    var bumpY = 0f
    var bumpZoomFactor = 0f
    var targetZoom = 1f
    var zoomSpeed = 0.0014f
    var zoomFrict = 0.9f
    val combinedZoom get() = zoom + bumpZoomFactor

    private val width get() = virtualWidth * combinedZoom
    private val height get() = virtualHeight * combinedZoom

    private val rawFocus = MutableVec2f()
    private val clampedFocus = MutableVec2f()

    private val cd = Cooldown()

    var scaledDistX: Float = 0f
        private set
    var scaledDistY: Float = 0f

    var tmod: Float = 1f

    fun update(dt: Duration, tmod: Float) {
        cd.update(dt)
        this.tmod = tmod
        updatePosition()
        sync()
    }

    fun updatePosition() {
        val tz = targetZoom
        if (tz != zoom) {
            if (tz > zoom) {
                dz += zoomSpeed
            } else {
                dz -= zoomSpeed
            }
        } else {
            dz = 0f
        }
        val prevZoom = zoom
        zoom += dz * tmod
        bumpZoomFactor *= (0.9f).pow(tmod)
        dz *= zoomFrict.pow(tmod)
        if (abs(tz - zoom) <= 0.05f * tmod) {
            dz *= (0.8f).pow(tmod)
        }

        if (prevZoom < tz && zoom >= tz || prevZoom > tz && zoom <= tz) {
            zoom = tz
            dz = 0f
        }

        val following = following
        if (following != null) {
            val angle = atan2(following.centerY.floor() - rawFocus.y, following.centerX.floor() - rawFocus.x).radians
            val distX = abs(following.centerX.floor() - rawFocus.x)
            if (distX >= deadZonePctX * width) {
                val speedX = 0.015f / combinedZoom * trackingSpeed
                dx += angle.cosine * (0.8f * distX - deadZonePctX * width) * speedX * tmod
            }
            val distY = abs(following.centerY.floor() - rawFocus.y)
            if (distY >= deadZonePctY * height) {
                val speedY = 0.023f / combinedZoom * trackingSpeed
                dy += angle.sine * (0.8f * distY - deadZonePctY * height) * speedY * tmod
            }
        }

        var frictX = friction - combinedZoom * 0.054f * friction
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

        rawFocus.x += dx * tmod
        rawFocus.y += dy * tmod
        dx *= frictX.pow(tmod)
        dy *= frictY.pow(tmod)

        bumpX *= bumpFrict.pow(tmod)
        bumpY *= bumpFrict.pow(tmod)

        if (clampToBounds) {
            clampedFocus.x = if (viewBounds.width < width - offset.x) {
                viewBounds.width * 0.5f
            } else {
                rawFocus.x.clamp(width * 0.5f - offset.x, viewBounds.width - width * 0.5f + offset.x)
            }

            clampedFocus.y = if (viewBounds.height < height - offset.y) {
                viewBounds.height * 0.5f
            } else {
                rawFocus.y.clamp(height * 0.5f - offset.y, viewBounds.height - height * 0.5f + offset.y)
            }
        } else {
            clampedFocus.x = rawFocus.x
            clampedFocus.y = rawFocus.y
        }
    }

    private fun sync() {
        var targetX = clampedFocus.x
        var targetY = clampedFocus.y
        if (cd.has(SHAKE)) {
            targetX += cos(shakeFrames * 1.1f) * 2.5f * shakePower * cd.ratio(SHAKE)
            targetY += sin(0.3f + shakeFrames * 1.7f) * 2.5f * shakePower * cd.ratio(SHAKE)
            shakeFrames++
        } else {
            shakeFrames = 0
        }


        val tx = (targetX * ppu).floor() / ppu
        val ty = (targetY * ppu).floor() / ppu
        scaledDistX = (targetX - tx) * ppu
        scaledDistY = (targetY - ty) * ppu

        position.x = tx + offset.x
        position.y = ty + offset.y
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