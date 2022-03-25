package com.mars.infra.mixin.annotations;

/**
 * Created by Mars on 2022/3/22
 * 调用原方法，即保留原MethodInsnNode指令
 *
 * 思路：
 * 1. 找到调用该方法对应的指令，修改其opcode的值，给定一个特殊值
 * 2. 在插入代码的时候，判断指令是否是特殊值，如果是，则插入原指令
 *
 * 也不一定需要设置特殊值，根据ProxyInsnChain的internal name判断也行
 *
 * 示例：
 * class HelloWorld1 {
 *
 *     public static int hookLogW(String tag, String msg) {
 *         int value = (int) ProxyInsnChain.proceed(tag, msg + " ---> TestMixin hook hookLogW success.");
 *         return value;
 *     }
 *
 *     public static int hookLogW_1(String tag, String msg) {
 *         return ProxyInsnChain.proceed1(tag, msg + " ---> hook success");
 *     }
 * }
 *
 * 上述两个方法对应的指令区别：
 *
 * 方法一：
 * il.add(new MethodInsnNode(INVOKESTATIC, "run/test/ProxyInsnChain", "proceed", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;", false));
 * il.add(new TypeInsnNode(CHECKCAST, "java/lang/Integer"));
 * il.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
 *
 * 方法二：
 * il.add(new MethodInsnNode(INVOKESTATIC, "run/test/ProxyInsnChain", "proceed1", "(Ljava/lang/String;Ljava/lang/String;)I", false));
 *
 * 因此如果改成方法一，那么就多了两个指令，将这两个指令过滤掉，就不报错了！
 */
public class ProxyInsnChain {

    // test two params
    public static Object proceed(String param1, String param2) {
        return null;
    }

    public static Object proceed(Object obj, String param1, String param2) {
        return null;
    }

    // 对比代码，不要删
//    public static int proceed(String param1, String param2) {
//        return 0;
//    }
}
