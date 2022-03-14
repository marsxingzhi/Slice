package com.mars.infra.mixin.gradle.plugin

/**
 * Created by Mars on 2022/3/14
 */
sealed class BooleanExt<out T>

object Otherwise : BooleanExt<Nothing>()
class Data<T>(val data: T) : BooleanExt<T>()

inline fun <T> Boolean.yes(block: () -> T) =
    when {
        this -> {
            Data(block())
        }
        else -> {
            Otherwise
        }
    }

inline fun <T> Boolean.no(block: () -> T) = when {
    this -> Otherwise
    else -> {
        Data(block())
    }
}

inline fun <T> BooleanExt<T>.otherwise(block: () -> T): T =
    when (this) {
        is Otherwise -> block()
        is Data -> this.data
    }
