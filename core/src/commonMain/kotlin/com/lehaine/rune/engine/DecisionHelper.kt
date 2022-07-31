package com.lehaine.rune.engine

import com.lehaine.littlekt.util.fastForEach

@PublishedApi
internal data class DecisionElement<T>(val value: T) {
    var score = 0f
    var out = false
}

// https://github.com/deepnight/deepnightLibs/blob/master/src/dn/DecisionHelper.hx
class DecisionHelper<T>(initialData: MutableList<T>) {
    @PublishedApi
    internal val all = mutableListOf<DecisionElement<T>>().apply {
        initialData.forEach {
            add(DecisionElement(it))
        }
    }

    fun reset() {
        all.fastForEach {
            it.out = false
            it.score = 0f
        }
    }

    inline fun remove(decide: (T) -> Boolean) {
        all.fastForEach {
            if (!it.out && decide(it.value)) {
                it.out = true
            }
        }
    }

    fun removeValue(value: T) {
        all.fastForEach {
            if (!it.out && it.value == value) {
                it.out = true
            }
        }
    }

    inline fun keepOnly(decide: (T) -> Boolean) {
        all.fastForEach {
            if (!it.out && !decide(it.value)) {
                it.out = true
            }
        }
    }

    inline fun score(decideScore: (T) -> Float) {
        all.fastForEach {
            if (!it.out) {
                it.score += decideScore(it.value)
            }
        }
    }

    fun countRemaining(): Int = all.filter { !it.out }.size
    fun isEmpty(): Boolean = all.find { !it.out } != null
    fun isNotEmpty(): Boolean = !isEmpty()

    fun getBest(): T? {
        var best: DecisionElement<T>? = null
        all.fastForEach {
            if (!it.out && (best == null || it.score > (best?.score ?: 0f))) {
                best = it
            }
        }

        return best?.value
    }
}