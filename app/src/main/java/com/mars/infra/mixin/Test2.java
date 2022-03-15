package com.mars.infra.mixin;

import com.mars.infra.mixin.annotations.Proxy;

/**
 * Created by Mars on 2022/3/15
 */
class Test2 {

    @Proxy(owner = "android/util/Log", name = "e")
    public static void test(String tag, String msg) {

    }
}
