# USAGE   

场景：在LoginService中调用登录SDK中的login方法，添加一个check用户名和密码有效性的逻辑。

```java
public class LoginService {
    public static void startLogin() {
        Login login = new Login();
        login.login("zhangsan", "123456");
    }

    public static void logout() {
        Login.logout(1);
    }
}
// login-sdk
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

    public static void logout(int code) {
        System.out.println("执行 Login#logout, code = " + code);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

使用方式：   

**一、目标方法是实例方法**      

1、添加@Mixin注解，标识该类是AOP类
2、添加Proxy注解，owner是目标方法所在类的全限定名，name是目标方法名，isStatic表示目标方法是否是静态方法
3、hook方法的描述符与目标方法一直，如果目标方法是实例方法，则需要在hook方法的第一个参数添加`Object obj`参数
4、MixinProxyInsn.invoke表示调用原方法，当然也可以使用全新的逻辑，不执行原方法
```java
@Mixin
class LoginMixin {
    @Proxy(owner = "com.mars.infra.mixin.lib.Login", name = "login", isStatic = false)
    public static void hookLogin(Object obj, String username, String password) {
        if (LoginUtils.check(username, password)) {
            MixinProxyInsn.invoke(obj, username, password);
        } else {
            throw Exception("username or password is not valid")
        }
    }
}
```    
**二、目标方法是静态方法**     

和实例方法的hook区别在于：
1、hook方法第一个参数不需要obj
2、@Proxy注解中的isStatic值为true
```java
@Mixin
class LoginMixin {
    @Proxy(owner = "com.mars.infra.mixin.lib.Login", name = "logout", isStatic = true)
    public static void hookLogout(int code) {
        System.out.println("LoginMixin#hookLogout, invoke hookLogout, code = " + code);
        MixinProxyInsn.invoke(code);
        System.out.println("LoginMixin#hookLogout, logout success");
    }
}
```
