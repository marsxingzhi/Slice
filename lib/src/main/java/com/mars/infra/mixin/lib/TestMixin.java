package com.mars.infra.mixin.lib;

import android.util.Log;

import com.mars.infra.mixin.annotations.Mixin;
import com.mars.infra.mixin.annotations.Proxy;
import com.mars.infra.mixin.annotations.ProxyInsnChain;

/**
 * Created by Mars on 2022/3/22
 */
@Mixin
class TestMixin {

    @Proxy(owner = "android/util/Log", name = "e")
    public static int superE(String tag, String msg) {
        System.out.println("打印日志--->TestMixin");
        return Log.e(tag, msg + " ---> hook success, TestMixin");
    }

    /**
     * hook Log.e，该方法有两个入参，这里也定义两个入参！
     */
//    @Proxy(owner = "android/util/Log", name = "w")
    public static int hookLogW(String tag, String msg) {
        Object value = ProxyInsnChain.proceed(tag, msg + " ---> TestMixin hook hookLogW success.");
        return (Integer) value;
    }

}
