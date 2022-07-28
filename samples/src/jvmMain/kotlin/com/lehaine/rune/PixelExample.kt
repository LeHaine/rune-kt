package com.lehaine.rune

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.createLittleKtApp
import com.lehaine.littlekt.file.vfs.readLDtkMapLoader
import com.lehaine.littlekt.file.vfs.readTexture
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.canvasLayer
import com.lehaine.littlekt.graph.node.ui.control
import com.lehaine.littlekt.graph.node.ui.label
import com.lehaine.littlekt.graphics.*
import com.lehaine.littlekt.input.Key
import com.lehaine.littlekt.math.MutableVec2f
import com.lehaine.littlekt.math.random
import com.lehaine.littlekt.util.seconds
import com.lehaine.rune.engine.GameLevel
import com.lehaine.rune.engine.Rune
import com.lehaine.rune.engine.RuneScene
import com.lehaine.rune.engine.node.EntityCamera2D
import com.lehaine.rune.engine.node.entityCamera2D
import com.lehaine.rune.engine.node.pixelPerfectSlice
import com.lehaine.rune.engine.node.pixelSmoothFrameBuffer
import com.lehaine.rune.engine.node.renderable.ParticleBatch
import com.lehaine.rune.engine.node.renderable.entity.LevelEntity
import com.lehaine.rune.engine.node.renderable.entity.cd
import com.lehaine.rune.engine.node.renderable.ldtkLevel
import com.lehaine.rune.engine.node.renderable.particleBatch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

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

    override var ppu: Float = 8f
    private val particleSimulator = ParticleSimulator(2048)
    private lateinit var topNormal: ParticleBatch
    private lateinit var smallCircle: TextureSlice
    private val mouseCoords = MutableVec2f()

    override suspend fun Node.initialize() {
        val person = resourcesVfs["test/heroIdle0.png"].readTexture(mipmaps = false).slice()
        val mapLoader = resourcesVfs["test/platformer.ldtk"].readLDtkMapLoader()
        smallCircle = resourcesVfs["test/fxSmallCircle0.png"].readTexture().slice()
        val world = mapLoader.loadMap(true)

        canvasLayer {
            val entityCamera: EntityCamera2D

            val fbo = pixelSmoothFrameBuffer {
                val level = ldtkLevel<String>(world.levels[0]) {
                    gridSize = 8
                }

                val player = player(level, 8f) {
                    slice = person
                    cx = 3
                    cy = 3

                    onUpdate += {
                        if (!cd.has("footstep")) {
                            cd("footstep", 250.milliseconds)
                            runDust(globalX, globalY, -1)
                        }
                    }
                }

                topNormal = particleBatch()

                entityCamera = entityCamera2D {
                    viewBounds.width = world.levels[0].pxWidth.toFloat()
                    viewBounds.height = world.levels[0].pxHeight.toFloat()
                    follow(player, true)
                    camera = canvasCamera

                    onUpdate += {
                        if (input.isKeyJustPressed(Key.Z)) {
                            targetZoom = 0.5f
                        }
                        if (input.isKeyJustPressed(Key.X)) {
                            targetZoom = 1f
                        }
                    }
                }

                onUpdate += {
                    mouseCoords.x = mouseX
                    mouseCoords.y = mouseY
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
        control {
            label {
                onUpdate += {
                    text = "Mouse coords: $mouseCoords"
                }
            }
        }
    }

    private fun create(num: Int, createParticle: (index: Int) -> Unit) {
        for (i in 0 until num) {
            createParticle(i)
        }
    }

    private fun allocTopNormal(slice: TextureSlice, x: Float, y: Float) =
        particleSimulator.alloc(slice, x, y).also { topNormal.add(it) }

    private fun runDust(x: Float, y: Float, dir: Int) {
        create(5) {
            val p = allocTopNormal(smallCircle, x, y)
            p.scale((0.15f..0.25f).random())
            p.color.set(DUST_COLOR).also { p.colorBits = DUST_COLOR_BITS }
            p.xDelta = (0.25f..0.75f).random() * dir
            p.yDelta = -(0.05f..0.15f).random()
            p.life = (0.05f..0.15f).random().seconds
            p.scaleDelta = (0.005f..0.015f).random()
        }
    }

    override fun update(dt: Duration) {
        super.update(dt)
        particleSimulator.update(dt, tmod)
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

    companion object {
        private val DUST_COLOR = Color.fromHex("#efddc0")
        private val DUST_COLOR_BITS = DUST_COLOR.toFloatBits()
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