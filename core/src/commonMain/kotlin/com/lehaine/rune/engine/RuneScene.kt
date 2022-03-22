package com.lehaine.rune.engine

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.util.milliseconds
import com.lehaine.littlekt.util.seconds
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * @author Colton Daily
 */
open class RuneScene(val context: Context) {

    var rune: Rune? = null
    var resize: (width: Int, height: Int) -> Unit = { _, _ -> }
    var preUpdate: (dt: Duration) -> Unit = {}
    var update: (dt: Duration) -> Unit = {}
    var postUpdate: (dt: Duration) -> Unit = {}
    var fixedUpdate: () -> Unit = {}
    var render: () -> Unit = {}
    var postRender: () -> Unit = {}
    var dispose: () -> Unit = {}

    val fixedProgressionRatio: Float get() = _fixedProgressionRatio
    var timesPerSecond: Int = 30
        set(value) {
            field = value
            time = (1f / value).seconds
        }

    private var accum = 0.milliseconds
    private var _fixedProgressionRatio = 1f
    private var time = (1f / timesPerSecond).seconds

    internal fun step(dt: Duration) {
        accum += dt
        while (accum >= time) {
            accum -= time
            fixedUpdate()
        }

        _fixedProgressionRatio = accum.milliseconds / time.milliseconds

        update(dt)
    }

    open suspend fun Context.initialize() = Unit

    fun changeTo(scene: RuneScene) {
        val rune = rune
        check(rune != null) { "This scenes `Rune` property is null. This is most likely because the scene isn't set!" }
        rune.scene = scene
    }
}