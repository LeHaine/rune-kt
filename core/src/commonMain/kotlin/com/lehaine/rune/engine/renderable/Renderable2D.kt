package com.lehaine.rune.engine.renderable

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.graphics.Batch
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.input.Input
import com.lehaine.littlekt.math.Mat3
import com.lehaine.littlekt.math.MutableVec2f
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.math.Vec2f
import com.lehaine.littlekt.math.geom.Angle
import com.lehaine.rune.engine.RuneScene
import com.lehaine.rune.engine.render.BlendMode
import kotlin.time.Duration

abstract class Renderable2D {
    protected val _position = MutableVec2f()
    val position: Vec2f get() = _position
    var x: Float
        get() = _position.x
        set(value) {
            _position.x = value
            onPositionChanged()
        }
    var y: Float
        get() = _position.y
        set(value) {
            _position.y = value
            onPositionChanged()
        }

    var rotation: Angle = Angle.ZERO

    protected val _scale = MutableVec2f(1f)
    val scale: Vec2f get() = _scale
    var scaleX: Float
        get() = _scale.x
        set(value) {
            _scale.x = value
        }
    var scaleY: Float
        get() = _scale.y
        set(value) {
            _scale.y = value
        }

    /**
     * The width of the [Renderable2D].
     */
    abstract val renderWidth: Float

    /**
     * The height of the [Renderable2D].
     */
    abstract val renderHeight: Float

    /**
     * The AABB that wraps this [Renderable2D]. Used for camera culling.
     */
    val renderBounds: Rect
        get() {
            if (boundsDirty) {
                calculateBounds(
                    position = position,
                    anchor = anchor,
                    scale = scale,
                    rotation = rotation,
                    width = _bounds.width,
                    height = _bounds.height
                )
                boundsDirty = false
            }
            return _bounds
        }

    val anchor: Vec2f get() = _anchor

    var anchorX: Float
        get() {
            return anchor.x
        }
        set(value) {
            if (value == _anchor.x) {
                return
            }
            _anchor.x = value
            boundsDirty = true
        }

    var anchorY: Float
        get() {
            return _anchor.y
        }
        set(value) {
            if (value == _anchor.y) {
                return
            }
            _anchor.y = value
            boundsDirty = true
        }

    val localOffset: Vec2f get() = _localOffset

    var localOffsetX: Float
        get() {
            return _localOffset.x
        }
        set(value) {
            if (value == _localOffset.x) {
                return
            }
            _localOffset.x = value
            boundsDirty = true
        }

    var localOffsetY: Float
        get() {
            return _localOffset.y
        }
        set(value) {
            if (value == _localOffset.y) {
                return
            }
            _localOffset.y = value
            boundsDirty = true
        }

    val context: Context
        get() = scene?.context
            ?: error("This Renderable2D could not get a context. Check to see if it is added to a RuneScene and if that RuneScene is the main scene!")
    val contextOrNull: Context? get() = scene?.context

    val input: Input get() = context.input

    /**
     * Used by a [Renderer] to specify how this [Renderable2D] should be rendered.
     */
    var blendMode: BlendMode = BlendMode.Alpha

    /**
     * Color that is passed along to the [Batch].
     */
    var color = Color.WHITE

    var scene: RuneScene? = null
        set(value) {
            if (field == value) return
            field?.renderables?.remove(this)
            field = value
            field?.renderables?.add(this)
        }

    val ppu: Float get() = scene?.ppu ?: 1f
    val ppuInv: Float get() = scene?.ppuInv ?: 1f

    private val transMat = Mat3()
    private val tempMat = Mat3()

    private val topLeft = MutableVec2f()
    private val topRight = MutableVec2f()
    private val bottomLeft = MutableVec2f()
    private val bottomRight = MutableVec2f()

    protected val _anchor = MutableVec2f()
    protected val _localOffset = MutableVec2f()

    protected var _bounds = Rect()
    protected var boundsDirty = true

    open fun onPositionChanged() = Unit
    open fun render(batch: Batch, camera: Camera) = Unit
    open fun preUpdate(dt: Duration) = Unit
    open fun update(dt: Duration) = Unit
    open fun postUpdate(dt: Duration) = Unit
    open fun fixedUpdate() = Unit
    open fun onDestroy() = Unit

    protected fun calculateBounds(
        position: Vec2f,
        anchor: Vec2f,
        scale: Vec2f,
        rotation: Angle,
        width: Float,
        height: Float
    ) {
        val originX = anchor.x * width
        val originY = anchor.y * height
        if (rotation == Angle.ZERO) {
            _bounds.let {
                it.x = position.x + originX - (width * anchorX * scale.x)
                it.y = position.y + originY - (height * anchorY * scale.y)
                it.width = width * scale.x
                it.height = height * scale.y
            }
        } else {
            val worldPosX = position.x
            val worldPosY = position.y

            transMat.setToTranslate(-worldPosX - originX, -worldPosY - originY)
            tempMat.setToScale(scale.x, scale.y) // scale
            transMat.mulLeft(tempMat)
            tempMat.setToRotation(rotation) // rotate
            transMat.mulLeft(tempMat)
            tempMat.setToTranslate(worldPosX + originX, worldPosY + originY) // translate back
            transMat.mulLeft(tempMat)

            // get four corners
            bottomLeft.set(worldPosX, worldPosY)
            bottomRight.set(worldPosX + width, worldPosY)
            topLeft.set(worldPosX, worldPosY + height)
            topRight.set(worldPosX + width, worldPosY + height)

            bottomLeft.mul(transMat)
            bottomRight.mul(transMat)
            topLeft.mul(transMat)
            topRight.mul(transMat)

            val minX = minOf(topLeft.x, bottomRight.x, topRight.x, bottomLeft.x)
            val maxX = maxOf(topLeft.x, bottomRight.x, topRight.x, bottomLeft.x)
            val minY = minOf(topLeft.y, bottomRight.y, topRight.y, bottomLeft.y)
            val maxY = maxOf(topLeft.y, bottomRight.y, topRight.y, bottomLeft.y)

            _bounds.set(minX, minY, maxX - minX, maxY - minY)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Renderable2D> addTo(scene: RuneScene): T {
        this.scene = scene
        return this as T
    }
}