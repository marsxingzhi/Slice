package com.mars.infra.mixin.gradle.plugin.visitor

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by Mars on 2022/3/14
 */
class SliceClassVisitor(private val classVisitor: ClassVisitor): ClassVisitor(Opcodes.ASM7, classVisitor) {

}