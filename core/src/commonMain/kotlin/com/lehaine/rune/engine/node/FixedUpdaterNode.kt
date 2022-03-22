package com.lehaine.rune.engine.node

import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.util.milliseconds
import com.lehaine.littlekt.util.seconds
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


class FixedUpdaterNode : Node() {
//    val fixedProgressionRatio: Float get() = _fixedProgressionRatio
//    var timesPerSecond: Int = 30
//        set(value) {
//            field = value
//            time = (1f / value).seconds
//        }
//
//    private var accum = 0.milliseconds
//    private var _fixedProgressionRatio = 1f
//    private var time = (1f / timesPerSecond).seconds
//
//    override fun update(dt: Duration) {
//        accum += dt
//        while (accum >= time) {
//            accum -= time
//            children.forEach {
//                it.fixedUpdate()
//            }
//        }
//
//        _fixedProgressionRatio = accum.milliseconds / time.milliseconds
//    }
}