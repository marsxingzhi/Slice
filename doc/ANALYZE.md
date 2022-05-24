# 概述     
1、先收集相关指令   

2、插入、修改

# 收集阶段      
1、找到@Mixin标识的类，找到@Proxy注解标识的方法，进行收集，封装成MixinData对象     

2、MixinProxyInsn指令修改     

## 指令修改     
主要是修改MixinProxyInsn.invoke指令。  
```java
public class MixinProxyInsn {
    public static Object invoke(Object... args) {
        return null;
    }
}
```   
### 一、返回值修改      
在hook方法中调用了MixinProxyInsn.invoke方法，对应指令如下：   
```java
MethodInsnNode(INVOKESTATIC, "run/test/MixinProxyInsn", "handle", "(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/Object;", false)
InsnNode(POP)
```   
如果目标方法是没有返回值的，那么这里需要将POP指令删除，代码如下： 
```java
if (returnType == Type.VOID_TYPE) {
    // it对应MixinProxyInsn.invoke指令
    if (it.next.opcode == Opcodes.POP) { 
        methodNode.instructions.remove(it.next)
    }
} 
```    
如果目标方法有返回值，则需要进行类型转换，在这里将指令补上，示例如下：   

例一：    
```java
boolean res = (boolean) MixinProxyInsn.invoke(code);
对应指令：
MethodInsnNode(INVOKESTATIC, "run/test/MixinProxyInsn", "invoke", "([Ljava/lang/Object;)Ljava/lang/Object;", false)
TypeInsnNode(CHECKCAST, "java/lang/Boolean")
MethodInsnNode(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false)
```    
例二：    
```java
Boolean res = (Boolean) MixinProxyInsn.handle(username, password, code);
对应指令：
MethodInsnNode(INVOKESTATIC, "run/test/MixinProxyInsn", "invoke", "([Ljava/lang/Object;)Ljava/lang/Object;", false)
TypeInsnNode(CHECKCAST, "java/lang/Boolean")
```     
使用者不同的行为会出现可能会有拆箱指令，可能也没有拆箱指令。因此，Mixin中为了统一判断，则不采取删除指令(无法知道要不要删除拆箱的)，而是采取新增装箱的指令，具体逻辑：   

1、删除CHECKCAST指令  

2、添加装箱指令   
```java
val nextInsnNode = it.next
if (nextInsnNode.opcode == Opcodes.CHECKCAST && nextInsnNode is TypeInsnNode) {
    val boxType = typeMap[returnType]
    boxType?.let { boxType ->
        val boxInsnNode = MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    boxType.internalName,
                                    "valueOf",
                                    "(${returnType.descriptor})${boxType.descriptor}",
                                    false)
        methodNode.instructions.insert(it, boxInsnNode)
    }
    methodNode.instructions.remove(nextInsnNode)
}
```     
### 二、invoke指令脱糖     
一句话描述：MixinProxyInsn.invoke方法参数是可变参数，即描述符为`([Ljava/lang/Object;)Ljava/lang/Object;`，而目标方法的参数暂时不支持数组的。  

例如：`MixinProxyInsn.invoke(username, password, code)`对应的指令如下：      
```java
new InsnNode(ICONST_3);
new TypeInsnNode(ANEWARRAY, "java/lang/Object"));

new InsnNode(DUP);
new InsnNode(ICONST_0);
new VarInsnNode(ALOAD, 0);
new InsnNode(AASTORE);

new InsnNode(DUP);
new InsnNode(ICONST_1);
new VarInsnNode(ALOAD, 1);
new InsnNode(AASTORE);

new InsnNode(DUP);
new InsnNode(ICONST_2);
new VarInsnNode(ILOAD, 2);
new MethodInsnNode(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
new InsnNode(AASTORE);

new MethodInsnNode(INVOKESTATIC, "run/test/MixinProxyInsn", "handle", "([Ljava/lang/Object;)Ljava/lang/Object;", false);
new TypeInsnNode(CHECKCAST, "java/lang/Boolean");
new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
```   
上述指令可以分成5个部分：   

