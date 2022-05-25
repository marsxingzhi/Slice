# Slice  
### 零、TODO    
- [ ] add scope 
- [ ] optimize process dir/jar
- [x] split transform，详见Fusion Project  

### 一、概述   
Slice Project是一个AOP框架，实现某些方法的增强。

### 二、使用方式     
#### 1. 场景描述      
在login module中存在Login类，在feed module中，点击评论后，执行登录逻辑 

需求：现在需要修改Login的login方法，在执行login方法时，先判断用户名和密码的有效性   
```java
public class LoginService {
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
}
``` 


#### 2. 引入Mixin插件  
- 在根build.gradle中，添加classpath
```groovy
classpath "com.mars.infra:mixin-plugin:0.1.3"
```  
- 在app的build.gradle中，apply plugin
```groovy
plugins {
    id 'com.mars.infra.mixin.plugin'
}
```   

#### 3. 使用   
定义`LoginMixin`类，该类不会被打到apk中
```java
@Mixin
class LoginMixin {

    @Proxy(owner = "com.mars.infra.mixin.lib.Login", name = "login", isStatic = false)
    public static void hookLogin(Object obj, String username, String password) {
        System.out.println("hookLogin_2 invoke.");
        Login login = (Login) obj;
        if (LoginUtils.check(username, password)) {
            MixinProxyInsn.invoke(obj, username, password);
        } else {
            Log.e("Login", "用户名和密码不正确.");
        }
    }
}
```   

#### 4. 最终效果    
反编译apk，查看hook后的效果。    
在LoginService中新建了`_generate_hookLogin_mixin`静态方法，原先在`startLogin`调用login对象的login方法，改成调用`_generate_hookLogin_mixin`静态方法
```java

    private static void _generate_hookLogin_mixin(Object obj, String username, String password) {
        System.out.println("hookLogin invoke.");
        Login login = (Login) obj;
        if (LoginUtils.check(username, password)) {
            login.login(username, password);
        } else {
            Log.e("Login", "用户名和密码不正确.");
        }
    }
```   
反编译apk，截图如下：
![hook-login](https://github.com/JohnnySwordMan/Mixin/blob/main/assets/hook-login.png)       

### 三、原理     
主要可以分成两个部分：
1. 收集阶段
   1. 收集@Proxy注解信息
   2. 指令修改，即MixinProxyInsn.invoke指令进行desugar
2. 修改阶段
   1. 添加新方法
   2. 修改调用点处的方法  

注意：详细分析，请参考 [Mixin项目解析](https://github.com/JohnnySwordMan/Mixin/blob/main/doc/ANALYZE.md)
