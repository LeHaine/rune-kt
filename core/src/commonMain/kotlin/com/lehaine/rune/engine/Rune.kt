package com.lehaine.rune.engine

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.ContextListener

/**
 * @author Colton Daily
 * @date 3/10/2022
 */
open class Rune(context: Context) : ContextListener(context) {

    /**
     * The internally handled scene
     */
    private var _scene: RuneScene? = null

    /**
     * The currently active [RuneScene].
     * Note: if set, the [RuneScene] will not actually change until the end of the [render]
     */
    var scene: RuneScene?
        get() = _scene
        set(value) {
            check(value != null) { "Scene can not be set to null!" }
            if (_scene == null) {
                _scene = value
                _scene?.apply {
                    rune = this@Rune
                }
                onSceneChanged()
                initialize = true

            } else {
                nextScene = value
            }
        }

    private var initialize = false

    private var nextScene: RuneScene? = null

    final override suspend fun Context.start() {
        create()
        onResize { width, height ->
            scene?.resize?.invoke(width, height)
        }

        onRender { dt ->
            scene?.let { _scene ->

                if (initialize) {
                    with(_scene) {
                        context.initialize()
                    }
                    _scene.resize(context.graphics.width, context.graphics.height)

                    initialize = false
                }

                _scene.preUpdate(dt)
                _scene.step(dt)
                _scene.postUpdate(dt)

                nextScene?.let { _nextScene ->
                    _scene.dispose()
                    this@Rune._scene = _nextScene
                    nextScene = null
                    _nextScene.apply {
                        rune = this@Rune
                    }
                    onSceneChanged()
                    with(_nextScene) {
                        context.initialize()
                    }
                    _nextScene.resize(context.graphics.width, context.graphics.height)
                }
            }
            scene?.render?.invoke()
            scene?.postRender?.invoke()
        }
    }

    open suspend fun Context.create() = Unit

    /**
     * Called after a [RuneScene] ends, before the next [RuneScene] begins.
     */
    open fun onSceneChanged() = Unit
}