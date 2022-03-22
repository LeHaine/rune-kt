package com.lehaine.rune

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.createLittleKtApp
import com.lehaine.littlekt.file.vfs.readLDtkMapLoader
import com.lehaine.littlekt.file.vfs.readTexture
import com.lehaine.littlekt.graphics.*
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.graphics.gl.State
import com.lehaine.littlekt.graphics.gl.TexMagFilter
import com.lehaine.littlekt.graphics.gl.TexMinFilter
import com.lehaine.littlekt.graphics.shader.ShaderProgram
import com.lehaine.littlekt.input.Key
import com.lehaine.littlekt.math.nextPowerOfTwo
import com.lehaine.littlekt.util.fastForEach
import com.lehaine.rune.engine.EntityCamera2D
import com.lehaine.rune.engine.GameLevel
import com.lehaine.rune.engine.Rune
import com.lehaine.rune.engine.RuneScene
import com.lehaine.rune.engine.renderable.entity.LevelEntity
import com.lehaine.rune.engine.renderable.ldtkLevel
import com.lehaine.rune.engine.shader.PixelSmoothFragmentShader
import com.lehaine.rune.engine.shader.PixelSmoothVertexShader
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

    val batch = SpriteBatch(context)
    var pxWidth = 0
    var pxHeight = 0
    val targetHeight = 160
    override var ppu: Float = 1f

    val sceneCamera = EntityCamera2D().apply {
        ortho(360f, 180f)
        clampToBounds = true
    }
    val viewportCamera = OrthographicCamera().apply {
        ortho(context.graphics.width, context.graphics.height)
        update()
    }
    val smoothCameraShader =
        ShaderProgram(PixelSmoothVertexShader(), PixelSmoothFragmentShader()).also { it.prepare(context) }

    override suspend fun initialize() {
        val person = resourcesVfs["test/heroIdle0.png"].readTexture(mipmaps = false).slice()
        val mapLoader = resourcesVfs["test/platformer.ldtk"].readLDtkMapLoader()
        val world = mapLoader.loadMap(true)

        var fbo =
            FrameBuffer(1, 1, minFilter = TexMinFilter.NEAREST, magFilter = TexMagFilter.NEAREST).also {
                it.prepare(context)
            }
        var fboRegion = TextureSlice(fbo.colorBufferTexture, 0, 0, fbo.width, fbo.height)

        val level = ldtkLevel<String>(world.levels[0])
        val player = player(level, 8f) {
            slice = person
            cx = 3
            cy = 3
        }
        sceneCamera.apply {
            viewBounds.width = world.levels[0].pxWidth.toFloat()
            viewBounds.height = world.levels[0].pxHeight.toFloat()
            follow(player)
        }

        resize = { width, height ->
            pxHeight = height / (height / targetHeight)
            pxWidth = (width / (height / pxHeight))
            fbo.dispose()
            fbo =
                FrameBuffer(
                    pxWidth.nextPowerOfTwo,
                    pxHeight.nextPowerOfTwo,
                    minFilter = TexMinFilter.NEAREST,
                    magFilter = TexMagFilter.NEAREST
                ).also {
                    it.prepare(context)
                }
            fboRegion = TextureSlice(fbo.colorBufferTexture, 0, (fbo.height - pxHeight), pxWidth, pxHeight)

            sceneCamera.apply {
                offset.set((fbo.width - pxWidth) * 0.5f, fbo.height * 0.5f - (fbo.height - pxHeight))
                ortho(fbo.width * ppuInv, fbo.height * ppuInv)
            }
        }

        update = {
            sceneCamera.update(it, tmod)
        }

        render = {
            gl.enable(State.SCISSOR_TEST)
            gl.scissor(0, 0, graphics.width, graphics.height)
            gl.clearColor(Color.DARK_GRAY)
            gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)

            if (input.isKeyJustPressed(Key.Z)) {
                sceneCamera.targetZoom = 0.5f
            }
            if (input.isKeyJustPressed(Key.X)) {
                sceneCamera.targetZoom = 1f
            }

            sceneCamera.update()
            fbo.begin()
            gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)
            batch.use(sceneCamera.viewProjection) {
                renderables.fastForEach {
                    it.render(batch, sceneCamera)
                }
            }
            fbo.end()

            batch.shader = smoothCameraShader
            viewportCamera.ortho(graphics.width, graphics.height)
            viewportCamera.update()
            batch.use(viewportCamera.viewProjection) {
                smoothCameraShader.vertexShader.uTextureSizes.apply(
                    smoothCameraShader,
                    fbo.width.toFloat(),
                    fbo.height.toFloat(),
                    0f,
                    0f
                )
                smoothCameraShader.vertexShader.uSampleProperties.apply(
                    smoothCameraShader,
                    0f,
                    0f,
                    sceneCamera.scaledDistX,
                    sceneCamera.scaledDistY
                )
                it.draw(
                    fboRegion,
                    0f,
                    0f,
                    width = graphics.width.toFloat(),
                    height = graphics.height.toFloat(),
                    flipY = true
                )
            }
            batch.shader = batch.defaultShader
        }

        postRender = {
            if (input.isKeyJustPressed(Key.P)) {
                logger.info { stats }
            }

            if (input.isKeyJustPressed(Key.ESCAPE)) {
                context.close()
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun RuneScene.player(level: GameLevel<*>, gridCellSize: Float, callback: Player.() -> Unit = {}): Player {
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
            //  println("grid pos: $cx,$cy,$xr,$yr --- real pos: $x,$y")
            velocityX += 0.12f * xDir
            velocityY += 0.12f * yDir
        }
    }
}

fun main() {
    createLittleKtApp {
        width = 960
        height = 540
    }.start {
        PixelExample(it)
    }
}