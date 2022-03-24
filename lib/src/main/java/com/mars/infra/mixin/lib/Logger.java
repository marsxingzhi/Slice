package com.mars.infra.mixin.lib;

import android.util.Log;

/**
 * Created by Mars on 2022/3/14
 * <p>
 * sdk
 */
public class Logger {

    public static long printL(String tag, String msg) {
        System.out.println(tag + " ---> " + msg);
        return 1L;
    }

    public static long printLForce(String tag, String msg) {
        // Log.d方法没有被hook
        int value = Log.d(tag, msg);
        return (long) value;
    }

    public void printNotStatic() {
        System.out.println("this is printNotStatic of Logger");
    }

}
