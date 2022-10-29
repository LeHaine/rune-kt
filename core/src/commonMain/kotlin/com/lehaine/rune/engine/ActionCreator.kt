package com.lehaine.rune.engine

import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.util.fastForEach
import kotlin.time.Duration


@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class ActionCreatorDslMarker


/**
 * An action creator to create cutscenes with sequential actions.
 * @author Colton Daily
 * @date 7/20/2021
 */
open class ActionCreator(
    var time: Duration = Duration.ZERO,
    var signal: String? = null,
    val executeUntil: () -> Boolean = { true },
    var parallel: Boolean = false,
    val init: ActionCreator.() -> Unit = {},
) {

    private var id = genId++
    private var initialized = false

    var executed: Boolean = false

    @PublishedApi
    internal val nodes = ArrayDeque<ActionCreator>()

    private val signals = mutableMapOf<String, Boolean>()

    fun execute(dt: Duration) {
        if (!initialized) {
            init(this)
            initialized = true
            executed = true
        }

        if (parallel) {
            nodes.fastForEach {
                handleNode(dt, it)
            }
        } else {
            while (nodes.isNotEmpty()) {
                val node = nodes.first()
                val continueNext = handleNode(dt, node)
                if (!continueNext) break
            }
        }
    }

    private fun handleNode(dt: Duration, node: ActionCreator): Boolean {
        if (!node.executed && node.time <= Duration.ZERO) {
            val signal = node.signal
            if (signal == null || signals[signal] == true) {
                node.execute(dt)
                node.executed = true
                node.signal = null
            }
        }
        return if (node.isDoneExecuting() && node.nodes.isEmpty()) {
            nodes.removeFirst()
            true
        } else {
            if (node.isDoneExecuting() && node.nodes.isNotEmpty()) {
                node.execute(dt)
            }
            if (node.time > Duration.ZERO) {
                node.time -= dt
            }
            parallel
        }
    }

    private fun isDoneExecuting() = executed && executeUntil() && time <= Duration.ZERO && signal == null

    fun waitFor(condition: () -> Boolean, init: @ActionCreatorDslMarker ActionCreator.() -> Unit = {}) {
        action(untilCondition = condition, init = init)
    }

    fun waitFor(
        signal: String,
        init: ActionCreator.() -> Unit = {}
    ) {
        action(untilSignal = signal) {
            signals.remove(signal)
            init()
        }
    }

    fun waitForSignal(
        init: @ActionCreatorDslMarker ActionCreator.() -> Unit = {}
    ) = waitFor("", init)


    fun wait(
        time: Duration,
        init: @ActionCreatorDslMarker ActionCreator.() -> Unit = {}
    ) {
        action(time = time, init = init)
    }

    fun signal(signal: String = "") {
        signals[signal] = true
    }

    fun action(
        time: Duration = Duration.ZERO,
        untilSignal: String? = null,
        untilCondition: () -> Boolean = { true },
        parallel: Boolean = false,
        init: @ActionCreatorDslMarker ActionCreator.() -> Unit = {}
    ) {
        ActionCreator(time, untilSignal, untilCondition, parallel, init).also { nodes += it }
    }

    fun parallel(
        time: Duration = Duration.ZERO,
        untilSignal: String? = null,
        untilCondition: () -> Boolean = { true },
        init: @ActionCreatorDslMarker ActionCreator.() -> Unit = {}
    ) = action(time, untilSignal, untilCondition, true, init)

    override fun toString(): String {
        return "ActionCreator(id=$id, time=$time, signal=$signal, executed=$executed, signals=$signals, nodes=$nodes)"
    }

    companion object {
        private var genId = 0
    }
}

fun Node.actionCreator(
    block: @ActionCreatorDslMarker ActionCreator.() -> Unit = {}
): ActionCreator = ActionCreator().apply(block)