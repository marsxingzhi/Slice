package com.mars.infra.mixin.gradle.plugin.visitor

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by Mars on 2022/3/15
 */
class MixinCollectClassVisitor : ClassVisitor(Opcodes.ASM7) {

    private var owner: String? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        owner = name
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
//        println("MixinCollectClassVisitor----owner = $owner, visitAnnotation = $descriptor, visible = $visible")
//        return try {
//            super.visitAnnotation(descriptor, visible)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv =  super.visitMethod(access, name, descriptor, signature, exceptions)
        return MixinCollectAdapter(owner, name, mv)
    }

}


class MixinCollectAdapter(private val owner: String?,
                          private val methodName: String?,
                          methodVisitor: MethodVisitor?): MethodVisitor(Opcodes.ASM7, methodVisitor) {

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        println("MixinCollectAdapter----owner = $owner, method = $methodName, visitAnnotation = $descriptor, visible = $visible")
        return super.visitAnnotation(descriptor, visible)
    }
}