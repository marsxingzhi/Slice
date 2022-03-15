package com.mars.infra.mixin.lib;

import android.util.Log;

import com.mars.infra.mixin.annotations.Proxy;

/**
 * Created by Mars on 2022/3/14
 */
public class Logger {

//    public static void superE(String tag, String msg) {
//        Log.e("super-" + tag, msg + "-增强了");
//    }

    /**
     * 使用Logger.superE方法替换Log.e方法，一定需要返回值
     * TODO
     * 1. 感觉最好还是加一个scope，可能只想hook业务方代码，对于系统或者其他三方库的不想要hook
     * 2. 新增一个descriptor
     */
    @Proxy(owner = "android/util/Log", name = "e")
    public static int superE(String tag, String msg) {
        Log.e(tag, msg + " ---> invoke by Logger");
        return -1;
    }
}
