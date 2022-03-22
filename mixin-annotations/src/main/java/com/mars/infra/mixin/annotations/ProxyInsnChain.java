package com.mars.infra.mixin.annotations;

/**
 * Created by Mars on 2022/3/22
 * 调用原方法，即保留原MethodInsnNode指令
 */
public class ProxyInsnChain {

    // test two params
    public static Object proceed(String param1, String param2) {
        return null;
    }
}
