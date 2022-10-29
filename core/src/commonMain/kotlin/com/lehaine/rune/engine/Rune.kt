package com.lehaine.rune.engine

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.ContextListener
import com.lehaine.littlekt.async.KtScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
    private var initialSceneJob: Job? = null
    private var sceneChangeJob: Job? = null

    final override suspend fun Context.start() {
        create()
        onResize { width, height ->
            scene?.resize(width, height)
        }

        onRender { dt ->
            scene?.let { _scene ->

                if (initialize && initialSceneJob?.isActive != true) {
                    initialSceneJob = KtScope.launch {
                        sceneChangeJob?.join()
                        _scene.initialize()
                        _scene.resize(context.graphics.width, context.graphics.height)

                        initialize = false
                    }
                }

                if (sceneChangeJob?.isActive != true && initialSceneJob?.isActive != true) {
                    _scene.update(dt)
                }

                nextScene?.let { _nextScene ->
                    if (sceneChangeJob?.isActive != true) {
                        sceneChangeJob = KtScope.launch {
                            initialSceneJob?.join()
                            _scene.dispose()
                            this@Rune._scene = _nextScene
                            nextScene = null
                            _nextScene.apply {
                                rune = this@Rune
                            }
                            onSceneChanged()
                            _nextScene.initialize()
                            _nextScene.resize(context.graphics.width, context.graphics.height)
                        }
                    }
                }
            }
            if (sceneChangeJob?.isActive != true && initialSceneJob?.isActive != true) {
                scene?.render()
            }
        }
    }

    open suspend fun Context.create() = Unit

    /**
     * Called after a [RuneScene] ends, before the next [RuneScene] begins.
     */
    open fun onSceneChanged() = Unit
}