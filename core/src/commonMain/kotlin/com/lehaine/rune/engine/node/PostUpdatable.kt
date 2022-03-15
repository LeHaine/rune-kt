package com.lehaine.rune.engine.node

import kotlin.time.Duration

/**
 * @author Colton Daily
 * @date 3/14/2022
 */
interface PostUpdatable {
    fun postUpdate(dt: Duration)
}