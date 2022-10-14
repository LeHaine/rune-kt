package com.lehaine.rune.engine

import com.lehaine.littlekt.graph.node.Node
import kotlin.reflect.KFunction
import kotlin.time.Duration


@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class ActionCreatorDslMarker

interface BaseActionNode {
    var time: Duration
    var executed: Boolean
    var signal: String?
    val executeUntil: () -> Boolean
    fun execute(dt: Duration)
    fun isDoneExecuting() = executed && executeUntil() && time <= Duration.ZERO && signal == null
}

/**
 * @author Colton Daily
 * @date 7/20/2021
 */
open class ActionCreator(
    val init: ActionCreator.() -> Unit = {}
) : BaseActionNode {

    private var initialized = false

    @PublishedApi
    internal val nodes = ArrayDeque<BaseActionNode>()

    private val signals = mutableMapOf<String, Boolean>()

    override var time: Duration = Duration.ZERO
    override var executed: Boolean = false
    override var signal: String? = null
    override val executeUntil: () -> Boolean = { nodes.isEmpty() }

    override fun execute(dt: Duration) {
        if (!initialized) {
            init(this)
            initialized = true
            executed = true
        }

        while (nodes.isNotEmpty()) {
            val node = nodes.first()
            if (!node.executed && node.time <= Duration.ZERO) {
                val signal = node.signal
                if (signal == null || signals[signal] == true) {
                    node.execute(dt)
                    node.executed = true
                    node.signal = null
                }
            }
            if (node.isDoneExecuting()) {
                nodes.removeFirst()
            } else {
                if (node.time > Duration.ZERO) {
                    node.time -= dt
                }
                break
            }
        }
    }

    /**
     *
     */
    fun waitFor(condition: () -> Boolean, callback: () -> Unit = {}) {
        action(untilCondition = condition, callback = callback)
    }

    fun waitFor(
        signal: String,
        callback: () -> Unit = {}
    ) {
        action(untilSignal = signal) {
            signals.remove(signal)
            callback()
        }
    }

    fun waitForSignal(
        callback: () -> Unit = {}
    ) = waitFor("", callback)


    fun wait(
        time: Duration,
        callback: () -> Unit = {}
    ) {
        action(time = time, callback = callback)
    }

    fun signal(signal: String = "") {
        signals[signal] = true
    }

    fun action(
        untilCondition: () -> Boolean = { true },
        untilSignal: String? = null,
        time: Duration = Duration.ZERO,
        callback: () -> Unit = {}
    ) {
        nodes.add(object : BaseActionNode {
            override var time: Duration = time
            override var signal: String? = untilSignal
            override var executed: Boolean = false
            override val executeUntil = untilCondition
            override fun execute(dt: Duration) = callback()
        })
    }

}

fun Node.actionCreator(
    block: @ActionCreatorDslMarker ActionCreator.() -> Unit = {}
): ActionCreator = ActionCreator().apply(block)