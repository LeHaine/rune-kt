package com.lehaine.rune.engine.node

import com.lehaine.littlekt.graph.node.Node
import com.lehaine.rune.engine.Cooldown
import kotlin.time.Duration

class CooldownNode : Node() {
    private val cooldown = Cooldown()

    override fun update(dt: Duration) {
        cooldown.update(dt)
    }

    fun timeout(name: String, time: Duration, callback: () -> Unit = { }) =
        cooldown.timeout(name, time, callback)

    fun has(name: String) = cooldown.has(name)

    fun remove(name: String) = cooldown.remove(name)

    fun ratio(name: String): Float = cooldown.ratio(name)
}