package com.lehaine.rune.engine.node.renderable.entity

import com.lehaine.rune.engine.GameLevel
import kotlin.math.abs
import kotlin.math.floor

/**
 * @author Colton Daily
 * @date 11/7/2022
 */
open class ObliqueEntity(level: GameLevel<*>, gridCellSize: Float) : LevelEntity(level, gridCellSize) {
    override var rightCollisionRatio: Float = 0.8f
    override var leftCollisionRatio: Float = 0.2f
    override var bottomCollisionRatio: Float = 1f
    override var topCollisionRatio: Float = 0.3f
    override var useTopCollisionRatio: Boolean = true

    var wallSlideDelta: Float = 0.005f
    var wallSlideTolerance: Float = 0.015f
    var wallDeltaRightCollisionRatio: Float = 0.5f
    var wallDeltaLeftCollisionRatio: Float = 0.5f
    var wallDeltaBottomCollisionRatio: Float = 0.6f
    var wallDeltaTopCollisionRatio: Float = 0.6f

    var velocityZ: Float = 0f
    var frictionZ: Float = 0.82f
    var zr: Float = 0f
    var gravity: Float = 0.05f
    var hasGravity: Boolean = true

    override val attachY: Float
        get() = (cy + yr - zr) * gridCellSize

    override fun checkXCollision() {
        if (level.hasCollision(cx + 1, cy) && xr >= rightCollisionRatio) {
            xr = rightCollisionRatio
            velocityX *= 0.5f

            // check if player is stuck on wall / corner and help them by nudging them off it
            if (shouldNudge(yr, wallDeltaTopCollisionRatio, 1, -1, velocityY, true)) {
                velocityY -= wallSlideDelta // todo fix this calculation
            }
            if (shouldNudge(yr, wallDeltaBottomCollisionRatio, 1, 1, velocityY, false)) {
                velocityY += wallSlideDelta
            }

            onLevelCollision(1, 0)
        }

        if (level.hasCollision(cx - 1, cy) && xr <= leftCollisionRatio) {
            xr = leftCollisionRatio
            velocityX *= 0.5f

            // check if player is stuck on wall / corner and help them by nudging them off it
            if (shouldNudge(yr, wallDeltaTopCollisionRatio, -1, -1, velocityY, true)) {
                velocityY -= wallSlideDelta // todo fix this calculation
            }
            if (shouldNudge(yr, wallDeltaBottomCollisionRatio, -1, 1, velocityY, false)) {
                velocityY += wallSlideDelta
            }

            onLevelCollision(-1, 0)
        }
    }

    override fun checkYCollision() {
        val heightCoordDiff = if (useTopCollisionRatio) topCollisionRatio else floor(height / gridCellSize)
        if (level.hasCollision(cx, cy - 1) && yr <= heightCoordDiff) {
            yr = heightCoordDiff
            velocityY *= 0.5f

            // check if player is stuck on wall / corner and help them by nudging them off it
            if (shouldNudge(xr, wallDeltaLeftCollisionRatio, -1, 1, velocityX, true)) {
                velocityX -= wallSlideDelta // todo fix this calculation
            }
            if (shouldNudge(xr, wallDeltaRightCollisionRatio, 1, 1, velocityX, false)) {
                velocityX += wallSlideDelta
            }

            onLevelCollision(0, -1)
        }
        if (level.hasCollision(cx, cy + 1) && yr >= bottomCollisionRatio) {
            velocityY *= 0.5f
            yr = bottomCollisionRatio

            // check if player is stuck on wall / corner and help them by nudging them off it
            if (shouldNudge(xr, wallDeltaLeftCollisionRatio, -1, -1, velocityX, true)) {
                velocityX -= wallSlideDelta // todo fix this calculation
            }
            if (shouldNudge(xr, wallDeltaRightCollisionRatio, 1, -1, velocityX, false)) {
                velocityX += wallSlideDelta
            }

            onLevelCollision(0, 1)
        }
    }

    override fun updateGridPosition() {
        super.updateGridPosition()
        zr += velocityZ

        if (zr > 0 && hasGravity) {
            velocityZ -= gravity
        }

        if (zr < 0) {
            zr = 0f
            velocityZ = -velocityZ * 0.9f
            if (abs(velocityZ) <= 0.06f) {
                velocityZ = 0f
            }
            onLand()
        }


        velocityZ *= frictionZ
        if (abs(velocityZ) <= 0.0005f) {
            velocityZ = 0f
        }
    }

    open fun onLand() = Unit

    private fun shouldNudge(
        gridRatio: Float,
        collisionRatio1: Float,
        xDir: Int,
        yDir: Int,
        velocity: Float,
        lowerThanTolerance: Boolean
    ): Boolean {
        return gridRatio < collisionRatio1 && !level.hasCollision(
            cx + xDir,
            cy + yDir
        ) && ((lowerThanTolerance && velocity <= wallSlideTolerance) || (!lowerThanTolerance && velocity >= wallSlideTolerance))
    }
}