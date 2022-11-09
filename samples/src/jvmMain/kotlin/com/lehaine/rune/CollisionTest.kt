package com.lehaine.rune

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.createLittleKtApp
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.toFloatBits
import com.lehaine.littlekt.input.Key
import com.lehaine.littlekt.math.geom.degrees
import com.lehaine.rune.engine.Rune
import com.lehaine.rune.engine.RuneSceneDefault
import com.lehaine.rune.engine.node.renderable.entity.Entity
import com.lehaine.rune.engine.node.renderable.entity.entity
import com.lehaine.rune.engine.node.renderable.entity.toPixelPosition

/**
 * @author Colton Daily
 * @date 11/8/2022
 */
class CollisionTest(context: Context) : Rune(context) {

    override suspend fun Context.create() {
        scene = CollisionTestScene(context)
    }
}

class CollisionTestScene(context: Context) : RuneSceneDefault(context) {

    override var ppu: Float = 1f

    override suspend fun Node.initialize() {
        val greenBits = Color.GREEN.toFloatBits()
        val cyanBits = Color.CYAN.toFloatBits()
        val redBits = Color.RED.toFloatBits()

        showDebugInfo = true

        val dummies = mutableListOf<Entity>()
        val player = Entity(8f).apply {
            width = 256f
            height = 128f
            anchorX = 0f
            anchorY = 0f

            var currentBits = greenBits
            onDebugRender += { _, _, shapeRenderer ->
                shapeRenderer.filledRectangle(
                    left,
                    top,
                    width,
                    height,
                    globalRotation,
                    currentBits
                )
                shapeRenderer.circle(
                    centerX,
                    centerY,
                    outerRadius.toFloat(),
                    color = greenBits
                )
            }

            onUpdate += {
                if (input.isKeyPressed(Key.W)) {
                    velocityY = -1f
                }
                if (input.isKeyPressed(Key.S)) {
                    velocityY = 1f
                }
                if (input.isKeyPressed(Key.A)) {
                    velocityX = -1f
                }
                if (input.isKeyPressed(Key.D)) {
                    velocityX = 1f
                }
                if (input.isKeyPressed(Key.E)) {
                    globalRotation += 1.degrees
                }
                if (input.isKeyPressed(Key.Q)) {
                    globalRotation -= 1.degrees
                }

                currentBits = if (dummies.any { isCollidingWith(it, true) }) redBits else greenBits
            }
        }

        dummies += entity(8f) {
            width = 384f
            height = 256f
            anchorX = 0f
            anchorY = 0f

            toPixelPosition(500f, 100f)
            onDebugRender += { _, _, shapeRenderer ->
                shapeRenderer.filledRectangle(
                    left,
                    top,
                    width,
                    height,
                    globalRotation,
                    color = Color.YELLOW.toFloatBits()
                )
                shapeRenderer.circle(
                    centerX,
                    centerY,
                    outerRadius.toFloat(),
                    color = greenBits
                )
            }
        }

        dummies += entity(8f) {
            width = 75f
            height = 93f
            anchorX = 0f
            anchorY = 0f

            var currentBits = greenBits
            toPixelPosition(300f, 100f)

            onDebugRender += { _, _, shapeRenderer ->
                shapeRenderer.filledRectangle(
                    left,
                    top,
                    width,
                    height,
                    globalRotation,
                    color = currentBits
                )
                shapeRenderer.circle(
                    centerX,
                    centerY,
                    outerRadius.toFloat(),
                    color = greenBits
                )
            }

            onUpdate += {
                currentBits = if (isCollidingWith(player, true)) redBits else cyanBits
            }
        }


        player.addTo(this)

    }
}

fun main() {
    createLittleKtApp {
        width = 960
        height = 540
        backgroundColor = Color.DARK_GRAY
    }.start {
        CollisionTest(it)
    }
}