package com.mars.infra.mixin;

import android.util.Log;

import com.mars.infra.mixin.lib.Login;

/**
 * Created by Mars on 2022/3/14
 */
public class LoginService {

    /**
     * aop替换Log.e方法，替换成Logger.superE
     */
    public static void startLogin() {
        Log.e("LoginService", "invoke login");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 真正执行登录逻辑
        Login login = new Login();
        login.login("zhangsan", "123456");


        Log.i("LoginService", "login end");
    }

    public static void logout() {
        Log.i("LoginService", "start logout");
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        Login.logout(1);
//        Login.logout_2(1);

        Login login = new Login();
//        login.logout_3(1);
        login.logout_4(1);

        Log.w("LoginService", "end logout");
    }
}
