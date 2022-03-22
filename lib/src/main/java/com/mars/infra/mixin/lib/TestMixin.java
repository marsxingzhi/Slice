package com.mars.infra.mixin.lib;

import android.util.Log;

import com.mars.infra.mixin.annotations.Mixin;
import com.mars.infra.mixin.annotations.Proxy;

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

}
