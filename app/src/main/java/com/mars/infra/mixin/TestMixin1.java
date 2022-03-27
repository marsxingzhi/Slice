package com.mars.infra.mixin;

import com.mars.infra.mixin.annotations.Mixin;
import com.mars.infra.mixin.annotations.Proxy;

/**
 * Created by Mars on 2022/3/21
 */
@Mixin
class TestMixin1 {

    @Proxy(owner = "android.util.Log", name = "i", isStatic = true)
    public static int hookLogI(String tag, String msg) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("TestMixin1--->hookLogI success, tag = " + tag + " msg = " + msg);
        return -1;
    }

    /**
     * 测试重复hook
     */
//    @Proxy(owner = "android/util/Log", name = "e")
    public static int testHookSameMethod(String tag, String msg) {
        return 0;
    }
}
