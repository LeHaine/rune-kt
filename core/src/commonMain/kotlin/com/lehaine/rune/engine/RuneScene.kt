package com.lehaine.rune.engine

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.graph.SceneGraph

/**
 * @author Colton Daily
 */
open class RuneScene(context: Context) : SceneGraph<String>(context) {

    val graphics get() = context.graphics
    val input get() = context.input
    val stats get() = context.stats
    val gl get() = context.gl
    val logger get() = context.logger
    val resourcesVfs get() = context.resourcesVfs
    val storageVfs get() = context.storageVfs
    val vfs get() = context.vfs
    val clipboard get() = context.clipboard

    var rune: Rune? = null
        internal set

    fun changeTo(scene: RuneScene) {
        val rune = rune
        check(rune != null) { "This scenes `Rune` property is null. This is most likely because the scene isn't set!" }
        rune.scene = scene
    }
}