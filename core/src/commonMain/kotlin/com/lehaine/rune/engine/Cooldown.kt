package com.lehaine.rune.engine

import com.lehaine.littlekt.util.datastructure.Pool
import com.lehaine.littlekt.util.fastForEach
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


private data class CooldownTimer(
    var time: Duration,
    var name: String,
    var callback: () -> Unit
) {
    val ratio get() = 1f - (elapsed / time).toFloat()
    var elapsed = 0.milliseconds
    val finished get() = elapsed >= time

    fun update(dt: Duration) {
        elapsed += dt
        if (finished) {
            callback()
        }
    }
}

class Cooldown {
    private val cooldownTimerPool = Pool(
        reset = {
            it.elapsed = 0.milliseconds
            it.time = 0.milliseconds
            it.name = ""
            it.callback = {}
        },
        gen = { CooldownTimer(0.milliseconds, "", {}) })

    private val timersNameToTimerInstance = mutableMapOf<String, CooldownTimer>()
    private val timers = arrayListOf<CooldownTimer>()

    fun update(dt: Duration) {
        timers.fastForEach { timer ->
            timer.update(dt)
            if (timer.finished) {
                timers.remove(timer)
                timersNameToTimerInstance.remove(timer.name)
                cooldownTimerPool.free(timer)
            }
        }
    }

    private fun addTimer(name: String, timer: CooldownTimer) {
        timers -= timer
        timers += timer
        timersNameToTimerInstance[name] = timer
    }

    private fun removeTimer(name: String) {
        timersNameToTimerInstance[name] ?: return
        timers.removeAll {
            val remove = it.name == name
            if (remove) {
                cooldownTimerPool.free(it)
            }
            remove
        }
    }

    private fun reset(name: String, time: Duration, callback: () -> Unit) {
        timersNameToTimerInstance[name]?.apply {
            this.time = time
            this.callback = callback
            this.elapsed = 0.milliseconds
        }
    }

    private fun interval(name: String, time: Duration, callback: () -> Unit = {}) {
        if (has(name)) {
            reset(name, time, callback)
            return
        }
        val timer = cooldownTimerPool.alloc().apply {
            this.time = time
            this.name = name
            this.callback = callback
        }
        addTimer(name, timer)
    }


    fun timeout(name: String, time: Duration, callback: () -> Unit = { }) =
        interval(name, time, callback)

    fun has(name: String) = timersNameToTimerInstance[name] != null

    fun remove(name: String) = removeTimer(name)

    fun ratio(name: String): Float {
        return timersNameToTimerInstance[name]?.ratio ?: 0f
    }
}