package com.mars.infra.mixin.lib;

import android.text.TextUtils;

/**
 * Created by Mars on 2022/3/25
 */
public class Login {

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
}
