package com.mars.infra.mixin.gradle.plugin.visitor

import com.mars.infra.mixin.gradle.plugin.Mixin
import com.mars.infra.mixin.gradle.plugin.MixinData
import com.mars.infra.mixin.gradle.plugin.buildMixinMethodName
import com.mars.infra.mixin.gradle.plugin.core.MethodTransformer
import com.mars.infra.mixin.gradle.plugin.no
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

/**
 * Created by Mars on 2022/3/14
 */
class MixinClassNode(private val classVisitor: ClassVisitor?) : ClassNode(Opcodes.ASM7), IHook {

    private var hasHook = false

    private var hookMethodSet = mutableSetOf<String>()

    override fun visitEnd() {

        val transformer = MixinMethodTransformer(this, name, null)

        methods.filter {
            (it.access and Opcodes.ACC_NATIVE) == 0 && (it.access and Opcodes.ACC_ABSTRACT) == 0
        }.forEach { methodNode ->
            transformer.transform(methodNode)
        }

        // TODO test
        if (name == "com/mars/infra/mixin/LoginService") {
            val methodNode = MethodNode(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, "test_123", "()V", null, null)
            methods.add(methodNode)

            val il = methodNode.instructions
            il.add(InsnNode(Opcodes.RETURN))

            methodNode.maxStack = 0
            methodNode.maxLocals = 0
        }
        // TODO 现在要做的就是将Logger中的superE方法添加到LoginService类中
        // 存在问题：关键现在遍历到LoginService了，但是怎么知道Logger的指令呢？
        // 是不是可以在第一遍的时候，就将指令保存起来？MethodNode指令保存一下

        super.visitEnd()
        classVisitor?.let { accept(it) }
    }

    /**
     * 这里新增方法
     */
    override fun hook(mixinData: MixinData) {
        hasHook.no {
//            cv.visitMethod()

            // 需要判断一下相同方法如果已经添加过了，就不能重复添加。这里判断不准确，应该方法名不一定一致啊
            val flag = "${mixinData.methodName.buildMixinMethodName()}-${mixinData.descriptor}"
            if (hookMethodSet.contains(flag)) {
                return
            }
            hookMethodSet.add(flag)
            val hookMethodNode = MethodNode(Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC,
                mixinData.methodName.buildMixinMethodName(),
                mixinData.descriptor, null, null)
            methods.add(hookMethodNode)

            mixinData.methodNode?.accept(hookMethodNode)

            // 这里不能过滤是否已经hook
//            hasHook = true
        }
    }

    private class MixinMethodTransformer(
        private val iHook: IHook,
        private val owner: String,
        methodTransformer: MethodTransformer?
    ) : MethodTransformer(methodTransformer) {

        override fun transform(node: MethodNode?) {
            super.transform(node)
//            println("MixinMethodTransformer---transform---node name = ${node?.name}")
            node ?: return

            // TODO 暂时 精准定位到LoginService#login方法
            if (owner == "com/mars/infra/mixin/LoginService" && node.name == "login" && node.desc == "()V") {
                println("MixinMethodTransformer---transform---call LoginService#login")

                node.instructions.iterator().forEach {
                    when (it) {
                        is MethodInsnNode -> {
                            // 简易版本
//                            if (it.owner == "android/util/Log" && it.name == "e" && it.desc == "(Ljava/lang/String;Ljava/lang/String;)I") {
//                                println("MixinMethodTransformer---transform---modifyMethodInsnNode")
//                                modifyMethodInsnNode(it, node)
//                            }
                            // 这里只是修改指令，使用Logger.superE替换Log.e，但是Logger这个类不应该存在!
                            it.handleInsnNode(node, owner, iHook)
                        }
                    }
                }

                // TODO test

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

private fun MethodInsnNode.handleInsnNode(node: MethodNode, owner: String, iHook: IHook) {
    Mixin.mixinDataList.forEach { mixinData ->
        val proxyData = mixinData.proxyData
        if (this.owner == proxyData?.owner
            && this.name == proxyData?.name
            && this.desc == "(Ljava/lang/String;Ljava/lang/String;)I"  // TODO proxtData新增descriptor属性
        ) {
            iHook.hook(mixinData)
            node.modify(this, mixinData, owner)
        }
    }
}

private fun MethodNode.modify(insnNode: MethodInsnNode, mixinData: MixinData, owner: String) {
    // 调用Logger.superE方法
//    val newMethodInsnNode =
//        MethodInsnNode(
//            insnNode.opcode,
//            mixinData.owner,
//            mixinData.methodName,
//            mixinData.descriptor,
//            false
//        )
    // 调用当前类中生成的_generate_superE_mixin方法
        val newMethodInsnNode =
            MethodInsnNode(
                insnNode.opcode,
                owner,
                mixinData.methodName.buildMixinMethodName(),
                mixinData.descriptor,
                false
            )
    instructions.insert(insnNode, newMethodInsnNode)
    instructions.remove(insnNode)
}

interface IHook {
    fun hook(mixinData: MixinData)
}