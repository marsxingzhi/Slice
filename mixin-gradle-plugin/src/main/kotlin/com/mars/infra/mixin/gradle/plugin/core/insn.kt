package com.mars.infra.mixin.gradle.plugin.core

import com.mars.infra.mixin.gradle.plugin.ext.TYPE_ANY
import com.mars.infra.mixin.gradle.plugin.ext.boxNameMap
import com.mars.infra.mixin.gradle.plugin.ext.getInternalName
import com.mars.infra.mixin.gradle.plugin.ext.typeMap
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*


/**
 * 指令脱糖
 *
 * il.add(new InsnNode(ICONST_3));
 * il.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));
 *
 * il.add(new InsnNode(DUP));
 * il.add(new InsnNode(ICONST_0));
 * il.add(new VarInsnNode(ALOAD, 0));
 * il.add(new InsnNode(AASTORE));
 *
 * il.add(new InsnNode(DUP));
 * il.add(new InsnNode(ICONST_1));
 * il.add(new VarInsnNode(ALOAD, 1));
 * il.add(new InsnNode(AASTORE));
 *
 * il.add(new InsnNode(DUP));
 * il.add(new InsnNode(ICONST_2));
 * il.add(new VarInsnNode(ILOAD, 2));
 * il.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));
 * il.add(new InsnNode(AASTORE));
 *
 * il.add(new MethodInsnNode(INVOKESTATIC, "run/test/MixinProxyInsn", "handle", "([Ljava/lang/Object;)Ljava/lang/Object;", false));

 * 逆序查找，不清楚前面有多少逻辑，那么就找到离handleInsnNode最近的一个ANEWARRAY指令
 */
fun MethodNode.desugarInstruction(argumentTypes: Array<Type>?, handleInsnNode: MethodInsnNode) {
    // TODO 暂不支持无参的
    if (argumentTypes == null || argumentTypes.isEmpty()) {
        throw Exception("暂不支持无参函数的hook")
    }
    val lastAASTOREInsn = handleInsnNode.findValidPreviousInsnNode()
        ?: throw Exception("handle方法出现异常, errorCode = 1")

    var cur = lastAASTOREInsn.previous?: throw Exception("handle方法出现异常, errorCode = 1.5")

    // 找到ANEWARRAY
    while (!(cur.opcode == Opcodes.ANEWARRAY && cur is TypeInsnNode && cur.desc == TYPE_ANY.internalName)) {
        cur = cur.previous
    }
    // 当前cur对应TypeInsnNode(ANEWARRAY, "java/lang/Object")
    val pre = cur.previous?: throw Exception("handle方法出现异常, errorCode = 2")  // 对应数组size
    if (pre.opcode in Opcodes.ICONST_0..Opcodes.ICONST_5) {
        val size = pre.opcode - Opcodes.ICONST_0
        if (size == argumentTypes.size) {
            instructions.remove(pre)  // 删除InsnNode(ICONST_3)
        }
    }
    var headNode = cur.next
    instructions.remove(cur)  // 删除TypeInsnNode(ANEWARRAY, "java/lang/Object")

    // dup ---> aastore作为一个block，有几个入参，就有几个block
    for (index in argumentTypes.indices) {
        if (headNode.opcode != Opcodes.DUP) {
            throw Exception("handle方法出现异常, errorCode = 3")
        }
        val insnNode = headNode.findValidNextInsnNode()!!
        if (insnNode.opcode in Opcodes.ICONST_0..Opcodes.ICONST_5) {
            val i = insnNode.opcode - Opcodes.ICONST_0
            if (i != index) {
                throw Exception("handle方法出现异常, errorCode = 4")
            }
            instructions.remove(headNode)  // 删除InsnNode(DUP)
            headNode = insnNode.next?: throw Exception("handle方法出现异常, errorCode = 5") // 此时dupHead对应VarInsnNode(ALOAD, i)，这个需要保留

            instructions.remove(insnNode)  // 删除InsnNode(ICONST_0)

            // 寻找aastore了
            while (headNode != lastAASTOREInsn && headNode.opcode != Opcodes.AASTORE) {
                headNode = headNode.next
            }

            // dupHead此时等于InsnNode(AASTORE)
            if (i != argumentTypes.size - 1 && headNode == lastAASTOREInsn) {
                throw Exception("handle方法出现异常, errorCode = 6")
            }

            // 存在自动装箱，因此需要添加拆箱的指令。上述注释的第三段，就有自动装箱的逻辑
            // 所以需要在aastore之前添加上拆箱的逻辑，然后删除掉aastore指令
            val type = typeMap[argumentTypes[i]]
            val boxName = boxNameMap[argumentTypes[i]]
            type?.let {
                val unboxInsnNode = MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                    type.internalName,
                    boxName,
                    "()${argumentTypes[i].descriptor}",
                    false)
                instructions.insert(headNode.previous, unboxInsnNode)
            }

            val nextDup = headNode.findValidNextInsnNode()  // 下一块的开始节点，即下一个dup指令
            instructions.remove(headNode)  // 移除aastore指令
            headNode  = nextDup
        }
    }
}

/**
 * 查找有效的前序，即排除LineNumberNode和LabelNode
 */
fun AbstractInsnNode.findValidPreviousInsnNode(): AbstractInsnNode? {
    return previous?.let {
        if (it.isValidInsnNode()) {
            it
        } else {
            it.findValidPreviousInsnNode()
        }
    }
}

fun AbstractInsnNode.findValidNextInsnNode(): AbstractInsnNode? {
    return next?.let {
        if (it.isValidInsnNode()) {
            it
        } else {
            it.findValidNextInsnNode()
        }
    }
}

fun AbstractInsnNode.isValidInsnNode(): Boolean {
    return !(this is LineNumberNode || this is LabelNode)
}