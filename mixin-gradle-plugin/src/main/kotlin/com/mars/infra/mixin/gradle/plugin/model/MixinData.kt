package com.mars.infra.mixin.gradle.plugin.model

import org.objectweb.asm.tree.MethodNode

/**
 * Created by Mars on 2022/3/15
 *
 * 举个例子：这是hookClass
 * owner: com/mars/infra/mixin/lib/Logger
 * methodName：superE
 * descriptor: (Ljava/lang/String;Ljava/lang/String;)I
 */
data class MixinData(
    val owner: String?,
    val methodName: String?,
    val descriptor: String?,
    var proxyData: ProxyData? = null,
    val methodNode: MethodNode? = null
)

/**
 * 举例：
 * owner: android/util/Log
 * name：e
 */
data class ProxyData(
    var owner: String? = null,
    var name: String? = null,
    var descriptor: String? = null,
    var isStatic: Boolean = true
)
