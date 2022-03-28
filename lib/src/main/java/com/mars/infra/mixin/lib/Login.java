package com.mars.infra.mixin.lib;

import android.text.TextUtils;

/**
 * Created by Mars on 2022/3/25
 */
public class Login {

    public static void init(String username, String password, int code) {
        System.out.println("Login#init start");
        System.out.println("打印参数： username = " + username + ", password = " + password + ", code = " + code);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Login#init done");
    }

    public void login(String username, String password) {
        System.out.println("开始登录~");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(username + " 登录成功");
    }

    public boolean check(String username, String password) {
        System.out.println("检查用户名和密码是否有效");
        return !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password);
    }

    public static void logout(int code) {
        System.out.println("执行 Login#logout, code = " + code);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean logout_2(int code) {
        System.out.println("执行 Login#logout_2, code = " + code);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return code == 1;
    }

    public void logout_3(int code) {
        System.out.println("执行 Login#logout_3, code = " + code);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean logout_4(int code) {
        System.out.println("执行 Login#logout_4, code = " + code);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return code == 1;
    }
}
