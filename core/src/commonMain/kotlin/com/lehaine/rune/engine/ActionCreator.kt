package com.lehaine.rune.engine

import com.lehaine.littlekt.graph.node.Node
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class ActionCreatorDslMarker

interface BaseActionNode {
    var time: Duration
    var executed: Boolean
    val executeUntil: () -> Boolean
    fun execute(dt: Duration)
    fun isDoneExecuting() = executed && executeUntil() && (time <= 0.milliseconds || time == Duration.ZERO)
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

    override var time: Duration = Duration.ZERO
    override var executed: Boolean = false
    override val executeUntil: () -> Boolean = { nodes.isEmpty() }

    override fun execute(dt: Duration) {
        if (!initialized) {
            init(this)
            initialized = true
            executed = true
        }
        while (nodes.isNotEmpty()) {
            val node = nodes.first()
            if (!node.executed) {
                node.execute(dt)
                node.executed = true
            }
            if (node.isDoneExecuting()) {
                nodes.removeFirst()
            } else {
                if (node.time != Duration.ZERO) {
                    node.time -= dt
                }
                break
            }
        }
    }

    fun waitFor(condition: () -> Boolean) {
        action(untilCondition = condition)
    }

    fun wait(time: Duration) {
        action(time = time)
    }

    fun action(
        untilCondition: () -> Boolean = { true },
        time: Duration = Duration.ZERO,
        callback: () -> Unit = {}
    ) {
        nodes.add(object : BaseActionNode {
            override var time: Duration = time
            override var executed: Boolean = false
            override val executeUntil = untilCondition
            override fun execute(dt: Duration) = callback()
        })
    }
}


fun Node.actionCreator(
    block: @ActionCreatorDslMarker ActionCreator.() -> Unit = {}
): ActionCreator = ActionCreator().apply(block)