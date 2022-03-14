# Mixin

AOP框架，使用注解标识需要aop的方法  

## TODO   
- [ ] 具体的ClassVisitor或者Transform抽象出来，单独作为一个模块
- [x] 替换指定类中的指定方法

### 石器时代       
**任务：** 修改LoginService#login方法中的Log.e方法，替换成Logger.superE
```java
public class LoginService {

    /**
     * aop替换Log.e方法，替换成Logger.superE
     */
    public static void login() {
        Log.e("LoginService", "invoke login");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```  
**修改方案：** 这里采用生成一个新的MethodInsnNode对象，将其添加到链表中，并且 删除原先Log.e语句对应的MethodInsnNode。关键代码如下：
```kotlin
private fun modifyMethodInsnNode(it: MethodInsnNode, node: MethodNode) {
    val newMethodInsnNode = MethodInsnNode(
            it.opcode,
            "com/mars/infra/mixin/lib/Logger",
            "superE",
            "(Ljava/lang/String;Ljava/lang/String;)I"
        )
    node.instructions.insert(it, newMethodInsnNode)
    node.instructions.remove(it) 
}
```      
详细代码，参考tag：v1.0
**注意：**
1. instruction修改前后需要保证操作数栈前后一致。例如：Log.e有返回值，替换的Logger.superE也需要有返回值，否则编译失败，出现了数组越界的问题。