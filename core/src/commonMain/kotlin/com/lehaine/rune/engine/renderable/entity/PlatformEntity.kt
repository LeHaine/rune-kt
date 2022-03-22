package com.lehaine.rune.engine.renderable.entity

import com.lehaine.rune.engine.GameLevel
import com.lehaine.rune.engine.RuneScene
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun RuneScene.platformEntity(
    level: GameLevel<*>,
    gridCellSize: Float,
    callback: PlatformEntity.() -> Unit = {}
): PlatformEntity {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return PlatformEntity(level, gridCellSize).also(callback).addTo(this)
}

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