package com.mars.infra.mixin.gradle.plugin.ext

import com.mars.infra.mixin.gradle.plugin.core.Slice

/**
 * Created by Mars on 2022/3/21
 */

fun String?.buildMixinMethodName(): String {
    return "_generate_${this}_mixin"
}

fun checkHookMethodExist(owner: String, name: String,
                         success: (String) -> Unit, error: () -> Unit) {
    val key = "$owner#Slice#$name"
    if (Slice.sliceDataMap.containsKey(key)) {
        error.invoke()
    } else {
        success.invoke(key)
    }
}

fun String.getInternalName(): String {
    return this.replace(".", "/")
}