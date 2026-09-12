---
id: globallevel
sidebar_position: 4
title: 全局拦截器
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 全局拦截器

全局拦截器实际是 匹配任意类任意方法 的一个拦截器。这种拦截器需要在 Module 中声明：

```java title='例如'
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        //1.任意类
        Predicate<Class<?>> atClass = Matchers.anyClass();
        //2.任意方法
        Predicate<Method> atMethod = Matchers.anyMethod();
        //3.注册拦截器
        apiBinder.bindInterceptor(atClass, atMethod, new SimpleInterceptor());
    }
}
```
