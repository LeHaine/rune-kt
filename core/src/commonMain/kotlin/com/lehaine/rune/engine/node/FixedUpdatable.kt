package com.lehaine.rune.engine.node

import com.lehaine.littlekt.graph.node.Node

/**
 * @author Colton Daily
 * @date 3/14/2022
 */
interface FixedUpdatable {

    fun fixedUpdate()

    fun Node.findClosestFixedUpdater(): FixedUpdaterNode {
        var current: Node? = this
        while (current != null) {
            val parent = current.parent
            if (parent is FixedUpdaterNode) {
                return parent
            } else {
                current = parent
            }
            if (current != null && current == scene?.root) {
                error("Unable to find a FixedUpdater for $name. Ensure that an EntityNode is a descendant of a FixedUpdaterNode.")
            }
        }
        error("Unable to find a FixedUpdater for $name. Ensure that an EntityNode is a descendant of a FixedUpdaterNode.")
    }
}

