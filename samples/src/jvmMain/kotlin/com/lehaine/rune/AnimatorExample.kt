package com.lehaine.rune

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.async.KtScope
import com.lehaine.littlekt.createLittleKtApp
import com.lehaine.littlekt.file.vfs.readTexture
import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.component.HAlign
import com.lehaine.littlekt.graph.node.ui.label
import com.lehaine.littlekt.graph.node.viewport
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.slice
import com.lehaine.littlekt.math.isFuzzyEqual
import com.lehaine.littlekt.util.seconds
import com.lehaine.littlekt.util.viewport.ExtendViewport
import com.lehaine.rune.engine.ActionCreator
import com.lehaine.rune.engine.Rune
import com.lehaine.rune.engine.RuneScene
import com.lehaine.rune.engine.actionCreator
import com.lehaine.rune.engine.node.renderable.Sprite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sign
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * @author Colton Daily
 * @date 10/13/2022
 */
class AnimatorExample(context: Context) : Rune(context) {

    override suspend fun Context.create() {
        scene = AnimatorExampleScene(context)
    }
}

class AnimatorExampleScene(context: Context) : RuneScene(context) {

    override var ppu: Float = 8f
    private val player = Player()
    private val player2 = Player()

    private lateinit var actionCreator: ActionCreator

    override suspend fun Node.initialize() {
        val person = resourcesVfs["test/heroIdle0.png"].readTexture(mipmaps = false).slice()

        viewport {
            viewport = ExtendViewport(30, 17)
            player.slice = person
            player.addTo(this)

            player2.slice = person
            player2.x = 12f
            player2.y = 7f
            player2.color = Color.LIGHT_CYAN.toMutableColor()
            player2.addTo(this)
        }

        actionCreator = actionCreator {
            logger.info { "starting" }
            fun end() {
                KtScope.launch {
                    delay(2.seconds)
                    signal()
                }
            }
            action { player.move(8, 8) }
            waitFor({ player.x == 8f && player.y == 8f })
            action {
                player.say("I have arrived")
                wait(1.seconds) {
                    player2.move(15, 10)
                    player2.say("Get away!!")
                }
            }
            wait(2.seconds) {
                player.laugh()
            }
            wait(3.seconds) {
                player.move(3,3)
            }
            waitFor({ player.x == 3f && player.y == 3f }) {
                player.say("I am back")
                player2.say("Phew.")
                end()
            }
            waitForSignal {
                player.say("Done.")
                player2.say("")
            }
        }
    }

    override fun update(dt: Duration) {
        super.update(dt)
        actionCreator.execute(dt)
    }

    private inner class Player : Sprite() {
        val speed = 2f
        var px = 0f
        var py = 0f
        var tx = -1
        var ty = -1

        val label = label {
            y = 3f
            horizontalAlign = HAlign.CENTER
            fontScaleX = 0.1f
            fontScaleY = 0.1f
        }

        fun move(x: Int, y: Int) {
            tx = x
            ty = y
        }

        fun say(message: String) {
            label.text = message
        }

        fun laugh() {
            label.text = "HA HA HA HA!"
        }

        override fun update(dt: Duration) {
            val eps = 0.05f
            if (tx != -1 && !x.isFuzzyEqual(tx.toFloat(), eps)) {
                val xDir = (tx - x).sign
                x += xDir * speed * dt.seconds
                if (x.isFuzzyEqual(tx.toFloat(), eps)) {
                    x = tx.toFloat()
                }
            }
            if (ty != -1 && !y.isFuzzyEqual(ty.toFloat(), eps)) {
                val yDir = (ty - y).sign
                y += yDir * speed * dt.seconds
                if (y.isFuzzyEqual(ty.toFloat(), eps)) {
                    y = ty.toFloat()
                }
            }

            if (x == tx.toFloat() && y == ty.toFloat() && px != x && py != y) {
                tx = -1
                ty = -1
            }
            px = x
            py = y
        }
    }
}

fun main() {
    createLittleKtApp {
        width = 960
        height = 540
        backgroundColor = Color.DARK_GRAY
    }.start {
        AnimatorExample(it)
    }
}