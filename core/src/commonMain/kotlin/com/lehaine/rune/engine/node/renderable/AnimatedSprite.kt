package com.lehaine.rune.engine.node.renderable

import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graphics.Animation
import com.lehaine.littlekt.graphics.AnimationPlayer
import com.lehaine.littlekt.graphics.TextureSlice
import com.lehaine.littlekt.util.SingleSignal
import com.lehaine.littlekt.util.signal1v
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration

@OptIn(ExperimentalContracts::class)
fun Node.animatedSprite(
    callback: AnimatedSprite.() -> Unit = {}
): AnimatedSprite {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return AnimatedSprite().also(callback).addTo(this)
}

/**
 * @author Colton Daily
 * @date 3/11/2022
 */
open class AnimatedSprite : Sprite() {

    private val player = AnimationPlayer<TextureSlice>().apply {
        onFrameChange = ::handleFrameChange
    }

    val anim = AnimationManager()

    val onFrameChanged: SingleSignal<Int> = signal1v()

    val totalFramesPlayed: Int get() = player.totalFramesPlayed
    val totalFrames: Int get() = player.totalFrames
    val currentFrameIdx: Int get() = player.currentFrameIdx

    override fun update(dt: Duration) {
        super.update(dt)

        player.update(dt)
        anim.update()
    }

    fun play(animation: Animation<TextureSlice>, times: Int = 1, force: Boolean = false) =
        player.play(animation, times, force)

    fun playLooped(animation: Animation<TextureSlice>, force: Boolean = false) = player.playLooped(animation, force)

    fun playOnce(animation: Animation<TextureSlice>, force: Boolean = false) = player.playOnce(animation, force)

    fun stop() = player.stop()

    private fun handleFrameChange(frame: Int) {
        slice = player.currentAnimation?.getFrame(frame) ?: slice
        onFrameChanged.emit(frame)
    }

    override fun onDestroy() {
        super.onDestroy()
        onFrameChanged.clear()
    }

    inner class AnimationManager {
        private val states = arrayListOf<AnimationState>()

        /**
         * Priority is represented by the deepest. The deepest has top priority while the shallowest has lowest.
         */
        fun registerState(anim: Animation<TextureSlice>, loop: Boolean = true, reason: () -> Boolean) {
            states.add(AnimationState(anim, loop, reason))
        }

        fun removeState(anim: Animation<TextureSlice>) {
            states.find { it.anim == anim }?.also { states.remove(it) }
        }

        fun removeAllStates() {
            states.clear()
        }

        internal fun update() {
            states.forEach { state ->
                if (state.reason() && player.currentAnimation !== state.anim) {
                    if (state.loop) {
                        playLooped(state.anim)
                    } else {
                        playOnce(state.anim)
                    }
                    return
                }
            }
        }
    }

    private data class AnimationState(val anim: Animation<TextureSlice>, val loop: Boolean, val reason: () -> Boolean)
}

fun AnimatedSprite.registerState(anim: Animation<TextureSlice>, loop: Boolean = true, reason: () -> Boolean) =
    this.anim.registerState(anim, loop, reason)

fun AnimatedSprite.removeState(anim: Animation<TextureSlice>) = this.anim.removeState(anim)
fun AnimatedSprite.removeAllStates() = anim.removeAllStates()