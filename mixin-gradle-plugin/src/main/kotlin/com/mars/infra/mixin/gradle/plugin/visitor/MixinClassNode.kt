package com.mars.infra.mixin.gradle.plugin.visitor

import com.mars.infra.mixin.gradle.plugin.core.MethodTransformer
import com.mars.infra.mixin.gradle.plugin.core.Mixin
import com.mars.infra.mixin.gradle.plugin.ext.PROXY_INSN_CHAIN_NAME
import com.mars.infra.mixin.gradle.plugin.ext.buildMixinMethodName
import com.mars.infra.mixin.gradle.plugin.ext.no
import com.mars.infra.mixin.gradle.plugin.model.MixinData
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
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
            it.access and Opcodes.ACC_ABSTRACT == 0
                    && it.access and Opcodes.ACC_NATIVE == 0
                    && it.name != "<init>"
                    && it.name != "<clinit>"
        }.forEach { methodNode ->
            transformer.transform(methodNode)
        }
        super.visitEnd()
        classVisitor?.let { accept(it) }
    }

    /**
     * 这里新增方法
     */
    override fun hook(insnNode: MethodInsnNode, mixinData: MixinData) {
        hasHook.no {
            // 需要判断一下相同方法如果已经添加过了，就不能重复添加。这里判断不准确，应该方法名不一定一致啊
            // 这里好像没有必要再次判断，重复的判断收敛到数据源
            val flag = "${mixinData.methodName.buildMixinMethodName()}-${mixinData.descriptor}"
            if (hookMethodSet.contains(flag)) {
                return
            }
            hookMethodSet.add(flag)
            val hookMethodNode = MethodNode(Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC,
                mixinData.methodName.buildMixinMethodName(),
                mixinData.descriptor, null, null)
            methods.add(hookMethodNode)

            /**
             * 原：mixinData.methodNode?.accept(hookMethodNode)
             * 但是，如果遇到MixinProxyInsn.proceed方法，需要将hooked method对应的指令写入
             *
             * 这里将hookLogW中的指令写入了_generate_hookLogW_mixin()方法体中，
             * 但是在写入过程中如果遇到MixinProxyInsn.proceed对应的指令需要将LogW对应的方法指令写入（原指令）
             */
            mixinData.methodNode?.accept(object: MethodVisitor(Opcodes.ASM7, hookMethodNode) {

                private var hasInvokeMixinProxyInsn = false

                // 美式方案，虽然粗糙了些，但是可以用
                private fun filter(opcode: Int, owner: String?, name: String?, descriptor: String?): Boolean {
                    if (opcode == Opcodes.INVOKEVIRTUAL
                        && owner == "java/lang/Integer"
                        && name == "intValue"
                        && descriptor == "()I") {
                        return true
                    }
                    if (opcode == Opcodes.INVOKEVIRTUAL
                        && owner == "java/lang/Long"
                        && name == "longValue"
                        && descriptor == "()J") {
                        return true
                    }
                    if (opcode == Opcodes.INVOKEVIRTUAL
                        && owner == "java/lang/Float"
                        && name == "floatValue"
                        && descriptor == "()F") {
                        return true
                    }
                    if (opcode == Opcodes.INVOKEVIRTUAL
                        && owner == "java/lang/Double"
                        && name == "doubleValue"
                        && descriptor == "()D") {
                        return true
                    }

                    if (opcode == Opcodes.INVOKEVIRTUAL
                        && owner == "java/lang/Boolean"
                        && name == "booleanValue"
                        && descriptor == "()Z") {
                        return true
                    }
                    if (opcode == Opcodes.INVOKEVIRTUAL
                        && owner == "java/lang/Character"
                        && name == "charValue"
                        && descriptor == "()C") {
                            Char
                        return true
                    }
                    if (opcode == Opcodes.INVOKEVIRTUAL
                        && owner == "java/lang/Byte"
                        && name == "byteValue"
                        && descriptor == "()B") {
                        Char
                        return true
                    }
                    if (opcode == Opcodes.INVOKEVIRTUAL
                        && owner == "java/lang/Short"
                        && name == "shortValue"
                        && descriptor == "()S") {
                        Char
                        return true
                    }
                    // 缺少数组
                    return false
                }


                override fun visitMethodInsn(
                    opcode: Int,
                    owner: String?,
                    name: String?,
                    descriptor: String?,
                    isInterface: Boolean
                ) {
                    // TODO 测试代码，如果调用Object MixinProxyInsn.proceed方法，则尝试过滤掉两个多余的指令
//                    if (filter(opcode, owner, name, descriptor)) {
//                        return
//                    }

                    if (owner == PROXY_INSN_CHAIN_NAME) {
                        // 原指令写入
                        hasInvokeMixinProxyInsn = true
                        insnNode.accept(mv)
                    } else {
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                    }
                }

                override fun visitTypeInsn(opcode: Int, type: String?) {
                    // TODO 暂时使用变量进行判断
                    if (opcode != Opcodes.CHECKCAST || !hasInvokeMixinProxyInsn) {
                        hasInvokeMixinProxyInsn = false  // 恢复
                        super.visitTypeInsn(opcode, type)
                    }
                }
            })
        }
    }

    private class MixinMethodTransformer(
        private val iHook: IHook,
        private val owner: String,
        methodTransformer: MethodTransformer?
    ) : MethodTransformer(methodTransformer) {

        override fun transform(node: MethodNode?) {
            super.transform(node)
            node ?: return
            node.instructions.iterator().forEach {
                when (it) {
                    is MethodInsnNode -> {
                        it.handleInsnNode(node, owner, iHook)
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

private fun MethodInsnNode.handleInsnNode(node: MethodNode, owner: String, iHook: IHook) {
    Mixin.mixinDataMap.values.forEach { mixinData ->
        val proxyData = mixinData.proxyData
        if (this.owner == proxyData?.owner
            && this.name == proxyData?.name
            && this.desc == proxyData?.descriptor) {
            iHook.hook(this, mixinData)
            node.modify(this, mixinData, owner)
        }
    }
}

/**
 * 在MethodNode中使用新指令替换insnNode旧指令
 */
private fun MethodNode.modify(insnNode: MethodInsnNode, mixinData: MixinData, owner: String) {
        val newMethodInsnNode =
            MethodInsnNode(
                Opcodes.INVOKESTATIC,  // 这里始终是调用静态方法
                owner,
                mixinData.methodName.buildMixinMethodName(),
                mixinData.descriptor,
                false
            )
    instructions.insert(insnNode, newMethodInsnNode)
    instructions.remove(insnNode)
}

interface IHook {
    fun hook(insnNode: MethodInsnNode, mixinData: MixinData)
}