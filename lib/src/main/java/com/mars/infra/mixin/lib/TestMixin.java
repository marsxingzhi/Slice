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
    public static int hookLogE(String tag, String msg) {
        System.out.println("打印日志--->TestMixin");
        return Log.e(tag, msg + " ---> hook success, TestMixin");
    }

    /**
     * hook Log.e，该方法有两个入参，这里也定义两个入参！
     *
     * Cannot constrain type: INT for value: v7 by constraint: OBJECT
     * 写指令时并未处理类型转换问题，因此暂时将proceed方法的返回值改成int
     */
    @Proxy(owner = "android/util/Log", name = "w")
    public static int hookLogW(String tag, String msg) {
        int value =  ProxyInsnChain.proceed(tag, msg + " ---> TestMixin hook hookLogW success.");
        return value;
    }

}
