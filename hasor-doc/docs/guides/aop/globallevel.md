---
id: globallevel
sidebar_position: 4
title: c.全局拦截器
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 全局拦截器

全局拦截器实际是 匹配任意类任意方法 的一个拦截器。这种拦截器需要在 Module 中声明：

```java title='例如'
public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        //1.任意类
        Matcher<Class<?>> atClass = Matchers.anyClass();
        //2.任意方法
        Matcher<Method> atMethod = Matchers.anyMethod();
        //3.注册拦截器
        apiBinder.bindInterceptor(atClass, atMethod, new SimpleInterceptor());
    }
}
```
