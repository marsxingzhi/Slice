package com.mars.infra.mixin.hook;

import android.util.Log;

import com.mars.infra.mixin.annotations.Mixin;
import com.mars.infra.mixin.annotations.Proxy;
import com.mars.infra.mixin.annotations.ProxyInsnChain;
import com.mars.infra.mixin.lib.Login;
import com.mars.infra.mixin.utils.LoginUtils;

/**
 * Created by Mars on 2022/3/25
 * <p>
 * 背景：Login#login登录方法中并未对用户名和密码做有效性判断
 * hook Login对象的login实例方法，需要在hook方法参数中添加this对象
 */
@Mixin
class LoginMixin {

    /**
     * 方案一
     */
//    @Proxy(owner = "com/mars/infra/mixin/lib/Login", name = "login", isStatic = false)
    public static void hookLogin(Object obj, String username, String password) {
        System.out.println("hookLogin invoke.");
        Login login = (Login) obj;
        if (LoginUtils.check(username, password)) {
            login.login(username, password);
        } else {
            Log.e("Login", "用户名和密码不正确.");
        }
    }

    /**
     * 方案二
     */
    @Proxy(owner = "com.mars.infra.mixin.lib.Login", name = "login", isStatic = false)
    public static void hookLogin_2(Object obj, String username, String password) {
        System.out.println("hookLogin_2 invoke.");
        Login login = (Login) obj;
        if (login.check(username, password)) {
//            ProxyInsnChain.proceed(obj, username, password);
            // TODO 上述强转一下，这里传入login，而不是obj。这里需要优化，不需要强转也能支持，因为不一定可以拿到Login这个类！依赖不到
            ProxyInsnChain.proceed(login, username, password);
        } else {
            Log.e("Login", "用户名和密码不正确.");
        }
    }

    @Proxy(owner = "com.mars.infra.mixin.lib.Login", name = "logout", isStatic = true)
    public static void hookLogout(int code) {
        System.out.println("LoginMixin#hookLogout, invoke hookLogout, code = " + code);
//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        ProxyInsnChain.proceed(code);
        System.out.println("LoginMixin#hookLogout, logout success");
    }

    //    @Proxy(owner = "com.mars.infra.mixin.lib.Login", name = "logout_2", isStatic = true)
    public static boolean hookLogout_2(int code) {
        System.out.println("LoginMixin#hookLogout_2, invoke hookLogout, code = " + code);
        // 整体替换
//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        // 继续调用原方法
        ProxyInsnChain.proceed(code);
        System.out.println("LoginMixin#hookLogout_2, logout success");
        return true;
    }

    @Proxy(owner = "com.mars.infra.mixin.lib.Login", name = "logout_3", isStatic = false)
    public static void hookLogout_3(Object obj, int code) {
        System.out.println("LoginMixin#hookLogout_3, invoke hookLogout_3, code = " + code);
        Login login = (Login) obj;
//        login.logout_3(code);
        ProxyInsnChain.proceed(login, code);
        System.out.println("LoginMixin#hookLogout_3, logout success");
    }

    @Proxy(owner = "com.mars.infra.mixin.lib.Login", name = "logout_4", isStatic = false)
    public static boolean hookLogout_4(Object obj, int code) {
        System.out.println("LoginMixin#hookLogout_4, invoke hookLogout_4, code = " + code);
        Login login = (Login) obj;
//        boolean res = login.logout_4(code);
        boolean res = (boolean) ProxyInsnChain.proceed(login, code);
        System.out.println("LoginMixin#hookLogout_4, logout success");
        return res;
    }
}
