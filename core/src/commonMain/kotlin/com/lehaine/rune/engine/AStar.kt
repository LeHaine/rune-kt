package com.lehaine.rune.engine

import com.lehaine.littlekt.math.castRay
import com.lehaine.littlekt.math.distSqr
import com.lehaine.littlekt.util.fastForEach

/**
 * Source: https://github.com/deepnight/deepnightLibs/blob/master/src/dn/pathfinder/AStar.hx
 * @author Colton Daily
 * @date 7/25/2021
 */
class AStar(private val width: Int, private val height: Int, private val hasCollision: (Int, Int) -> Boolean) {
    private val nodes = arrayListOf<PathNode>()
    private var initialized = false

    fun init() {
        initialized = true
        nodes.clear()
        for (cy in 0 until height) {
            for (cx in 0 until width) {
                if (hasCollision(cx - 1, cy - 1) && !hasCollision(cx, cy - 1) && !hasCollision(cx - 1, cy)
                    || hasCollision(cx + 1, cy - 1) && !hasCollision(cx, cy - 1) && !hasCollision(cx + 1, cy)
                    || hasCollision(cx + 1, cy + 1) && !hasCollision(cx, cy + 1) && !hasCollision(cx + 1, cy)
                    || hasCollision(cx - 1, cy + 1) && !hasCollision(cx, cy + 1) && !hasCollision(cx - 1, cy)
                ) {
                    nodes.add(PathNode(width, cx, cy))
                }
            }
        }

        nodes.fastForEach { n ->
            nodes.fastForEach { n2 ->
                if (n != n2 && castRay(n.cx, n.cy, n2.cx, n2.cy)) {
                    n.link(n2)
                }
            }
        }
    }

    fun path(fx: Int, fy: Int, tx: Int, ty: Int): List<GridPoint> {
        check(initialized) { "AStar.init() needs to be called first!" }

        if (castRay(fx, fy, tx, ty)) {
            return listOf(GridPoint(tx, ty))
        }

        nodes.fastForEach {
            it.initBeforeAStar()
        }

        var added = 0
        var start = nodeAt(fx, fy)
        if (start == null) {
            added++
            start = PathNode(width, fx, fy)
            nodes.fastForEach {
                if (castRay(start.cx, start.cy, it.cx, it.cy)) {
                    start.link(it, true)
                }
            }
            nodes.add(start)
        }

        var end = nodeAt(tx, ty)
        if (end == null) {
            added++
            end = PathNode(width, tx, ty)
            nodes.fastForEach {
                if (castRay(end.cx, end.cy, it.cx, it.cy)) {
                    end.link(it, true)
                }
            }
            nodes.add(end)
        }
        val path = astar(start, end).map { GridPoint(it.cx, it.cy) }
        for (i in 0 until added) {
            nodes.removeLast()
        }
        return path
    }

    private fun astar(start: PathNode, end: PathNode): ArrayList<PathNode> {
        val open = arrayListOf(start)
        val openMap = hashMapOf(start.id to true)
        val closedMap = hashMapOf<Int, Boolean>()

        while (open.size > 0) {
            var best = -1
            for (i in 0 until open.size) {
                if (best < 0 || open[i].distTotalSqr(end.cx, end.cy) < open[best].distTotalSqr(end.cx, end.cy)) {
                    best = i
                }
            }

            val current = open[best]

            if (current == end) {
                val path = arrayListOf(current)
                var node = getDeepestParentOnSight(current)
                while (node != null) {
                    path.add(node)
                    node = getDeepestParentOnSight(node)
                }
                path.reverse()
                return path
            }

            closedMap[current.id] = true
            open.removeAt(best)
            openMap.remove(current.id)

            var i = 0
            current.links.fastForEach { n ->
                if (closedMap.contains(n.id)) {
                    return@fastForEach
                }

                val homeDist = current.homeDist + current.distSqr(n.cx, n.cy)
                var isBetter = false

                if (!openMap.contains(n.id)) {
                    isBetter = true
                    open.add(n)
                    openMap[n.id] = true
                } else if (homeDist < n.homeDist) {
                    isBetter = true
                }

                if (isBetter) {
                    n.parent = current
                    n.homeDist = homeDist
                    i++
                }
            }
        }

        return arrayListOf()
    }


    private fun nodeAt(cx: Int, cy: Int): PathNode? {
        nodes.fastForEach { n ->
            if (n.cx == cx && n.cy == cy) {
                return n
            }
        }
        return null
    }

    private fun castRay(fx: Int, fy: Int, tx: Int, ty: Int) =
        castRay(fx, fy, tx, ty, rayCanPass = { x, y -> !hasCollision(x, y) })

    private fun getDeepestParentOnSight(node: PathNode): PathNode? {
        var parent = node.parent
        var lastSight = parent
        while (parent != null) {
            if (castRay(node.cx, node.cy, parent.cx, parent.cy)) {
                lastSight = parent
            } else {
                return lastSight
            }
            parent = parent.parent
        }
        return null
    }
}

private class PathNode(cWdith: Int, val cx: Int, val cy: Int) {

    val id = cx + cy * cWdith

    var homeDist = 0f
    var parent: PathNode? = null

    val links = arrayListOf<PathNode>()
    private val _links = arrayListOf<PathNode>()

    fun initBeforeAStar() {
        homeDist = 0f
        parent = null
        links.clear()
        links.addAll(_links)
    }

    fun link(node: PathNode, tempLink: Boolean = false) {
        if (tempLink) {
            links.add(node)
            node.links.add(this)
        } else {
            _links.add(node)
            node._links.add(this)
        }
    }


    fun distSqr(tx: Int, ty: Int) = distSqr(cx, cy, tx, ty).toFloat()
    fun distTotalSqr(tx: Int, ty: Int) = homeDist + distSqr(tx, ty)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PathNode

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "PathNode(cx=$cx, cy=$cy, id=$id)"
    }
}