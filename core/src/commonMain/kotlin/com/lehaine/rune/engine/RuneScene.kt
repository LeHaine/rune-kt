package com.lehaine.rune.engine

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graph.addDefaultUiInput
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.util.viewport.ScreenViewport
import com.lehaine.littlekt.util.viewport.Viewport

/**
 * @author Colton Daily
 */
open class RuneScene<T>(
    context: Context,
    viewport: Viewport = ScreenViewport(
        context.graphics.width,
        context.graphics.height
    ),
    uiInputSignals: UiInputSignals<T>,
) : SceneGraph<T>(
    context,
    viewport,
    uiInputSignals = uiInputSignals
) {

    val graphics get() = context.graphics
    val input get() = context.input
    val stats get() = context.stats
    val gl get() = context.gl
    val logger get() = context.logger
    val resourcesVfs get() = context.resourcesVfs
    val storageVfs get() = context.storageVfs
    val vfs get() = context.vfs
    val clipboard get() = context.clipboard

    open var clearColor = Color.CLEAR

    var rune: Rune? = null
        internal set

    init {
        controller.addDefaultUiInput(uiInputSignals)
    }

    override fun render() {
        gl.clearColor(clearColor)
        gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)
        super.render()
    }

    fun changeTo(scene: RuneScene<*>) {
        val rune = rune
        check(rune != null) { "This scenes `Rune` property is null. This is most likely because the scene isn't set!" }
        rune.scene = scene
    }
}

/**
 * A [RuneScene] String default implementation for input.
 */
open class RuneSceneDefault(
    context: Context,
    viewport: Viewport = ScreenViewport(context.graphics.width, context.graphics.height),
    uiInputSignals: UiInputSignals<String> = UiInputSignals(
        "ui_accept",
        "ui_select",
        "ui_cancel",
        "ui_focus_next",
        "ui_focus_prev",
        "ui_left",
        "ui_right",
        "ui_up",
        "ui_down",
        "ui_home",
        "ui_end"
    )
) : RuneScene<String>(context, viewport, uiInputSignals)