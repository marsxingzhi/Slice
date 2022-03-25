package com.mars.infra.mixin.hook;

import com.mars.infra.mixin.annotations.Mixin;
import com.mars.infra.mixin.annotations.Proxy;
import com.mars.infra.mixin.annotations.ProxyInsnChain;

/**
 * Created by Mars on 2022/3/24
 */
@Mixin
class LoggerMixin {


    /**
     * hook Logger.printL方法
     */
    @Proxy(owner = "com/mars/infra/mixin/lib/Logger", name = "printL", isStatic = true)
    public static long hookPrintL(String tag, String msg) {
        long res = -1;
         try {
             res =  (long) ProxyInsnChain.proceed(tag, msg);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return res;
    }

    @Proxy(owner = "com/mars/infra/mixin/lib/Logger", name = "printLForce", isStatic = true)
    public static long hookPrintLForce(String tag, String msg) {
        long res = -1;
        try {
            res =  (long) ProxyInsnChain.proceed(tag, msg + " ---> has hooked success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * hook Logger的printNotStatic方法，实例方法
     *
     * 1. 首先无论hook的是静态方法，还是非静态方法，LoggerMixin最终是不会打进apk中的（最终效果），因此这里统一使用的是静态方法
     */
//    @Proxy(owner = "com/mars/infra/mixin/lib/Logger", name = "printNotStatic")
    public static void hookPrintNotStatic() {
        System.out.println("LoggerMixin#hookPrintNotStatic, hook success, Logger对象的printNotStatic被hook成功了");
    }
}


