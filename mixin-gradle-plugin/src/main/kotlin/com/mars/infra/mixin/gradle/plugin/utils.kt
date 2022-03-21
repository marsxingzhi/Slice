package com.mars.infra.mixin.gradle.plugin

/**
 * Created by Mars on 2022/3/21
 */

fun String?.buildMixinMethodName(): String {
    return "_generate_${this}_mixin"
}