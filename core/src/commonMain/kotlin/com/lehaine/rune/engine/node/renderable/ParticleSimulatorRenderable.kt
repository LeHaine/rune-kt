package com.lehaine.rune.engine.node.renderable

import com.lehaine.littlekt.graph.node.Node
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.node2d.Node2D
import com.lehaine.littlekt.graphics.Batch
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.ParticleSimulator
import com.lehaine.littlekt.graphics.TextureSlice
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.util.calculateViewBounds
import com.lehaine.littlekt.util.fastForEach
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
fun Node.particleSimulator(
    callback: ParticleSimulatorRenderable.() -> Unit = {}
): ParticleSimulatorRenderable {
    contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
    return ParticleSimulatorRenderable().also(callback).addTo(this)
}

class ParticleSimulatorRenderable : Node2D() {

    var maxParticles = 2048

    private val simulator by lazy { ParticleSimulator(maxParticles) }

    fun alloc(slice: TextureSlice, x: Float, y: Float) = simulator.alloc(slice, x, y)

    override fun render(batch: Batch, camera: Camera) {
        viewBounds.calculateViewBounds(camera)
        simulator.particles.fastForEach {
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