1、加载一个数组，大小为3     

2、Object[0] = ALOAD_0，将局部变量表中加载下标为0的对象，赋值给Object[0]，最后将Object[0]存储到局部变量表   

3、Object[1] = ALOAD_1，将局部变量表中加载下标为1的对象，赋值给Object[1]，最后将Object[1]存储到局部变量表  

4、Object[2] = ALOAD_2，将局部变量表中加载下标为2的对象，赋值给Object[2]，最后将Object[2]存储到局部变量表  

5、调用MixinProxyInsn.invoke指令

我们需要的指令最终如下：   
```java
new VarInsnNode(ALOAD, 0);
new VarInsnNode(ALOAD, 1);
new VarInsnNode(ILOAD, 2);
        
new MethodInsnNode(INVOKESTATIC, "run/test/MixinProxyInsn", "handle1", "(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/Object;", false);   
```      
因此，需要将上述多余的指令删除，代码如下：    
```java
fun MethodNode.desugarInstruction(argumentTypes: Array<Type>?, handleInsnNode: MethodInsnNode, proxyData: ProxyData) {
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

            // 强转指令，在调用MixinProxyInsn的invoke方法之前，如果是实例方法，需要将ALOAD_0的值进行强转
            if (i == 0 && !proxyData.isStatic) {
                val checkCastInsnNode = TypeInsnNode(Opcodes.CHECKCAST, proxyData.owner)
                //        il.add(new InsnNode(DUP));
                //        il.add(new InsnNode(ICONST_0));
                //        il.add(new VarInsnNode(ALOAD, 0));
                //        il.add(new TypeInsnNode(CHECKCAST, "run/test/Login"));
                //        il.add(new InsnNode(AASTORE));

//                instructions.insert(handleInsnNode.previous, checkCastInsnNode)
                instructions.insert(headNode.previous, checkCastInsnNode)  // 不是在handleInsnNode指令之前，而是在AASTORE之前
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
```    
# 修改阶段    

## 添加方法    

一句话描述：在调用目标方法的类中，新增静态方法，转而以静态方法取代目标方法。   

1、新建方法   
```java
val flag = "${mixinData.methodName.buildMixinMethodName()}-${mixinData.descriptor}"

val hookMethodNode = MethodNode(Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC,
                mixinData.methodName.buildMixinMethodName(),
                mixinData.descriptor, null, null)
methods.add(hookMethodNode)
```    
2、将hook方法的方法体的指令写入该新建的方法中    

如果遇到MixinProxyInsn.invoke指令，则写入原指令  
```java
mixinData.methodNode?.accept(object: MethodVisitor(Opcodes.ASM7, hookMethodNode) {

                private var hasInvokeMixinProxyInsn = false
                

                override fun visitMethodInsn(
                    opcode: Int,
                    owner: String?,
                    name: String?,
                    descriptor: String?,
                    isInterface: Boolean
                ) {

                    if (owner == PROXY_INSN_CHAIN_NAME) {
                        // 原指令写入
                        hasInvokeMixinProxyInsn = true
                        insnNode.accept(mv)
                    } else {
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                    }
                }

                override fun visitTypeInsn(opcode: Int, type: String?) {
                   
                    if (opcode != Opcodes.CHECKCAST || !hasInvokeMixinProxyInsn) {
                        hasInvokeMixinProxyInsn = false  
                        super.visitTypeInsn(opcode, type)
                    }
                }
            })
```    
## 调用新方法     
删除原方法的指令，插入新方法的指令    
```java
private fun MethodNode.modify(insnNode: MethodInsnNode, mixinData: MixinData, owner: String) {
        val newMethodInsnNode =
            MethodInsnNode(
                Opcodes.INVOKESTATIC,  
                owner,
                mixinData.methodName.buildMixinMethodName(),
                mixinData.descriptor,
                false
            )
    instructions.insert(insnNode, newMethodInsnNode)
    instructions.remove(insnNode)
}
```