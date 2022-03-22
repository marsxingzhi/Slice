package com.mars.infra.mixin.annotations;

/**
 * Created by Mars on 2022/3/22
 * 调用原方法，即保留原MethodInsnNode指令
 *
 * 思路：
 * 1. 找到调用该方法对应的指令，修改其opcode的值，给定一个特殊值
 * 2. 在插入代码的时候，判断指令是否是特殊，如果是，则插入原指令
 */
public class ProxyInsnChain {

    // test two params
    public static Object proceed(String param1, String param2) {
        return null;
    }
}
