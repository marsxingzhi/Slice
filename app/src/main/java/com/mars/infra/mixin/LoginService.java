package com.mars.infra.mixin;

import android.util.Log;

/**
 * Created by Mars on 2022/3/14
 */
public class LoginService {

    /**
     * aop替换Log.e方法，替换成Logger.superE
     */
    public static void login() {
        Log.e("LoginService", "invoke login");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
