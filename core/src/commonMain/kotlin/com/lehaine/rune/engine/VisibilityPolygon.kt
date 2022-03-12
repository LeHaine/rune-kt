package com.lehaine.rune.engine

import kotlin.math.*

data class Point(var x: Double = 0.0, var y: Double = 0.0, var angle: Double = 0.0)
data class Intersection(var pt: Point = Point(), var param: Double = 0.0, var angle: Double = 0.0)
data class Segment(var p1: Point = Point(), var p2: Point = Point())
data class Ray(var p1: Point = Point(), var p2: Point = Point())

/**
 * Based off of the info here: https://www.redblobgames.com/articles/visibility/
 */
class VisibilityPolygon(private var sightX: Double = 0.0, private var sightY: Double = 0.0) {
    private val segments = arrayListOf<Segment>()
    var output = arrayListOf<Point>()

    private val points = arrayListOf<Point>()
    private val uniqueAngles = arrayListOf<Double>()
    private val pointMap = mutableMapOf<String, Boolean>()
    private val intersections = arrayListOf<Intersection>()
    private val tempRay = Ray()

    fun addSegment(x1: Double, y1: Double, x2: Double, y2: Double) {
        segments += Segment(Point(x1, y1), Point(x2, y2))
    }

    fun sweep() {
        output.clear()
        points.clear()
        segments.forEach {
            points += it.p1
            points += it.p2
        }
        pointMap.clear()
        val uniquePoints = points.filter { point ->
            val key = "${point.x},${point.y}"
            if (pointMap.contains(key)) {
                return@filter false
            } else {
                pointMap[key] = true
                return@filter true
            }
        }

        uniqueAngles.clear()

        uniquePoints.forEach {
            val angle = atan2(it.y - sightY, it.x - sightX)
            it.angle = angle
            uniqueAngles += angle - 0.00001
            uniqueAngles += angle
            uniqueAngles += angle + 0.00001
        }

        intersections.clear()

        uniqueAngles.forEach angles@{ angle ->
            val dx = cos(angle)
            val dy = sin(angle)

            tempRay.p1.apply {
                x = sightX
                y = sightY
            }
            tempRay.p2.apply {
                x = sightX + dx
                y = sightY + dy
            }

            var closestIntersection: Intersection? = null
            segments.forEach segments@{ segment ->
                val intersection = getIntersection(tempRay, segment) ?: return@segments
                if (closestIntersection == null || intersection.param < closestIntersection?.param!!) {
                    closestIntersection = intersection
                }
            }
            closestIntersection?.angle = angle
            closestIntersection?.also { intersections += it }
        }
        intersections.sortWith { a, b -> sign(a.angle - b.angle).toInt() }
        output.addAll(intersections.map { it.pt })
    }

    private fun getIntersection(ray: Ray, segment: Segment): Intersection? {
        // RAY in parametric: Point + Delta*T1
        val r_px = ray.p1.x
        val r_py = ray.p1.y
        val r_dx = ray.p2.x - ray.p1.x
        val r_dy = ray.p2.y - ray.p1.y

        // SEGMENT in parametric: Point + Delta*T2
        val s_px = segment.p1.x
        val s_py = segment.p1.y
        val s_dx = segment.p2.x - segment.p1.x
        val s_dy = segment.p2.y - segment.p1.y

        // Are they parallel? If so, no intersect
        val r_mag: Double = sqrt((r_dx * r_dx + r_dy * r_dy).toDouble())
        val s_mag: Double = sqrt((s_dx * s_dx + s_dy * s_dy).toDouble())
        if (r_dx / r_mag == s_dx / s_mag && r_dy / r_mag == s_dy / s_mag) {
            // Unit vectors are the same.
            return null
        }

        // SOLVE FOR T1 & T2
        // r_px+r_dx*T1 = s_px+s_dx*T2 && r_py+r_dy*T1 = s_py+s_dy*T2
        // ==> T1 = (s_px+s_dx*T2-r_px)/r_dx = (s_py+s_dy*T2-r_py)/r_dy
        // ==> s_px*r_dy + s_dx*T2*r_dy - r_px*r_dy = s_py*r_dx + s_dy*T2*r_dx - r_py*r_dx
        // ==> T2 = (r_dx*(s_py-r_py) + r_dy*(r_px-s_px))/(s_dx*r_dy - s_dy*r_dx)
        val T2 = (r_dx * (s_py - r_py) + r_dy * (r_px - s_px)) / (s_dx * r_dy - s_dy * r_dx)
        val T1 = (s_px + s_dx * T2 - r_px) / r_dx

        // Must be within parametric whatever for RAY/SEGMENT
        if (T1 < 0) {
            return null
        }
        if (T2 < 0 || T2 > 1) {
            return null
        }

        val intersection = Intersection()
        intersection.pt = Point(r_px + r_dx * T1, r_py + r_dy * T1)
        intersection.param = T1
        return intersection
    }
}