package com.lehaine.rune.engine.ext

import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.rune.engine.node.PostUpdatable
import kotlin.time.Duration

fun Node.postUpdate(dt: Duration) {
    if (this is PostUpdatable) {
        this.postUpdate(dt)
    }
    children.forEach {
        it.postUpdate(dt)
    }
}

fun SceneGraph<*>.postUpdate(dt: Duration) {
    root.postUpdate(dt)
}