---
id: startbean
sidebar_position: 3
title: 2.6.2 启动创建 Bean
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 2.6.2 启动创建 Bean

## 方式一

如果您组合使用 `@Singleton` 注解和 `@Init` 注解，同时这个类在 Hasor 启动时通过 Module 预先注册了。那么 Hasor 会在启动时自动创建这个类并调用 init 方法。例如：

```java
@Singleton
public class PojoBean {
    @Init
    public void init(){
        ...
    }
}

public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.bindType(PojoBean.class);
    }
}
```

方式二

通过编码方式在 Module 初始化时指定，例如下面这样：

```java
public class PojoBean {
    public void init(){
        ...
    }
}

public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.bindType(PojoBean.class)
                .initMethod("init")    // 初始化方法，相当于 @Init 注解
                .asEagerSingleton();   // 单例，相当于 @Singleton 注解
    }
}
```
