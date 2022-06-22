package com.mars.infra.mixin.gradle.plugin.visitor

import com.mars.infra.mixin.gradle.plugin.core.Slice
import com.mars.infra.mixin.gradle.plugin.ext.ANNOTATION_PROXY
import com.mars.infra.mixin.gradle.plugin.ext.checkHookMethodExist
import com.mars.infra.mixin.gradle.plugin.model.SliceData
import com.mars.infra.mixin.gradle.plugin.model.ProxyData
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by Mars on 2022/3/15
 */
@Deprecated(message = "需要使用tree api拿到Insn")
class SliceCollectClassVisitor : ClassVisitor(Opcodes.ASM7) {

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

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv =  super.visitMethod(access, name, descriptor, signature, exceptions)
        return MixinCollectAdapter(owner, name, descriptor, mv)
    }

}


class MixinCollectAdapter(private val owner: String?,
                          private val methodName: String?,
                          private val methodDesc: String?,
                          methodVisitor: MethodVisitor?): MethodVisitor(Opcodes.ASM7, methodVisitor) {

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        println("MixinCollectAdapter----owner = $owner, method = $methodName, visitAnnotation = $descriptor, visible = $visible")

        var av = super.visitAnnotation(descriptor, visible)
        if (ANNOTATION_PROXY == descriptor) {
            // 记录hookClass
            val sliceData = SliceData(owner, methodName, methodDesc)
//            val proxyData = ProxyData()
//            Mixin.mixinDataList.add(mixinData)
            av = MixinCollectAnnotation(av, sliceData)
        }
        return av
    }
}

class MixinCollectAnnotation(annotationVisitor: AnnotationVisitor?,
                             private val sliceData: SliceData
): AnnotationVisitor(Opcodes.ASM7, annotationVisitor) {

    private val proxyData = ProxyData()

    override fun visit(name: String?, value: Any?) {
        super.visit(name, value)
        if (name == "owner") {
            proxyData.owner = value as String?
        } else if (name == "name") {
            proxyData.name = value as String?
        }
        println("MixinCollectAnnotation---visit---name = $name")
    }

    override fun visitEnd() {
        super.visitEnd()
        sliceData.proxyData = proxyData
        Slice.sliceDataList.add(sliceData)
        checkHookMethodExist(proxyData.owner!!, proxyData.name!!, {
            Slice.sliceDataMap[it] = sliceData
        }, {
            throw Exception("不能重复hook相同方法")
        })
    }
}