package com.mars.infra.mixin.gradle.plugin.visitor

import com.mars.infra.mixin.gradle.plugin.core.MethodTransformer
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

/**
 * Created by Mars on 2022/3/14
 */
class MixinClassNode(private val classVisitor: ClassVisitor?) : ClassNode(Opcodes.ASM7) {

    override fun visitEnd() {

        val transformer = MixinMethodTransformer(name, null)

        methods.filter {
            (it.access and Opcodes.ACC_NATIVE) == 0 && (it.access and Opcodes.ACC_ABSTRACT) == 0
        }.forEach { methodNode ->
            transformer.transform(methodNode)
        }

        super.visitEnd()
        classVisitor?.let { accept(it) }
    }

    private class MixinMethodTransformer(private val owner: String, methodTransformer: MethodTransformer?) : MethodTransformer(methodTransformer) {

        override fun transform(node: MethodNode?) {
            super.transform(node)
//            println("MixinMethodTransformer---transform---node name = ${node?.name}")
            node ?: return

            // 精准定位到LoginService#login方法
            if (owner == "com/mars/infra/mixin/LoginService" && node.name == "login" && node.desc == "()V") {
                println("MixinMethodTransformer---transform---call LoginService#login")

                node.instructions.iterator().forEach {
                    when (it) {
                        is MethodInsnNode -> {
                            if (it.owner == "android/util/Log" && it.name == "e" && it.desc == "(Ljava/lang/String;Ljava/lang/String;)I") {

                                println("MixinMethodTransformer---transform---modifyMethodInsnNode")
                                modifyMethodInsnNode(it, node)
                            }
                        }
                    }
                }
            }
        }

        /**
         * 这里并未修改原先的MethodInsnNode的属性，而是生成一个新的MethodInsnNode，将其添加到链表中，删除旧的
         */
        private fun modifyMethodInsnNode(it: MethodInsnNode, node: MethodNode) {
            val newMethodInsnNode = MethodInsnNode(
                it.opcode,
                "com/mars/infra/mixin/lib/Logger",
                "superE",
                "(Ljava/lang/String;Ljava/lang/String;)I"
            )
            node.instructions.insert(it, newMethodInsnNode)
            node.instructions.remove(it)  // 看起来内部自己处理了链表的删除逻辑
        }
    }
}