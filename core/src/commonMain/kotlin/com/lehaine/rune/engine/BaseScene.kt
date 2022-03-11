package com.lehaine.rune.engine

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.Scene
import com.lehaine.rune.engine.ext.UpdateComponent
import com.lehaine.rune.engine.ext.createFixedInterpUpdater
import com.lehaine.rune.engine.ext.createTmodUpdater
import kotlin.time.Duration


open class BaseScene(context: Context) : Scene(context) {
    val updateComponents = mutableListOf<UpdateComponent>()

    final override suspend fun Context.render(dt: Duration) {
        updateComponents.forEach {
            it.update(dt)
        }
    }
}

fun <T : BaseScene> T.addFixedInterpUpdater(
    timesPerSecond: Float,
    initial: Boolean = true,
    interpolate: (ratio: Float) -> Unit,
    updatable: T.() -> Unit
) = createFixedInterpUpdater(timesPerSecond, initial, interpolate, updatable).also { updateComponents += it }

fun <T : BaseScene> T.addFixedInterpUpdater(
    time: Duration,
    initial: Boolean = true,
    interpolate: (ratio: Float) -> Unit,
    updatable: T.() -> Unit
) = createFixedInterpUpdater(time, initial, interpolate, updatable).also { updateComponents += it }


fun <T : BaseScene> T.addTmodUpdater(
    targetFPS: Int,
    updatable: T.(dt: Duration, tmod: Float) -> Unit
) = createTmodUpdater(targetFPS, updatable).also { updateComponents += it }