package com.lehaine.rune.engine.node.renderable

import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.node2d.Node2D
import com.lehaine.littlekt.graphics.Batch
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.Particle
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.util.calculateViewBounds
import com.lehaine.littlekt.util.fastForEach
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration


@OptIn(ExperimentalContracts::class)
fun Node.particleBatch(
    callback: ParticleBatch.() -> Unit = {}
): ParticleBatch {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return ParticleBatch().also(callback).addTo(this)
}

class ParticleBatch : Node2D() {

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
                    it.x + globalX,
                    it.y + globalY,
                    it.x + globalX + it.slice.width * it.scaleX * globalScaleX * ppuInv,
                    it.y + globalY + it.slice.height * it.scaleY * globalScaleY * ppuInv
                )
            ) {
                batch.draw(
                    it.slice,
                    it.x + globalX,
                    it.y + globalY,
                    it.anchorX * it.slice.width,
                    it.anchorY * it.slice.height,
                    scaleX = it.scaleX * globalScaleX * ppuInv,
                    scaleY = it.scaleY * globalScaleY * ppuInv,
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