package com.mars.infra.mixin.gradle.plugin.core

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

/**
 * Created by Mars on 2022/3/14
 */
abstract class ClassTransformer(private val ct: ClassTransformer?) {

    open fun transform(cn: ClassNode?) {
        ct?.transform(cn)
    }
}

abstract class MethodTransformer(private val mt: MethodTransformer?) {

    open fun transform(node: MethodNode?) {
        mt?.transform(node)
    }
}