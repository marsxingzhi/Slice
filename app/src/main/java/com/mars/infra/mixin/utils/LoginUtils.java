package com.mars.infra.mixin.utils;

import android.text.TextUtils;

/**
 * Created by Mars on 2022/3/25
 */
public class LoginUtils {

    private LoginUtils() {}

    public static boolean check(String username, String password) {
        System.out.println("检查 用户名和密码是否 有效");
        System.out.println("username: " + username + " --- password: " + password);
        return !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password);
    }
}
