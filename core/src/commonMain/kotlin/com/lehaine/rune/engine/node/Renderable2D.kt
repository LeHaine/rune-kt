package com.lehaine.rune.engine.node

import com.lehaine.rune.engine.BlendMode
import com.lehaine.littlekt.graph.node.node2d.Node2D
import com.lehaine.littlekt.graphics.Color

abstract class Renderable2D : Node2D() {
    var width: Float = 0f
    var height: Float = 0f

    var blendMode: BlendMode = BlendMode.Alpha
    var color = Color.WHITE
}