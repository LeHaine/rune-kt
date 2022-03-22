package com.lehaine.rune.engine.renderable

import com.lehaine.littlekt.graphics.Batch
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.Particle
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.util.calculateViewBounds
import com.lehaine.littlekt.util.fastForEach
import kotlin.time.Duration


class ParticleBatch : Renderable2D() {

    // max value because we handle culling internally
    override val renderWidth: Float = Float.MAX_VALUE
    override val renderHeight: Float = Float.MAX_VALUE

    private val particles = mutableListOf<Particle>()

    fun add(particle: Particle) {
        particles += particle
    }

    override fun update(dt: Duration) {
        particles.fastForEach {
            if (it.killed || !it.alive) {
                particles -= it
            }
        }
    }

    override fun render(batch: Batch, camera: Camera) {
        viewBounds.calculateViewBounds(camera)
        particles.fastForEach {
            if (!it.visible || !it.alive) return@fastForEach

            if (viewBounds.intersects(
                    it.x + x,
                    it.y + y,
                    it.slice.width.toFloat(),
                    it.slice.height.toFloat()
                )
            ) {

                batch.draw(
                    it.slice,
                    it.x + x,
                    it.y + y,
                    it.anchorX * it.slice.width,
                    it.anchorY * it.slice.height,
                    scaleX = it.scaleX * scaleX,
                    scaleY = it.scaleY * scaleY,
                    rotation = it.rotation + rotation,
                    colorBits = it.colorBits
                )
            }
        }
    }

    companion object {
        private val viewBounds: Rect = Rect()
    }
}