package com.lehaine.rune

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.createLittleKtApp
import com.lehaine.littlekt.file.vfs.readLDtkMapLoader
import com.lehaine.littlekt.file.vfs.readTexture
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.canvasLayer
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.slice
import com.lehaine.littlekt.input.Key
import com.lehaine.rune.engine.GameLevel
import com.lehaine.rune.engine.Rune
import com.lehaine.rune.engine.RuneScene
import com.lehaine.rune.engine.node.EntityCamera2D
import com.lehaine.rune.engine.node.entityCamera2D
import com.lehaine.rune.engine.node.pixelPerfectSlice
import com.lehaine.rune.engine.node.pixelSmoothFrameBuffer
import com.lehaine.rune.engine.node.renderable.entity.LevelEntity
import com.lehaine.rune.engine.node.renderable.ldtkLevel
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/11/2022
 */
class PixelExample(context: Context) : Rune(context) {

    override suspend fun Context.create() {
        scene = PixelExampleScene(context)
    }
}

class PixelExampleScene(context: Context) : RuneScene(context) {

    override var ppu: Float = 1f


    override suspend fun Node.initialize() {
        val person = resourcesVfs["test/heroIdle0.png"].readTexture(mipmaps = false).slice()
        val mapLoader = resourcesVfs["test/platformer.ldtk"].readLDtkMapLoader()
        val world = mapLoader.loadMap(true)

        canvasLayer {
            val entityCamera: EntityCamera2D

            val fbo = pixelSmoothFrameBuffer {
                val level = ldtkLevel<String>(world.levels[0])

                val player = player(level, 8f) {
                    slice = person
                    cx = 3
                    cy = 3
                }

                entityCamera = entityCamera2D {
                    viewBounds.width = world.levels[0].pxWidth.toFloat()
                    viewBounds.height = world.levels[0].pxHeight.toFloat()
                    follow(player)
                    camera = fboCamera

                    onUpdate += {
                        if (input.isKeyJustPressed(Key.Z)) {
                            targetZoom = 0.5f
                        }
                        if (input.isKeyJustPressed(Key.X)) {
                            targetZoom = 1f
                        }
                    }
                }
            }

            pixelPerfectSlice {
                this.fbo = fbo
                onUpdate += {
                    scaledDistX = entityCamera.scaledDistX
                    scaledDistY = entityCamera.scaledDistY
                }
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun Node.player(level: GameLevel<*>, gridCellSize: Float, callback: Player.() -> Unit = {}): Player {
        contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
        return Player(level, gridCellSize).also(callback).addTo(this)
    }

    private class Player(level: GameLevel<*>, gridCellSize: Float) : LevelEntity(level, gridCellSize) {

        private var xDir = 0
        private var yDir = 0
        override fun update(dt: Duration) {
            super.update(dt)
            xDir = 0
            yDir = 0
            if (input.isKeyPressed(Key.W)) {
                yDir = -1
            }
            if (input.isKeyPressed(Key.S)) {
                yDir = 1
            }
            if (input.isKeyPressed(Key.D)) {
                xDir = 1
            }
            if (input.isKeyPressed(Key.A)) {
                xDir = -1
            }
        }

        override fun fixedUpdate() {
            super.fixedUpdate()
            velocityX += 0.12f * xDir
            velocityY += 0.12f * yDir
        }
    }
}

fun main() {
    createLittleKtApp {
        width = 960
        height = 540
        backgroundColor = Color.DARK_GRAY
    }.start {
        PixelExample(it)
    }
}