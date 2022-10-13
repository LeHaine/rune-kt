package com.lehaine.rune.engine.node.renderable

import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graphics.Batch
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.tilemap.ldtk.LDtkIntGridLayer
import com.lehaine.littlekt.graphics.tilemap.ldtk.LDtkLevel
import com.lehaine.littlekt.math.clamp
import com.lehaine.rune.engine.GameLevel
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
fun <LevelMark> Node.ldtkLevel(
    level: LDtkLevel,
    callback: LDtkGameLevelRenderable<LevelMark>.() -> Unit = {}
): LDtkGameLevelRenderable<LevelMark> {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return LDtkGameLevelRenderable<LevelMark>(level).also(callback).addTo(this)
}

open class LDtkGameLevelRenderable<LevelMark>(var level: LDtkLevel) : Renderable2D(), GameLevel<LevelMark> {
    override var gridSize: Int = 16

    override val renderWidth: Float
        get() = level.pxWidth.toFloat()

    override val renderHeight: Float
        get() = level.pxHeight.toFloat()

    var worldScale = 1f
        set(value) {
            field = value
            scaleX = value
            scaleY = value
        }

    val levelWidth get() = level.layers[0].gridWidth
    val levelHeight get() = level.layers[0].gridHeight

    protected val marks = mutableMapOf<LevelMark, MutableMap<Int, Int>>()

    // a list of collision layers indices from LDtk world
    protected val collisionLayers = intArrayOf(1)
    protected val collisionLayer = level["Collisions"] as LDtkIntGridLayer

    override fun isValid(cx: Int, cy: Int) = collisionLayer.isCoordValid(cx, cy)
    override fun getCoordId(cx: Int, cy: Int) = collisionLayer.getCoordId(cx, cy)

    override fun hasCollision(cx: Int, cy: Int): Boolean {
        return if (isValid(cx, cy)) {
            collisionLayers.contains(collisionLayer.getInt(cx, cy))
        } else {
            true
        }
    }

    override fun hasMark(cx: Int, cy: Int, mark: LevelMark, dir: Int): Boolean {
        return marks[mark]?.get(getCoordId(cx, cy)) == dir && isValid(cx, cy)
    }

    override fun setMarks(cx: Int, cy: Int, marks: List<LevelMark>) {
        marks.forEach {
            setMark(cx, cy, it)
        }
    }

    override fun setMark(cx: Int, cy: Int, mark: LevelMark, dir: Int) {
        if (isValid(cx, cy) && !hasMark(cx, cy, mark)) {
            if (!marks.contains(mark)) {
                marks[mark] = mutableMapOf()
            }

            marks[mark]?.set(getCoordId(cx, cy), dir.clamp(-1, 1))
        }
    }

    // set level marks at start of level creation to react to certain tiles
    protected open fun createLevelMarks() = Unit

    override fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        level.render(batch, camera, globalX, globalY, (globalScaleY / globalScaleY * globalScaleX) * ppuInv)
    }

}