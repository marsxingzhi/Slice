package com.mars.infra.mixin.lib;

import android.util.Log;

/**
 * Created by Mars on 2022/3/14
 */
public class Logger {

//    public static void superE(String tag, String msg) {
//        Log.e("super-" + tag, msg + "-增强了");
//    }

    public static int superE(String tag, String msg) {
        Log.e("super-" + tag, msg + "-增强了");
        return -1;
    }
}
