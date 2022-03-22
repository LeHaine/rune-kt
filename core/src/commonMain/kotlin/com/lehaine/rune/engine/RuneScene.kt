package com.lehaine.rune.engine

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.util.fastForEach
import com.lehaine.littlekt.util.milliseconds
import com.lehaine.littlekt.util.seconds
import com.lehaine.rune.engine.renderable.Renderable2D
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * @author Colton Daily
 */
open class RuneScene(val context: Context) {

    val graphics get() = context.graphics
    val input get() = context.input
    val stats get() = context.stats
    val gl get() = context.gl
    val logger get() = context.logger
    val resourcesVfs get() = context.resourcesVfs
    val storageVfs get() = context.storageVfs
    val vfs get() = context.vfs
    val clipboard get() = context.clipboard

    val renderables = mutableListOf<Renderable2D>()
    open var ppu = 1f
    val ppuInv get() = 1f / ppu

    var rune: Rune? = null
        internal set

    protected var resize: (width: Int, height: Int) -> Unit = { _, _ -> }
    protected var preUpdate: (dt: Duration) -> Unit = {}
    protected var update: (dt: Duration) -> Unit = {}
    protected var postUpdate: (dt: Duration) -> Unit = {}
    protected var fixedUpdate: () -> Unit = {}
    protected var render: () -> Unit = {}
    protected var postRender: () -> Unit = {}
    var dispose: () -> Unit = {}

    val fixedProgressionRatio: Float get() = _fixedProgressionRatio
    var fixedTimesPerSecond: Int = 30
        set(value) {
            field = value
            time = (1f / value).seconds
        }
    var targetFPS = 60
    var tmod = 1f
        private set
    private var accum = 0.milliseconds
    private var _fixedProgressionRatio = 1f
    private var time = (1f / fixedTimesPerSecond).seconds

    internal fun step(dt: Duration) {
        tmod = dt.seconds * targetFPS
        accum += dt
        while (accum >= time) {
            accum -= time
            fixedUpdate()
            renderables.fastForEach {
                it.fixedUpdate()
            }
        }

        _fixedProgressionRatio = accum.milliseconds / time.milliseconds

        preUpdate(dt)
        renderables.fastForEach {
            it.preUpdate(dt)
        }
        update(dt)
        renderables.fastForEach {
            it.update(dt)
        }
        postUpdate(dt)
        renderables.fastForEach {
            it.postUpdate(dt)
        }
    }

    internal fun resize(width: Int, height: Int) {
        resize.invoke(width, height)
    }

    internal fun render() {
        render.invoke()
        postRender.invoke()
    }

    open suspend fun initialize() = Unit

    fun changeTo(scene: RuneScene) {
        val rune = rune
        check(rune != null) { "This scenes `Rune` property is null. This is most likely because the scene isn't set!" }
        rune.scene = scene
    }
}