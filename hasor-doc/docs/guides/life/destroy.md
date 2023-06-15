---
id: destroybean
sidebar_position: 4
title: c.销毁 Bean
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 销毁 Bean

:::tip
需要注意的是只有单例的对象才支持销毁能力。
:::

## 方式一

注解换成 `net.hasor.core.Destroy` 或 `javax.annotation.PreDestroy`

组合使用 `@Singleton` 注解和 `@Destroy` 注解。当创建 Bean 之后 Hasor 就会自动跟踪它，之后一旦 Hasor 容器进入 destroy 过程就会自动调用它：

```java
@Singleton
public class PojoBean {
    @Destroy
    public void destroy(){
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

ApiBinder 的方法对应的是 destroyMethod，通过编码方式在 Module 初始化时指定，例如下面这样：

```java
public class PojoBean {
    public void destroy(){
        ...
    }
}

public class MyModule implements Module {
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.bindType(PojoBean.class)
                .destroyMethod("destroy") // 销毁方法，相当于 @Destroy 注解
                .asEagerSingleton();      // 单例，相当于 @Singleton 注解
    }
}
```
