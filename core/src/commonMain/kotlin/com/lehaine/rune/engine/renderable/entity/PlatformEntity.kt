package com.lehaine.rune.engine.renderable.entity

import com.lehaine.rune.engine.GameLevel

open class PlatformEntity(level: GameLevel<*>, gridCellSize: Float) : LevelEntity(level, gridCellSize) {
    val onGround
        get() = velocityY == 0f && level.hasCollision(
            cx,
            cy + 1
        ) && yr == bottomCollisionRatio

    var hasGravity: Boolean = true

    private val gravityPulling get() = !onGround && hasGravity

    init {
        gravityY = 0.075f
    }

    override fun calculateDeltaYGravity(): Float {
        return if (gravityPulling) {
            gravityMultiplier * gravityY
        } else {
            0f
        }
    }
}